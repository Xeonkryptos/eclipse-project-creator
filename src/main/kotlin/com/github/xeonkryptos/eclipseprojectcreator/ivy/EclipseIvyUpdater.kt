package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class EclipseIvyUpdater private constructor() {

    companion object {

        @JvmStatic
        fun updateClasspathFile(project: Project, module: Module, virtualClasspathFile: VirtualFile) {
            val psiClasspathFile: XmlFile = PsiManager.getInstance(project).findFile(virtualClasspathFile) as XmlFile

            var notFound = true
            psiClasspathFile.rootTag?.findSubTags("classpathentry")?.let { classPathEntryTags ->
                for (classPathEntryTag in classPathEntryTags) {
                    val kindAttribute = classPathEntryTag.getAttribute("kind")
                    val pathAttribute = classPathEntryTag.getAttribute("path")

                    if (kindAttribute?.textMatches("con") == true && pathAttribute?.value?.startsWith("org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER") == true) {
                        notFound = false
                        break
                    }
                }
            }
            if (notFound) {
                val classPathEntryTag = psiClasspathFile.rootTag?.createChildTag("classpathentry", null, null, false)
                classPathEntryTag?.setAttribute("kind", "con")
                classPathEntryTag?.setAttribute("path", "org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER/?project=" + module.name + "\$amp;ivyXmlPath=ivy.xml\$amp;confs=*")
                WriteCommandAction.runWriteCommandAction(project) {
                    psiClasspathFile.rootTag?.addSubTag(classPathEntryTag, false)
                }
            }
        }

        @JvmStatic
        fun updateProjectFile(project: Project, virtualProjectFile: VirtualFile) {
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
