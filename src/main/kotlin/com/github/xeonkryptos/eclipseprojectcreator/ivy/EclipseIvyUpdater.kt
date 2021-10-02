package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.github.xeonkryptos.eclipseprojectcreator.util.EclipseUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.JDOMUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jdom.Element
import org.jdom.output.EclipseJDOMUtil
import org.jetbrains.idea.eclipse.EclipseXml
import org.jetbrains.idea.eclipse.conversion.EclipseClasspathWriter

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class EclipseIvyUpdater private constructor() {

    companion object {

        @JvmStatic
        fun updateClasspathFileWithIvyContainer(module: Module, virtualClasspathFile: VirtualFile? = null, providedClasspathElement: Element? = null) {
            val classpathElement = providedClasspathElement ?: JDOMUtil.load(virtualClasspathFile!!.toNioPath())

            val ivyDeContainerPath = EclipseIvyCommons.computeIvyDeContainerPath(module)

            var foundIvyDeContainer = false
            for (classpathEntryElement in classpathElement.getChildren(EclipseXml.CLASSPATHENTRY_TAG)) {
                val pathAttributeValue = classpathEntryElement.getAttributeValue(EclipseXml.PATH_ATTR)
                if (pathAttributeValue.startsWith(EclipseIvyCommons.IVYDE_CONTAINER_NAME)) {
                    classpathEntryElement.setAttribute(EclipseXml.PATH_ATTR, ivyDeContainerPath)
                    foundIvyDeContainer = true
                    break
                }
            }

            if (!foundIvyDeContainer) {
                EclipseClasspathWriter.addOrderEntry(EclipseXml.CON_KIND, ivyDeContainerPath, classpathElement, mapOf())
            }

            if (virtualClasspathFile != null) {
                EclipseJDOMUtil.output(classpathElement, virtualClasspathFile.toNioPath().toFile(), module.project)
                EclipseUtil.refreshAfterJDOMWrite(virtualClasspathFile.path)
            }
        }

        @JvmStatic
        fun updateProjectFileWithIvyNature(project: Project, virtualProjectFile: VirtualFile) {
            val element = JDOMUtil.load(virtualProjectFile.toNioPath())

            val naturesElement = element.getChild("natures")
            for (natureElement in naturesElement.getChildren("nature")) {
                if (natureElement.value == EclipseIvyCommons.IVY_NATURE) return
            }

            val natureElement = Element("nature")
            natureElement.text = EclipseIvyCommons.IVY_NATURE
            naturesElement.addContent(natureElement)

            EclipseJDOMUtil.output(element, virtualProjectFile.toNioPath().toFile(), project)
            EclipseUtil.refreshAfterJDOMWrite(virtualProjectFile.path)
        }
    }
}
