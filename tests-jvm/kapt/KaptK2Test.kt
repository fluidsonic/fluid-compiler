package io.fluidsonic.compiler

import java.io.*
import java.nio.file.*
import javax.annotation.processing.*
import javax.lang.model.*
import javax.lang.model.element.*
import kotlin.test.*
import org.jetbrains.kotlin.cli.common.*


private class TrackingProcessor : AbstractProcessor() {

	var wasProcessCalled = false


	override fun getSupportedAnnotationTypes() = setOf("*")


	override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()


	override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
		wasProcessCalled = true
		return false
	}
}


@Suppress("DEPRECATION")
class KaptK2Test {

	private var tempDir: Path? = null


	@BeforeTest
	fun setUp() {
		tempDir = Files.createTempDirectory("kapt-k2-test")
	}


	@AfterTest
	fun tearDown() {
		tempDir?.toFile()?.deleteRecursively()
	}


	private fun newCompiler(): KotlinCompiler = KotlinCompiler()
		.includesCurrentClasspath()
		.arguments {
			compileJava = false
			useJavac = false
			pluginClasspaths = pluginClasspaths?.filter { File(it).exists() }?.toTypedArray()
			languageVersion = "2.0"
		}


	@Test
	fun k2_kapt_basicProcessorIsCalled() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		val processor = TrackingProcessor()

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.processors(processor)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(processor.wasProcessCalled, "Processor.process() should have been called in K2 mode")
	}


	@Test
	fun k2_kapt_multipleProcessorsAreCalled() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		val processor1 = TrackingProcessor()
		val processor2 = TrackingProcessor()

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.processors(processor1, processor2)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(processor1.wasProcessCalled, "First processor should have been called in K2 mode")
		assertTrue(processor2.wasProcessCalled, "Second processor should have been called in K2 mode")
	}


	@Test
	fun k2_kapt_withCompilationMode_doesNotError() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		val processor = TrackingProcessor()

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.processors(processor)
			.kaptOptions {
				mode = org.jetbrains.kotlin.kapt.base.AptMode.WITH_COMPILATION
			}
			.compile()

		// Should succeed and not error out even though WITH_COMPILATION is translated to STUBS_AND_APT
		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun k2_kapt_kotlinGeneratedOptionIsSet() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		var kotlinGeneratedPath: String? = null
		val processor = object : AbstractProcessor() {
			override fun getSupportedAnnotationTypes() = setOf("*")
			override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()
			override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
				kotlinGeneratedPath = processingEnv.options["kapt.kotlin.generated"]
				return false
			}
		}

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.processors(processor)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertNotNull(kotlinGeneratedPath, "kapt.kotlin.generated processing option must be set")
	}


	@Test
	fun k2_kapt_worksWithoutExplicitLanguageVersion() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		val processor = TrackingProcessor()

		// Default language version (2.0 in Kotlin 2.x): should use the K2 KAPT path.
		val result = KotlinCompiler()
			.includesCurrentClasspath()
			.arguments {
				compileJava = false
				useJavac = false
				pluginClasspaths = pluginClasspaths?.filter { File(it).exists() }?.toTypedArray()
			}
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.processors(processor)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(processor.wasProcessCalled, "Processor.process() should have been called")
	}
}
