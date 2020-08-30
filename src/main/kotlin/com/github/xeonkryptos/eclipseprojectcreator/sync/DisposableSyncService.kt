package com.github.xeonkryptos.eclipseprojectcreator.sync

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project

/**
 * @author Xeonkryptos
 * @since 30.08.2020
 */
class DisposableSyncService(project: Project) : Disposable {

    override fun dispose() {
        // Nothing to do here
    }
}
