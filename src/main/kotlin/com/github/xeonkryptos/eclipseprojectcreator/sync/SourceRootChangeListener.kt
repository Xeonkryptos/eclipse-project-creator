package com.github.xeonkryptos.eclipseprojectcreator.sync

import com.github.xeonkryptos.eclipseprojectcreator.helper.PsiDocumentWriterHelper
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.impl.storage.ClassPathStorageUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.concurrency.AppExecutorUtil
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import org.jetbrains.idea.eclipse.EclipseXml
import org.jetbrains.jps.eclipse.model.JpsEclipseClasspathSerializer

/**
 * @author Xeonkryptos
 * @since 30.08.2020
 */
class SourceRootChangeListener : ModuleRootListener {

    companion object {
        @JvmStatic
        private val UNIQUE_ID = "ECLIPSE_PROJECT_CREATOR_SOURCE_ROOT_CHANGE_LISTENER"
        @JvmStatic
        private val BOUNDED_ECLIPSE_SOURCE_ROOT_CHANGE_LISTENER = AppExecutorUtil.createBoundedApplicationPoolExecutor("Bounded Eclipse File Updater", 4)
    }

    private val eclipseModules: MutableMap<Module, ChangeableSourceRoots> = ConcurrentHashMap()

    override fun rootsChanged(event: ModuleRootEvent) {
        val projectCreatorService = ServiceManager.getService(event.project, DisposableSyncService::class.java)
        ReadAction.nonBlocking(Callable { findChangedModuleSourceRoots(event.project) })
            .coalesceBy(event.project, UNIQUE_ID)
            .expireWith(projectCreatorService)
            .inSmartMode(event.project)
            .withDocumentsCommitted(event.project)
            .finishOnUiThread(ModalityState.NON_MODAL) { executeClasspathUpdateSync(it) }
            .submit(BOUNDED_ECLIPSE_SOURCE_ROOT_CHANGE_LISTENER)
    }

    private fun findChangedModuleSourceRoots(project: Project): List<DataHolder> {
        val classpathFiles = FilenameIndex.getFilesByName(project, EclipseXml.CLASSPATH_FILE, GlobalSearchScope.projectScope(project))
        return ModuleManager.getInstance(project).modules.filter { ClassPathStorageUtil.getStorageType(it) != JpsEclipseClasspathSerializer.CLASSPATH_STORAGE_ID }.mapNotNull { module ->
            val fileIndex = ModuleRootManager.getInstance(module).fileIndex

            classpathFiles.filter { classpathFile -> fileIndex.isInContent(classpathFile.virtualFile) }.firstOrNull {
                val changeableSourceRoots = eclipseModules.computeIfAbsent(module) {
                    return@computeIfAbsent ChangeableSourceRoots()
                }
                val sourceRoots = ModuleRootManager.getInstance(module).sourceRoots.filter { sourceRootFile -> sourceRootFile.isValid && sourceRootFile.exists() }
                changeableSourceRoots.updateSourcesRoots(sourceRoots)
                if (changeableSourceRoots.changed) {
                    return@mapNotNull DataHolder(module, changeableSourceRoots.sourceRoots, classpathFiles.first())
                }
                return@mapNotNull null
            }
            eclipseModules.remove(module)
            return@mapNotNull null
        }
    }

    private fun executeClasspathUpdateSync(dataHolders: List<DataHolder>) {
        dataHolders.mapNotNull {
            val moduleRootDir = it.module.moduleFile?.parent
            if (moduleRootDir != null) {
                val convertedSourceRoots = it.changeableSourcesRoots.filter { virtualSourceRootFile ->
                    virtualSourceRootFile.isValid
                }.mapNotNull { virtualSourceRootFile ->
                    VfsUtilCore.getRelativeLocation(virtualSourceRootFile, moduleRootDir)
                }
                return@mapNotNull ClasspathUpdateAction(it.module.project, convertedSourceRoots, it.classpathFile)
            }
            return@mapNotNull null
        }.forEach { it.updateClasspathFile() }
    }

    internal class ChangeableSourceRoots {

        private val _sourceRoots: MutableSet<VirtualFile> = CopyOnWriteArraySet()

        val sourceRoots: Set<VirtualFile> = _sourceRoots

        @Volatile
        var changed = true

        @Synchronized
        fun updateSourcesRoots(currentSourcesRoots: List<VirtualFile>) {
            changed = _sourceRoots.retainAll(currentSourcesRoots) or changed
            changed = _sourceRoots.addAll(currentSourcesRoots) or changed
        }
    }

    internal data class DataHolder(val module: Module, val changeableSourcesRoots: Set<VirtualFile>, val classpathFile: PsiFile)

    internal class ClasspathUpdateAction(private val project: Project, changeableSourceRoots: List<String>, private val classpathFile: PsiFile) {

        private val localChangeableSourceRoots = HashSet(changeableSourceRoots)

        fun updateClasspathFile() {
            val psiClasspathFile = classpathFile as XmlFile

            val deletableEntries: MutableSet<XmlTag> = HashSet()
            var lastFoundSrcTag: XmlTag? = null
            psiClasspathFile.rootTag?.findSubTags(EclipseXml.CLASSPATHENTRY_TAG)?.let { classPathEntryTags -> lastFoundSrcTag = synchronizeChangeableSourceRoots(classPathEntryTags, deletableEntries) }

            PsiDocumentWriterHelper.executePsiWriteAction(project, psiClasspathFile) {
                deletableEntries.forEach { it.delete() }

                localChangeableSourceRoots.forEach {
                    val classPathEntryTag = psiClasspathFile.rootTag?.createChildTag(EclipseXml.CLASSPATHENTRY_TAG, null, null, false)
                    if (classPathEntryTag != null) {
                        classPathEntryTag.setAttribute(EclipseXml.KIND_ATTR, EclipseXml.SRC_KIND)
                        classPathEntryTag.setAttribute(EclipseXml.PATH_ATTR, it)
                        if (lastFoundSrcTag != null) {
                            psiClasspathFile.rootTag?.addAfter(classPathEntryTag, lastFoundSrcTag)
                        } else {
                            psiClasspathFile.rootTag?.addSubTag(classPathEntryTag, true)
                        }
                    }
                }
            }
        }

        private fun synchronizeChangeableSourceRoots(classPathEntryTags: Array<XmlTag>, deletableEntries: MutableSet<XmlTag>): XmlTag? {
            var lastFoundSrcTag: XmlTag? = null
            for (classPathEntryTag in classPathEntryTags) {
                val kindAttribute = classPathEntryTag.getAttributeValue(EclipseXml.KIND_ATTR)
                val pathAttribute = classPathEntryTag.getAttribute(EclipseXml.PATH_ATTR)

                if (kindAttribute != null && kindAttribute == "src" && pathAttribute != null) {
                    if (!localChangeableSourceRoots.contains(pathAttribute.value)) {
                        deletableEntries.add(classPathEntryTag)
                        localChangeableSourceRoots.remove(pathAttribute.value)
                    } else if (localChangeableSourceRoots.contains(pathAttribute.value)) {
                        localChangeableSourceRoots.remove(pathAttribute.value)
                        lastFoundSrcTag = classPathEntryTag
                    }
                }
            }
            return lastFoundSrcTag
        }
    }
}
