package com.github.xeonkryptos.eclipseprojectcreator.framework

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyUpdater
import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ProjectFacetManager
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.vfs.VirtualFileManager
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import javax.swing.JComponent
import org.jetbrains.idea.eclipse.EclipseXml

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
        val moduleBaseDir = Paths.get(ModuleUtil.getModuleDirPath(module))
        val projectTargetFile = moduleBaseDir.resolve(EclipseXml.PROJECT_FILE)
        val classpathTargetFile = moduleBaseDir.resolve(EclipseXml.CLASSPATH_FILE)

        val templateAttributes = mapOf(Pair("MODULE_NAME", module.name))
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

        FacetTypeRegistry.getInstance().facetTypeIds.firstOrNull { facetTypeId -> facetTypeId.toString() == "IvyIDEA" }.let { ivyIdeaFacetType ->
            if (ivyIdeaFacetType != null && ProjectFacetManager.getInstance(module.project).getModulesWithFacet(ivyIdeaFacetType).contains(module)) {
                val virtualFileManager = VirtualFileManager.getInstance()
                virtualFileManager.findFileByNioPath(projectTargetFile)?.let {
                    EclipseIvyUpdater.updateProjectFileWithIvyNature(module.project, it)
                }
                virtualFileManager.findFileByNioPath(classpathTargetFile)?.let {
                    EclipseIvyUpdater.updateClasspathFileWithIvyContainer(module.project, module, it)
                }
            }
        }
    }
}
