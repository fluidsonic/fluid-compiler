import io.fluidsonic.gradle.*

plugins {
	id("io.fluidsonic.gradle") version "3.0.0"
}

fluidLibrary(name = "compiler", version = "0.15.0")

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

// K2 KAPT stub generation uses deep recursion and requires access to internal
// javac APIs; increase stack size and open all required jdk.compiler packages.
tasks.withType<Test>().configureEach {
	jvmArgs(
		"-Xss8m",
		"-Xmx2g",
		"--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED",
		"--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
	)
}
