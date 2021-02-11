package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.github.xeonkryptos.eclipseprojectcreator.psi.PsiDocumentWriterHelper
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

            PsiDocumentWriterHelper.executePsiWriteAction(project, psiClasspathFile) {
                var notFound = true
                it.rootTag?.findSubTags(EclipseXml.CLASSPATHENTRY_TAG)?.let { classPathEntryTags ->
                    for (classPathEntryTag in classPathEntryTags) {
                        val kindAttribute = classPathEntryTag.getAttributeValue(EclipseXml.KIND_ATTR)
                        val pathAttribute = classPathEntryTag.getAttributeValue(EclipseXml.PATH_ATTR)

                        if (isKindOfTypeCon(kindAttribute) && isPathAttributeReferencingToIvy(pathAttribute)) {
                            notFound = false
                            break
                        }
                    }

                    if (notFound) {
                        val classPathEntryTag = it.rootTag?.createChildTag(EclipseXml.CLASSPATHENTRY_TAG, null, null, false)
                        classPathEntryTag?.setAttribute(EclipseXml.KIND_ATTR, EclipseXml.CON_KIND)
                        classPathEntryTag?.setAttribute(EclipseXml.PATH_ATTR, "${EclipseIvyCommons.IVYDE_CONTAINER_NAME}/?project=${module.name}\$amp;ivyXmlPath=ivy.xml\$amp;confs=*")
                        it.rootTag?.addSubTag(classPathEntryTag, false)
                    }
                }
            }
        }

        private fun isKindOfTypeCon(kindAttribute: String?): Boolean {
            return kindAttribute != null && kindAttribute == "con"
        }

        private fun isPathAttributeReferencingToIvy(pathAttribute: String?): Boolean {
            return pathAttribute != null && pathAttribute.startsWith(EclipseIvyCommons.IVYDE_CONTAINER_NAME)
        }

        @JvmStatic
        fun updateProjectFileWithIvyNature(project: Project, virtualProjectFile: VirtualFile) {
            val psiProjectFile = PsiManager.getInstance(project).findFile(virtualProjectFile) as XmlFile
            PsiDocumentWriterHelper.executePsiWriteAction(project, psiProjectFile) {
                val naturesTag = it.rootTag?.findFirstSubTag("natures")
                val ivyNatureFound = naturesTag?.findSubTags("nature")?.any { natureTag -> natureTag.textMatches(EclipseIvyCommons.IVY_NATURE) }
                if (ivyNatureFound == null || ivyNatureFound == false) {
                    val natureChildTag = naturesTag?.createChildTag("nature", null, EclipseIvyCommons.IVY_NATURE, false)
                    naturesTag?.addSubTag(natureChildTag, false)
                }
            }
        }
    }
}
