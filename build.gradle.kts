import com.github.fluidsonic.fluid.library.*
import org.jetbrains.kotlin.gradle.plugin.*

plugins {
	id("com.github.fluidsonic.fluid-library") version "0.9.25"
}

fluidJvmLibrary {
	name = "fluid-compiler"
	version = "0.9.5"
}

fluidJvmLibraryVariant {
	description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin"
	jdk = JvmTarget.jdk8
}

dependencies {
	api(fluid("stdlib", "0.9.25")) {
		attributes {
			attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
		}
	}

	api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.50")
	api("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.3.50")
}
