package com.github.xeonkryptos.eclipseprojectcreator.import

import com.github.xeonkryptos.eclipseprojectcreator.ivy.EclipseIvyCommons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import java.nio.file.Files
import java.nio.file.Paths
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class EclipseModuleImportListener : ModuleListener {

    override fun moduleAdded(project: Project, module: Module) {
        val moduleDirPath = Paths.get(ModuleUtil.getModuleDirPath(module))
        val classpathFile = Files.list(moduleDirPath).use { fileStream ->
            fileStream.filter { it.fileName.toString() == EclipseXml.CLASSPATH_FILE }.findFirst().orElse(null)
        }
        if (classpathFile != null) { // The new module seems to be a new Eclipse project. Not yet sure, if it's an import of an existing module or a creation maybe with this plugin.
            val relativeNioPath = moduleDirPath.relativize(classpathFile)
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
