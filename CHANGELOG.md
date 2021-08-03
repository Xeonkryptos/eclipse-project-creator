<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# eclipse-project-creator Changelog

## [Unreleased]
## [0.0.10]

### Fixed

- Fixed NullPointerException
- Fixed .project enrichment with ivy nature

## [0.0.9]

### Changed

- Made compatible to the newest IntelliJ IDE platform version

## [0.0.8]

### Fixed

- Fixed NullPointerException when working with a project without any modules

### Security
## [0.0.7]

### Changed

- Fixed thrown exception when creating a new module with Eclipse and Ivy framework at the same time
- Create .classpath and .project files with system-dependent line separators

## [0.0.6] - 2020-12-02

### Changed

- Updated supported version to IntelliJ 2020.3 (the second)
- Replaced deprecated API call
- Replaced internal API calls

## [0.0.5] - 2020-12-02

### Changed

- Update compatibility to IntelliJ 2020.3

## [0.0.4] - 2020-09-08

### Added

- Ensures, only those source roots are defined within the .classpath file that really exists within the local file system. Source roots directories removed from the file system gets removed from within
the .classpath file, too.
- Removing ivy container reference imported by Eclipse project import basing on an ivy project within the module's dependency list as long as the IvyIDEA project is installed

### Changed

- Place new src tags after the last found one or at the start of the file
- Only synchronize existing source entries into .classpath

### Fixed

- Fixed too many threads with the same stacktrace when module roots changes
- Fixed update mechanism when modifying local files (shouldn't throw an invalid file tree exception anymore)
- Fixed synchronization of source roots into .classpath files of the modules

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
