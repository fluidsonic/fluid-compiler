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
