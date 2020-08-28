package com.github.xeonkryptos.eclipseprojectcreator.framework

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyUpdater
import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ProjectFacetManager
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.vfs.VirtualFileManager
import org.apache.groovy.util.Maps
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.swing.JComponent

/**
 * @author Xeonkryptos
 * @since 26.08.2020
 */
class EclipseSupportConfigurable : FrameworkSupportInModuleConfigurable() {

    override fun createComponent(): JComponent? {
        return null
    }

    override fun createLibraryDescription(): CustomLibraryDescription? {
        return null
    }

    override fun isOnlyLibraryAdded(): Boolean {
        return false
    }

    override fun addSupport(module: Module, rootModel: ModifiableRootModel, modifiableModelsProvider: ModifiableModelsProvider) {
        val moduleBaseDir = Paths.get(module.moduleFilePath)
        val projectTargetFile = moduleBaseDir.resolveSibling(".project")
        val classpathTargetFile = moduleBaseDir.resolveSibling(".classpath")

        val templateAttributes = Maps.of("MODULE_NAME", module.name)
        Files.newBufferedWriter(projectTargetFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE).use { writer ->
            this::class.java.getResource("/templates/.project")?.readText(StandardCharsets.UTF_8)?.let { fileContent ->
                val finalizedProjectFileContent = FileTemplateUtil.mergeTemplate(templateAttributes, fileContent, true)
                writer.write(finalizedProjectFileContent)
            }
        }
        Files.newBufferedWriter(classpathTargetFile, StandardCharsets.UTF_8, StandardOpenOption.CREATE).use { writer ->
            this::class.java.getResource("/templates/.classpath")?.readText(StandardCharsets.UTF_8)?.let { fileContent ->
                val finalizedClasspathFileContent = FileTemplateUtil.mergeTemplate(templateAttributes, fileContent, true)
                writer.write(finalizedClasspathFileContent)
            }
        }

        FacetTypeRegistry.getInstance().facetTypeIds.first { facetTypeId -> facetTypeId.toString() == "IvyIDEA" }.let { ivyIdeaFacetType ->
            if (ProjectFacetManager.getInstance(module.project).getModulesWithFacet(ivyIdeaFacetType).contains(module)) {
                VirtualFileManager.getInstance().findFileByNioPath(projectTargetFile)?.let {
                    EclipseIvyUpdater.updateProjectFile(module.project, it)
                }
                VirtualFileManager.getInstance().findFileByNioPath(classpathTargetFile)?.let {
                    EclipseIvyUpdater.updateClasspathFile(module.project, module, it)
                }
            }
        }
    }
}
