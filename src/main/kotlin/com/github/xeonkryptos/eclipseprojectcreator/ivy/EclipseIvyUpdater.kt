package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import org.jetbrains.idea.eclipse.EclipseXml

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class EclipseIvyUpdater private constructor() {

    companion object {

        @JvmStatic
        fun updateClasspathFileWithIvyContainer(project: Project, module: Module, virtualClasspathFile: VirtualFile) {
            val psiClasspathFile: XmlFile = PsiManager.getInstance(project).findFile(virtualClasspathFile) as XmlFile

            var notFound = true
            psiClasspathFile.rootTag?.findSubTags(EclipseXml.CLASSPATHENTRY_TAG)?.let { classPathEntryTags ->
                for (classPathEntryTag in classPathEntryTags) {
                    val kindAttribute = classPathEntryTag.getAttributeValue(EclipseXml.KIND_ATTR)
                    val pathAttribute = classPathEntryTag.getAttributeValue(EclipseXml.PATH_ATTR)

                    if (kindAttribute?.equals("con") == true && pathAttribute?.startsWith("org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER") == true) {
                        notFound = false
                        break
                    }
                }
            }
            if (notFound) {
                val classPathEntryTag = psiClasspathFile.rootTag?.createChildTag(EclipseXml.CLASSPATHENTRY_TAG, null, null, false)
                classPathEntryTag?.setAttribute(EclipseXml.KIND_ATTR, EclipseXml.CON_KIND)
                classPathEntryTag?.setAttribute(EclipseXml.PATH_ATTR, "org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER/?project=" + module.name + "\$amp;ivyXmlPath=ivy.xml\$amp;confs=*")
                WriteCommandAction.runWriteCommandAction(project) {
                    psiClasspathFile.rootTag?.addSubTag(classPathEntryTag, false)
                }
            }
        }

        @JvmStatic
        fun updateProjectFileWithIvyNature(project: Project, virtualProjectFile: VirtualFile) {
            val psiProjectFile = PsiManager.getInstance(project).findFile(virtualProjectFile) as XmlFile
            val naturesTag = psiProjectFile.rootTag?.findFirstSubTag("natures")
            val ivyNatureFound = naturesTag?.findSubTags("nature")?.any { natureTag -> natureTag.textMatches("org.apache.ivyde.eclipse.ivynature") }
            if (ivyNatureFound == null || ivyNatureFound == false) {
                val natureChildTag = naturesTag?.createChildTag("nature", null, "org.apache.ivyde.eclipse.ivynature", false)
                WriteCommandAction.runWriteCommandAction(project) {
                    naturesTag?.addSubTag(natureChildTag, false)
                }
            }
        }
    }
}
