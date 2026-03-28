# fluid-compiler

Kotlin library that wraps `kotlin-compiler-embeddable` and `kotlin-annotation-processing-embeddable` to compile Kotlin code and run KAPT annotation processing programmatically.

## Conventions

- **Tags**: no `v` prefix (e.g. `0.14.0`, not `v0.14.0`)
- **Indentation**: tabs in Kotlin and Gradle files
- **Source layout**: `sources-jvm/`, `tests-jvm/`, `resources-jvm/` (configured by fluid-gradle plugin)
- **Package**: `io.fluidsonic.compiler`
- **Platform**: JVM-only (compiler-embeddable is JVM-only)

## Dependencies

- Depends on `kotlin-compiler-embeddable` and `kotlin-annotation-processing-embeddable` internal APIs
- These APIs are not stable across Kotlin versions — updates may require import/API changes
- Uses `io.fluidsonic.gradle` plugin for build configuration

## Build

```sh
./gradlew build        # full build
./gradlew jvmTest      # run tests
./gradlew check        # full check
```

## K2 KAPT internals

Hard-won knowledge about `kotlin-annotation-processing-embeddable` for K2 — not documented publicly.

### Override point (version-dependent)
- **2.3.20**: override `protected open fun loadProcessors(): LoadedProcessors`
- **master / future**: override `protected open fun createProcessorLoader(): ProcessorLoader`
- Always verify against the actual JAR (`javap`) before implementing — GitHub master and the released JAR diverge.

### `isApplicable()` requirements
`FirKaptAnalysisHandlerExtension.isApplicable()` checks three things:
1. `configuration[KAPT_OPTIONS] != null`
2. `configuration.getBoolean(USE_FIR)` — automatically `true` when `languageVersion >= 2.0`
3. `!configuration.skipBodies`

The `skipBodies` check is the built-in re-entrancy guard: `contextForStubGeneration()` copies the config with `skipBodies = true` and runs an inner K2 compilation pipeline to generate stubs. Without the guard, this triggers `isApplicable()` → `doAnalysis()` again → infinite recursion. `configuration.copy()` is **shallow** (shared extension storage), so the `skipBodies` flag is the only thing preventing the loop.

### `processingClasspath` must be non-empty
`checkOptions()` returns early (skips processing silently) when `processingClasspath` is empty. Since fluid-compiler injects `Processor` instances directly (bypassing classpath scanning), a sentinel entry is added to satisfy the check. The actual content is irrelevant because `loadProcessors()` is overridden.

### Required output dirs
`sourcesOutputDir`, `classesOutputDir`, and `stubsOutputDir` must all be set on the `KaptOptions.Builder` or `checkOptions()` warns and skips.

### `AptMode.WITH_COMPILATION` is forbidden in K2
K2 KAPT explicitly rejects this mode with an error. Translate it to `AptMode.STUBS_AND_APT` before passing to `super.doAnalysis()`. `APT_ONLY` is not a viable alternative — it skips stub generation and requires pre-existing stubs.

### `--add-opens` required
The inner stub-generation pipeline runs javac internally and requires access to `jdk.compiler` internals. Without `--add-opens=jdk.compiler/com.sun.tools.javac.*=ALL-UNNAMED` (and related packages), the inner pipeline fails with `INTERNAL_ERROR` or `IllegalAccessException`. See `build.gradle.kts` for the full list.

### `KAPT_OPTIONS` key
Defined as `val KAPT_OPTIONS = CompilerConfigurationKey.create<KaptOptions.Builder>("KAPT_OPTIONS")` in `org.jetbrains.kotlin.kapt` — public, importable directly.
