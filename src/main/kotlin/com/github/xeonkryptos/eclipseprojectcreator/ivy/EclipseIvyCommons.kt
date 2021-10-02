package com.github.xeonkryptos.eclipseprojectcreator.ivy

import com.github.xeonkryptos.eclipseprojectcreator.util.EclipseUtil
import com.intellij.openapi.module.Module
/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class EclipseIvyCommons private constructor() {

    companion object {
        @JvmStatic
        val IVYDE_CONTAINER_NAME = "org.apache.ivyde.eclipse.cpcontainer.IVYDE_CONTAINER"
        @JvmStatic
        val IVY_NATURE = "org.apache.ivyde.eclipse.ivynature"

        fun computeIvyDeContainerPath(module: Module): String {
            return "$IVYDE_CONTAINER_NAME/?project=${EclipseUtil.getCleanedModuleName(module)}&amp;ivyXmlPath=ivy.xml&amp;confs=*"
        }
    }
}
