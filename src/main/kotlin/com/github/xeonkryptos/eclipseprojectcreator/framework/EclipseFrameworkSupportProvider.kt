package com.github.xeonkryptos.eclipseprojectcreator.framework

import com.intellij.framework.FrameworkTypeEx
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel
import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes

/**
 * @author Xeonkryptos
 * @since 26.08.2020
 */
class EclipseFrameworkSupportProvider : FrameworkSupportInModuleProvider() {

    override fun getFrameworkType(): FrameworkTypeEx {
        return FrameworkTypeEx.EP_NAME.findExtension(EclipseFrameworkType::class.java)!!
    }

    override fun createConfigurable(model: FrameworkSupportModel): FrameworkSupportInModuleConfigurable {
        return EclipseSupportConfigurable()
    }

    override fun isEnabledForModuleType(moduleType: ModuleType<*>): Boolean {
        return moduleType == StdModuleTypes.JAVA
    }

    override fun isEnabledForModuleBuilder(builder: ModuleBuilder): Boolean {
        return super.isEnabledForModuleBuilder(builder) && builder::class == JavaModuleBuilder::class
    }
}
