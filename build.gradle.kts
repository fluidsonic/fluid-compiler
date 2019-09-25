import com.github.fluidsonic.fluid.library.*

plugins {
	id("com.github.fluidsonic.fluid-library") version "0.9.24"
}

fluidJvmLibrary {
	name = "fluid-compiler"
	version = "0.9.4"
}

fluidJvmLibraryVariant {
	description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin"
	jdk = JvmTarget.jdk8
}

dependencies {
	api(fluid("stdlib", "0.9.25"))

	api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.50")
	api("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.3.50")
}
