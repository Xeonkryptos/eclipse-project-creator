package com.github.xeonkryptos.eclipseprojectcreator.psi

import com.intellij.lang.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.PsiManager

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class EclipseFileViewProviderFactory : FileViewProviderFactory {

    override fun createFileViewProvider(file: VirtualFile, language: Language?, manager: PsiManager, eventSystemEnabled: Boolean): FileViewProvider {
        return EclipseFileViewProvider(manager, file, eventSystemEnabled)
    }
}
