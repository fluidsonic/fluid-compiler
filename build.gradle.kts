import com.github.fluidsonic.fluid.library.*

plugins {
	id("com.github.fluidsonic.fluid-library") version "0.9.2"
}

fluidLibrary {
	name = "fluid-compiler"
	version = "0.9.0"
}

fluidLibraryVariant {
	description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin"
	jdk = JDK.v1_8
}

dependencies {
	api(fluid("stdlib-jdk8", "0.9.1"))

	api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.21")
	api("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.3.21")
}
