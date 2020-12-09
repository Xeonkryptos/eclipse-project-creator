package com.github.xeonkryptos.eclipseprojectcreator.psi

import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import org.jetbrains.annotations.NotNull

/**
 * @author Xeonkryptos
 * @since 28.08.2020
 */
class EclipseFileViewProvider(@NotNull manager: PsiManager, @NotNull virtualFile: VirtualFile, eventSystemEnabled: Boolean) : SingleRootFileViewProvider(
    manager,
    virtualFile,
    eventSystemEnabled,
    XMLLanguage.INSTANCE
)
