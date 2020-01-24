import io.fluidsonic.gradle.*
import org.jetbrains.kotlin.gradle.plugin.*

plugins {
	id("io.fluidsonic.gradle") version "1.0.6"
}

fluidJvmLibrary(name = "compiler", version = "0.9.7")

fluidJvmLibraryVariant(JvmTarget.jdk8) {
	description = "Compile Kotlin code and run Kapt annotation processing directly from Kotlin"
}

dependencies {
	api(fluid("stdlib", "0.9.29")) {
		attributes {
			attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
			attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, Usage.JAVA_RUNTIME))
			attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 8)
		}
	}

	api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.3.61")
	api("org.jetbrains.kotlin:kotlin-annotation-processing-embeddable:1.3.61")
}
