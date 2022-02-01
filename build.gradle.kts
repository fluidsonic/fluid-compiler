import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.25"
}

fluidLibrary(name = "compiler", version = "0.11.0")

fluidLibraryModule(description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin") {
	targets {
		jvm {
			dependencies {
				api(kotlin("compiler-embeddable"))
				api(kotlin("annotation-processing-embeddable"))
			}
		}
	}
}
