import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "1.1.0"
}

fluidLibrary(name = "compiler", version = "0.9.10")

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
