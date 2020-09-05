package com.github.xeonkryptos.eclipseprojectcreator.helper

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class PsiDocumentWriterHelper private constructor() {

    companion object {
        @JvmStatic
        fun executePsiWriteAction(project: Project, psiFile: PsiFile, writeAction: Runnable) {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                psiDocumentManager.getDocument(psiFile)?.let { document -> psiDocumentManager.commitDocument(document) }
                if (psiFile.isValid && psiFile.isWritable) {
                    writeAction.run()
                }
            }
        }
    }
}
