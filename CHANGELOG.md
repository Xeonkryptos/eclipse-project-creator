<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# eclipse-project-creator Changelog

## [Unreleased]

### Added

- Ensures, only those source roots are defined within the .classpath file that really exists within the local file system. Source roots directories removed from the file system gets removed from within
the .classpath file, too.
- Removing ivy container reference imported by Eclipse project import basing on an ivy project within the module's dependency list as long as the IvyIDEA project is installed

### Changed

- Place new src tags after the last found one or at the start of the file

### Fixed

- Fixed too many threads with the same stacktrace when module roots changes
- Fixed update mechanism when modifying local files (shouldn't throw an invalid file tree exception anymore)

## [0.0.3] - 2020-09-01

### Changed

- Modified keys provided for coalesceBy when starting a non blocked read action 

## [0.0.2] - 2020-08-30

### Added

- Support for basic synchronization of sources roots of a module into Eclipse's .classpath when IntelliJ doesn't sync into it by itself
- Use constants available by the bundled Eclipse plugin

## [0.0.1] - 2020-08-28

### Added
- Support for Eclipse as framework when creating Java projects/modules via the wizards
- Support for IvyIDEA to modify .project and .classpath file with ivy nature and ivy container
