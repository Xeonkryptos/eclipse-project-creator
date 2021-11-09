package com.github.xeonkryptos.eclipseprojectcreator.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleGrouper
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.impl.storage.ClasspathStorage
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem

class EclipseUtil {

    companion object {

        @JvmStatic
        fun getModuleStorageRoot(module: Module): String {
            val contentRoots = ModuleRootManager.getInstance(module).contentRoots
            return if (contentRoots.size == 1) contentRoots[0].path else ClasspathStorage.getStorageRootFromOptions(module)
        }

        @JvmStatic
        fun refreshAfterJDOMWrite(filePath: String) {
            ApplicationManager.getApplication().runWriteAction {
                LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(filePath))
            }
        }

        @Suppress("UnstableApiUsage")
        fun getCleanedModuleName(module: Module): String {
            return ModuleGrouper.instanceFor(module.project).getShortenedName(module)
        }
    }
}