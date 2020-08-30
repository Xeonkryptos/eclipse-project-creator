# eclipse-project-creator

![Build](https://github.com/Xeonkryptos/eclipse-project-creator/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/14967-eclipse-project-creator)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/14967-eclipse-project-creator)

<!-- Plugin description -->
Simple and small plugin to improve cooperation with Eclipse. When creating a new project in IntelliJ with the build-in wizard, you'll typically need to add the files required by Eclipse to work
properly (.project and .classpath) manually. Don't forget to edit the relevant places within them! This sucks if you'll need to do it every time, so your colleagues working with Eclipse don't
complain.<br/>
To improve this behaviour this small plugin adds Eclipse as external framework support for Java-based projects. Means, in the project/module wizard for Java projects, you have a new entry allowing
you to create with the basic project structure defined by IntelliJ the .project and .classpath file set to the minimum required. You don't need to bother to create them by yourself or use the export
feature of the bundled Eclipse plugin.

To improve your usual work life, sources roots are synced into your .classpath file on every change as long as you don't let IntelliJ the syncing.

Additionally, it supports Ivy in such as if it detects the installed plugin IvyIDEA and its facet on a module, it automatically updates the .classpath and .project files. Required is the modified version
of IvyIDEA that adds the same library creation support into the project wizard as this plugin provides. The modified version you'll find [here](https://github.com/Xeonkryptos/ivyidea/tree/new-features).
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "eclipse-project-creator"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/Xeonkryptos/eclipse-project-creator/releases/latest) and install it manually using
  <kbd>Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

