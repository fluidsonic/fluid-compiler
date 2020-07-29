import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.2"
}

fluidLibrary(name = "compiler", version = "0.10.1")

fluidLibraryModule(description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin") {
	publishSingleTargetAsModule()

	targets {
		jvm {
			dependencies {
				api(fluid("stdlib", "0.10.0"))
				api(kotlin("compiler-embeddable"))
				api(kotlin("annotation-processing-embeddable"))
			}
		}
	}
}
