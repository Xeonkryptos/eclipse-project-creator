package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.intellij.openapi.application.ApplicationManager
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
import java.util.Objects
import java.util.stream.Collectors
import org.jetbrains.annotations.NotNull
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class IvyFileCreationListener : BulkFileListener {

    private val projectManager = ProjectManager.getInstance()

    private var fileIndices: List<Pair<Project, ProjectFileIndex>>? = null
    private var eclipseFileCreationEvents: List<VFileCreateEvent>? = null

    @Override
    @SuppressWarnings("unchecked")
    override fun before(@NotNull events: List<VFileEvent>) {
        val openProjects = projectManager.openProjects
        val fileIndices = ArrayList<Pair<Project, ProjectFileIndex>>(openProjects.size)
        for (i in openProjects.indices) {
            val openProject = openProjects[i]
            val projectRootManager = ProjectRootManager.getInstance(openProject)
            fileIndices.add(Pair(openProject, projectRootManager.fileIndex))
        }
        this.fileIndices = fileIndices
        eclipseFileCreationEvents = events.stream()
            .filter { it.isValid }
            .filter { it is VFileCreateEvent }
            .map { it as VFileCreateEvent }
            .filter { !it.isDirectory }
            .filter { EclipseXml.CLASSPATH_FILE == it.childName || EclipseXml.PROJECT_FILE == it.childName }
            .collect(Collectors.toList())
    }

    @Override
    override fun after(@NotNull events: List<VFileEvent>) {
        eclipseFileCreationEvents?.stream()?.map { it.file }?.filter {
            Objects.nonNull(it)
        }?.map { virtualFile ->
            for (pair in fileIndices!!) {
                if (virtualFile != null && pair.second.isInContent(virtualFile)) {
                    for (module in ModuleManager.getInstance(pair.first).modules) {
                        val moduleRootManager = ModuleRootManager.getInstance(module)
                        val moduleFileIndex = moduleRootManager.fileIndex
                        if (moduleFileIndex.isInContent(virtualFile)) {
                            return@map Pair<Pair<Project, Module?>, VirtualFile?>(Pair<Project, Module?>(pair.first, module), virtualFile)
                        }
                    }
                    return@map Pair<Pair<Project, Module?>, VirtualFile?>(Pair<Project, Module?>(pair.first, null), virtualFile)
                }
            }
            return@map null
        }?.filter(Objects::nonNull)?.forEach { pair ->
            if (EclipseXml.PROJECT_FILE == pair?.second?.name && pair.second?.isInLocalFileSystem == true) {
                ApplicationManager.getApplication().invokeLater {
                    EclipseIvyUpdater.updateProjectFileWithIvyNature(pair.first.first, pair.second!!)
                }
            } else if (pair?.second?.isInLocalFileSystem == true) {
                ApplicationManager.getApplication().invokeLater {
                    EclipseIvyUpdater.updateClasspathFileWithIvyContainer(pair.first.first, pair.first.second!!, pair.second!!)
                }
            }
        }
    }
}
