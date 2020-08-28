package com.github.xeonkryptos.eclipseprojectcreator.framework

import com.github.xeonkryptos.eclipseprojectcreator.icons.PluginIcons
import com.intellij.framework.FrameworkTypeEx
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider
import javax.swing.Icon

/**
 * @author Xeonkryptos
 * @since 26.08.2020
 */
class EclipseFrameworkType: FrameworkTypeEx("Eclipse") {

    override fun getPresentableName(): String {
        return "Eclipse"
    }

    override fun getIcon(): Icon {
        return PluginIcons.ECLIPSE
    }

    override fun createProvider(): FrameworkSupportInModuleProvider {
        return EclipseFrameworkSupportProvider()
    }
}
