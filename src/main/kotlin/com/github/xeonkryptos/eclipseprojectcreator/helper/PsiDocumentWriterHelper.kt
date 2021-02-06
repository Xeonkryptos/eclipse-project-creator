package com.github.xeonkryptos.eclipseprojectcreator.helper

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.util.Consumer

/**
 * @author Xeonkryptos
 * @since 05.09.2020
 */
class PsiDocumentWriterHelper private constructor() {

    companion object {
        @JvmStatic
        fun <T : PsiFile> executePsiWriteAction(project: Project, psiFile: T, writeAction: Consumer<T>) {
            WriteCommandAction.runWriteCommandAction(project) {
                val psiDocumentManager = PsiDocumentManager.getInstance(project)
                val psiDocument = psiDocumentManager.getCachedDocument(psiFile)

                CommandProcessor.getInstance().executeCommand(project, {
                    psiDocument?.let { document -> psiDocumentManager.commitDocument(document) }
                    if (psiFile.isValid && psiFile.isWritable) {
                        writeAction.consume(psiFile)
                    }
                }, null, null, psiDocument)
            }
        }
    }
}
