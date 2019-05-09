import com.github.fluidsonic.fluid.library.*

plugins {
	id("com.github.fluidsonic.fluid-library") version "0.9.8"
}

fluidJvmLibrary {
	name = "fluid-compiler"
	version = "0.9.4"
}

fluidJvmLibraryVariant {
	description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin"
	jdk = JDK.v1_8
}

dependencies {
	api(fluid("stdlib", "0.9.4"))

	api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.31")
	api("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.3.31")
}
