package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.*
import java.io.*
import java.nio.file.*
import kotlin.test.*


class KotlinCompilerTest {

	private var tempDir: Path? = null


	@BeforeTest
	fun setUp() {
		tempDir = Files.createTempDirectory("kotlin-compiler-test")
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
		}


	@Test
	fun compile_succeeds_withValidKotlinSource() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello, World!\"")

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(tempDir!!.resolve("output").toFile())
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(result.messages.none { it.severity.isError })
	}


	@Test
	fun compile_fails_withInvalidKotlinSource() {
		val sourceFile = tempDir!!.resolve("Invalid.kt").toFile()
		sourceFile.writeText("fun hello(): String = ")

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(tempDir!!.resolve("output").toFile())
			.compile()

		assertNotEquals(actual = result.exitCode, illegal = ExitCode.OK)
		assertTrue(result.messages.any { it.severity.isError })
	}


	@Test
	fun compile_succeeds_withMultipleSources() {
		val file1 = tempDir!!.resolve("Foo.kt").toFile()
		file1.writeText("class Foo { fun greet(): String = \"foo\" }")

		val file2 = tempDir!!.resolve("Bar.kt").toFile()
		file2.writeText("class Bar { fun greet(): String = Foo().greet() }")

		val result = newCompiler()
			.sources(file1, file2)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(tempDir!!.resolve("output").toFile())
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun builderMethods_returnSameInstance() {
		val compiler = KotlinCompiler()

		assertSame(actual = compiler.jvmTarget(KotlinJvmTarget.v21), expected = compiler)
		assertSame(actual = compiler.destination(tempDir!!.resolve("output").toFile()), expected = compiler)
		assertSame(actual = compiler.includesCurrentClasspath(), expected = compiler)
		assertSame(actual = compiler.moduleName("test-module"), expected = compiler)
		assertSame(actual = compiler.sources(tempDir!!.resolve("Foo.kt").toFile()), expected = compiler)
	}


	@Test
	fun includesCurrentClasspath_doesNotBreakCompilation() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello\"")

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.includesCurrentClasspath()
			.destination(tempDir!!.resolve("output").toFile())
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun moduleName_setsModuleName() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello\"")

		val compiler = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.moduleName("my-test-module")
			.destination(tempDir!!.resolve("output").toFile())

		assertEquals(actual = compiler.arguments.moduleName, expected = "my-test-module")

		val result = compiler.compile()
		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun compile_producesOutputFiles() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("class Hello")

		val outputDir = tempDir!!.resolve("output").toFile()

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(outputDir)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(outputDir.exists(), "Output directory should exist")
		assertTrue(outputDir.listFiles()?.isNotEmpty() == true, "Output directory should contain compiled files")
	}


	@Test
	fun compile_createsTemporaryOutputDirectory_whenNoneSpecified() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello\"")

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun sources_acceptsStringPaths() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello\"")

		val result = newCompiler()
			.sources(sourceFile.absolutePath)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(tempDir!!.resolve("output").toFile())
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}


	@Test
	fun destination_acceptsStringPath() {
		val sourceFile = tempDir!!.resolve("Hello.kt").toFile()
		sourceFile.writeText("fun hello(): String = \"Hello\"")

		val result = newCompiler()
			.sources(sourceFile)
			.jvmTarget(KotlinJvmTarget.v21)
			.destination(tempDir!!.resolve("output").toString())
			.compile()

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
	}
}
