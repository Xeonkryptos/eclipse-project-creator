package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import org.jetbrains.annotations.NotNull
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class IvyFileCreationListener : BulkFileListener {

    private val projectManager = ProjectManager.getInstance()

    private var fileIndexContainers: List<FileIndexProjectContainer> = emptyList()
    private var eclipseFileCreationEventContainers: List<VFileCreateEvent> = emptyList()

    @Override
    @SuppressWarnings("unchecked")
    override fun before(@NotNull events: List<VFileEvent>) {
        val openProjects = projectManager.openProjects
        val fileIndices = ArrayList<FileIndexProjectContainer>(openProjects.size)
        for (i in openProjects.indices) {
            val openProject = openProjects[i]
            val projectRootManager = ProjectRootManager.getInstance(openProject)
            fileIndices.add(FileIndexProjectContainer(openProject, projectRootManager.fileIndex))
        }
        this.fileIndexContainers = fileIndices
        eclipseFileCreationEventContainers = events.asSequence()
            .filter { it.isValid && it is VFileCreateEvent }
            .map { it as VFileCreateEvent }
            .filter { !it.isDirectory && (EclipseXml.CLASSPATH_FILE == it.childName || EclipseXml.PROJECT_FILE == it.childName) }
            .toList()
    }

    @Override
    override fun after(@NotNull events: List<VFileEvent>) {
        eclipseFileCreationEventContainers.asSequence().mapNotNull { it.file }.mapNotNull { virtualFile ->
            return@mapNotNull fileIndexContainers.asSequence()
                .filter { it.fileIndex.isInContent(virtualFile) }
                .map { fileIndexContainer -> createModuleProjectFileContainerForFile(fileIndexContainer, virtualFile) }
                .firstOrNull()
        }.filter { it.virtualFile?.isInLocalFileSystem == true }.forEach { enrichEclipseFiles(it) }
    }

    private fun createModuleProjectFileContainerForFile(fileIndexContainer: FileIndexProjectContainer, virtualFile: VirtualFile): ModuleProjectFileContainer {
        ModuleManager.getInstance(fileIndexContainer.project).modules.forEach { module ->
            val moduleRootManager = ModuleRootManager.getInstance(module)
            val moduleFileIndex = moduleRootManager.fileIndex
            if (moduleFileIndex.isInContent(virtualFile)) {
                return ModuleProjectFileContainer(fileIndexContainer.project, module, virtualFile)
            }
        }
        return ModuleProjectFileContainer(fileIndexContainer.project, virtualFile = virtualFile)
    }

    private fun enrichEclipseFiles(moduleProjectFileContainer: ModuleProjectFileContainer) {
        if (EclipseXml.PROJECT_FILE == moduleProjectFileContainer.virtualFile?.name) {
            EclipseIvyUpdater.updateProjectFileWithIvyNature(moduleProjectFileContainer.project, moduleProjectFileContainer.virtualFile)
        } else if (moduleProjectFileContainer.module != null && moduleProjectFileContainer.virtualFile != null) {
            EclipseIvyUpdater.updateClasspathFileWithIvyContainer(moduleProjectFileContainer.module, moduleProjectFileContainer.virtualFile)
        }
    }

    private data class FileIndexProjectContainer(val project: Project, val fileIndex: ProjectFileIndex)

    private data class ModuleProjectFileContainer(val project: Project, val module: Module? = null, val virtualFile: VirtualFile?)
}
