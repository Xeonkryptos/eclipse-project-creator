package com.github.xeonkryptos.eclipseprojectcreator.import

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyCommons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class EclipseModuleImportListener : ModuleListener {

    override fun moduleAdded(project: Project, module: Module) {
        val moduleDir = module.moduleFile?.parent
        val classpathFile = moduleDir?.findChild(EclipseXml.CLASSPATH_FILE)
        if (moduleDir != null && classpathFile != null) {
            val moduleDirNioPath = moduleDir.toNioPath()
            val classpathFileNioPath = classpathFile.toNioPath()
            val relativeNioPath = moduleDirNioPath.relativize(classpathFileNioPath)
            // The new module seems to be a new Eclipse project. Not yet sure, if it's an import of an existing module or a creation maybe with this plugin.
            if (relativeNioPath.toString() == EclipseXml.CLASSPATH_FILE) {
                val modifiableRootModel = ModuleRootManager.getInstance(module).modifiableModel
                val detectedIvyContainerEntry = modifiableRootModel.orderEntries.firstOrNull { orderEntry -> orderEntry.presentableName.startsWith(EclipseIvyCommons.IVYDE_CONTAINER_NAME) }
                if (detectedIvyContainerEntry != null) {
                    val application = ApplicationManager.getApplication()
                    application.invokeLater {
                        application.runWriteAction {
                            modifiableRootModel.removeOrderEntry(detectedIvyContainerEntry)
                            modifiableRootModel.commit()
                        }
                    }
                } else {
                    modifiableRootModel.dispose()
                }
            }
        }
    }
}
