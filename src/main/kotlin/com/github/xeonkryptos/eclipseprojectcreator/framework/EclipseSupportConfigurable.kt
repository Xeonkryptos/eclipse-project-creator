package com.github.xeonkryptos.eclipseprojectcreator.framework

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyUpdater
import com.github.xeonkryptos.eclipseprojectcreator.util.EclipseUtil
import com.intellij.facet.FacetTypeRegistry
import com.intellij.facet.ProjectFacetManager
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModifiableModelsProvider
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ModuleRootModel
import com.intellij.openapi.roots.ui.configuration.libraries.CustomLibraryDescription
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.nio.file.Path
import javax.swing.JComponent
import org.jdom.output.EclipseJDOMUtil
import org.jetbrains.idea.eclipse.EclipseXml
import org.jetbrains.idea.eclipse.conversion.DotProjectFileHelper
import org.jetbrains.idea.eclipse.conversion.EclipseClasspathWriter

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
        val storageRoot = EclipseUtil.getModuleStorageRoot(module)
        DotProjectFileHelper.saveDotProjectFile(module, storageRoot)

        val model: ModuleRootModel = ModuleRootManager.getInstance(module)
        val classpathElement = EclipseClasspathWriter().writeClasspath(model)

        FacetTypeRegistry.getInstance().facetTypeIds.firstOrNull { facetTypeId -> facetTypeId.toString() == "IvyIDEA" }.let { ivyIdeaFacetType ->
            if (ivyIdeaFacetType != null && ProjectFacetManager.getInstance(module.project).getModulesWithFacet(ivyIdeaFacetType).contains(module)) {
                EclipseIvyUpdater.updateClasspathFileWithIvyContainer(module, providedClasspathElement = classpathElement)

                LocalFileSystem.getInstance().findFileByNioFile(Path.of(storageRoot, EclipseXml.PROJECT_FILE))?.let { virtualProjectFile ->
                    EclipseIvyUpdater.updateProjectFileWithIvyNature(module.project, virtualProjectFile)
                }
            }
        }

        val classpathFile = File(storageRoot, EclipseXml.CLASSPATH_FILE)
        if (FileUtil.createIfDoesntExist(classpathFile)) {
            EclipseJDOMUtil.output(classpathElement, classpathFile, module.project)
            EclipseUtil.refreshAfterJDOMWrite(classpathFile.path)
        }
    }
}
