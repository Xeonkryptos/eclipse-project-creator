package com.github.xeonkryptos.eclipseprojectcreator.sync

import com.github.xeonkryptos.eclipseprojectcreator.util.EclipseUtil
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootEvent
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.impl.storage.ClassPathStorageUtil
import com.intellij.openapi.util.JDOMUtil
import java.nio.file.Files
import java.nio.file.Path
import org.jdom.Element
import org.jdom.output.EclipseJDOMUtil
import org.jetbrains.idea.eclipse.EclipseXml
import org.jetbrains.jps.eclipse.model.JpsEclipseClasspathSerializer

/**
 * @author Xeonkryptos
 * @since 30.08.2020
 */
class SourceRootChangeListener : ModuleRootListener {

    override fun rootsChanged(event: ModuleRootEvent) {
        synchronizeModuleSourceRootsIntoEclipseClasspath(event.project)
    }

    private fun synchronizeModuleSourceRootsIntoEclipseClasspath(project: Project) {
        for (module in ModuleManager.getInstance(project).modules) {
            if (ClassPathStorageUtil.getStorageType(module) == JpsEclipseClasspathSerializer.CLASSPATH_STORAGE_ID) {
                continue
            }

            val storageRoot = EclipseUtil.getModuleStorageRoot(module)
            val storageRootPath = Path.of(storageRoot)
            val classpathFile = storageRootPath.resolve(EclipseXml.CLASSPATH_FILE)
            if (Files.exists(classpathFile)) {
                val classpathElement = JDOMUtil.load(classpathFile)
                val currentEclipseClasspathSources = collectSourceRootsFromEclipseClasspathFile(classpathElement)

                for (moduleSourceRoot in ModuleRootManager.getInstance(module).sourceRoots) {
                    val relativeSourceRootPath = storageRootPath.relativize(moduleSourceRoot.toNioPath()).toString().replace('\\', '/')
                    if (!currentEclipseClasspathSources.containsKey(relativeSourceRootPath)) {
                        val newClasspathEntryElement = Element(EclipseXml.CLASSPATHENTRY_TAG)
                        newClasspathEntryElement.setAttribute(EclipseXml.KIND_ATTR, EclipseXml.SRC_KIND)
                        newClasspathEntryElement.setAttribute(EclipseXml.PATH_ATTR, relativeSourceRootPath)
                        classpathElement.addContent(newClasspathEntryElement)
                    } else {
                        currentEclipseClasspathSources.remove(relativeSourceRootPath)
                    }
                }
                currentEclipseClasspathSources.values.forEach { classpathElement.removeContent(it) }

                EclipseJDOMUtil.output(classpathElement, classpathFile.toFile(), project)
                EclipseUtil.refreshAfterJDOMWrite(classpathFile.toString())
            }
        }
    }

    private fun collectSourceRootsFromEclipseClasspathFile(classpathElement: Element): MutableMap<String, Element> {
        val currentEclipseClasspathSources = mutableMapOf<String, Element>()
        for (classpathEntryElement in classpathElement.getChildren(EclipseXml.CLASSPATHENTRY_TAG)) {
            if (classpathEntryElement.getAttributeValue(EclipseXml.KIND_ATTR) == EclipseXml.SRC_KIND) {
                val pathAttributeValue = classpathEntryElement.getAttributeValue(EclipseXml.PATH_ATTR)
                currentEclipseClasspathSources[pathAttributeValue] = classpathEntryElement
            }
        }
        return currentEclipseClasspathSources
    }
}
