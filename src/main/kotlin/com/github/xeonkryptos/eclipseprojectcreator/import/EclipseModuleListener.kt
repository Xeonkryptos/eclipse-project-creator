package com.github.xeonkryptos.eclipseprojectcreator.import

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyCommons
import com.github.xeonkryptos.eclipseprojectcreator.util.EclipseUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootModificationUtil
import com.intellij.openapi.roots.impl.storage.ClasspathStorage
import com.intellij.openapi.util.JDOMUtil
import com.intellij.util.Function
import java.nio.file.Files
import java.nio.file.Path
import org.jdom.output.EclipseJDOMUtil
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class EclipseModuleListener : ModuleListener {

    override fun moduleAdded(project: Project, module: Module) {
        val storageRoot = ClasspathStorage.getStorageRootFromOptions(module)
        val classpathFile = Path.of(storageRoot, EclipseXml.CLASSPATH_FILE)
        if (Files.exists(classpathFile)) {
            removeIvyDeContainerEntry(module)
        }
    }

    private fun removeIvyDeContainerEntry(module: Module) {
        ModuleRootModificationUtil.modifyModel(module) {
            val detectedIvyContainerEntry = it.orderEntries.firstOrNull { orderEntry -> orderEntry.presentableName.startsWith(EclipseIvyCommons.IVYDE_CONTAINER_NAME) }
            if (detectedIvyContainerEntry != null) {
                it.removeOrderEntry(detectedIvyContainerEntry)
                return@modifyModel true
            }
            return@modifyModel false
        }
    }

    override fun modulesRenamed(project: Project, modules: MutableList<out Module>, oldNameProvider: Function<in Module, String>) {
        for (module in modules) {
            val storageRoot = ClasspathStorage.getStorageRootFromOptions(module)
            updateClasspathFileEntriesAfterRename(storageRoot, module)
            updateProjectFileAfterRename(storageRoot, module)
        }
    }

    private fun updateClasspathFileEntriesAfterRename(storageRoot: String, module: Module) {
        val classpathFile = Path.of(storageRoot, EclipseXml.CLASSPATH_FILE)
        if (Files.exists(classpathFile)) {
            val classpathElement = JDOMUtil.load(classpathFile)
            for (classpathEntryElement in classpathElement.getChildren(EclipseXml.CLASSPATHENTRY_TAG)) {
                val pathAttributeValue = classpathEntryElement.getAttributeValue(EclipseXml.PATH_ATTR)
                if (pathAttributeValue.startsWith(EclipseIvyCommons.IVYDE_CONTAINER_NAME)) {
                    val newIvyDeContainerPath = EclipseIvyCommons.computeIvyDeContainerPath(module)
                    classpathEntryElement.setAttribute(EclipseXml.PATH_ATTR, newIvyDeContainerPath)

                    EclipseJDOMUtil.output(classpathElement, classpathFile.toFile(), module.project)
                    EclipseUtil.refreshAfterJDOMWrite(classpathFile.toString())
                    break
                }
            }
        }
    }

    private fun updateProjectFileAfterRename(storageRoot: String, module: Module) {
        val eclipseProjectFile = Path.of(storageRoot, EclipseXml.PROJECT_FILE)
        if (Files.exists(eclipseProjectFile)) {
            val projectElement = JDOMUtil.load(eclipseProjectFile)
            val projectNameElement = projectElement.getChild(EclipseXml.NAME_TAG)
            projectNameElement.text = EclipseUtil.getCleanedModuleName(module)

            EclipseJDOMUtil.output(projectElement, eclipseProjectFile.toFile(), module.project)
            EclipseUtil.refreshAfterJDOMWrite(eclipseProjectFile.toString())
        }
    }
}
