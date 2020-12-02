package com.github.xeonkryptos.eclipseprojectcreator.icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

/**
 * @author Xeonkryptos
 * @since 26.08.2020
 */
interface PluginIcons {

    companion object {
        val ECLIPSE: Icon = IconLoader.getIcon("/icons/eclipse.png", PluginIcons::class.java)
    }
}
