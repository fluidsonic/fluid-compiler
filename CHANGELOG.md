# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/), and this project adheres to [Semantic Versioning](https://semver.org/).


## [0.14.0] - 2026-03-28

### Added
- KDoc documentation on all public API surfaces.
- `KotlinJvmTarget.v23` and `KotlinJvmTarget.v24` enum entries.
- Unit tests for all public APIs.
- GitHub Actions CI workflow.
- CLAUDE.md with project conventions.

### Changed
- Minimum JDK version is now 21.
- Updated to Kotlin 2.3.20.
- Updated to Gradle 9.4.1.
- Updated `io.fluidsonic.gradle` plugin from 2.0.0 to 3.0.0.
- Simplified classpath discovery by removing legacy JDK reflection code.

### Deprecated
- `KotlinCompiler.processors()` — KAPT is deprecated, migrate to KSP.
- `KotlinCompiler.kaptOptions()` — KAPT is deprecated, migrate to KSP.
- `KaptOptions.toBuilder()` — KAPT is deprecated, migrate to KSP.

### Removed
- `KotlinJvmTarget` entries below v21 (`v1_6`, `v1_8`, `v9` through `v20`).
- `loadToolsJarIfNeeded()` and related tools.jar discovery code (unnecessary on JDK 9+).
- ClassLoader reflection utilities (`addUrl`, `urlClassPath`, `urls` extensions).
- `Boolean.thenTake()` utility function.


## [0.13.0]

### Changed
- Updated dependencies.
