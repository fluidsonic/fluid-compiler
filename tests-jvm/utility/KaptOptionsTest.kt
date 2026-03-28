package io.fluidsonic.compiler

import java.io.*
import org.jetbrains.kotlin.kapt.base.*
import kotlin.test.*


@Suppress("DEPRECATION")
class KaptOptionsTest {

	@Test
	fun toBuilder_roundTripsScalarFields() {
		val projectBase = File("/tmp/project")
		val sourcesOut = File("/tmp/sources")
		val classesOut = File("/tmp/classes")
		val stubsOut = File("/tmp/stubs")
		val incrementalOut = File("/tmp/incremental")

		val original = KaptOptions.Builder().apply {
			projectBaseDir = projectBase
			sourcesOutputDir = sourcesOut
			classesOutputDir = classesOut
			stubsOutputDir = stubsOut
			incrementalDataOutputDir = incrementalOut
			mode = AptMode.STUBS_AND_APT
			detectMemoryLeaks = DetectMemoryLeaksMode.PARANOID
		}.build()

		val rebuilt = original.toBuilder().build()

		assertEquals(actual = rebuilt.projectBaseDir, expected = projectBase)
		assertEquals(actual = rebuilt.sourcesOutputDir, expected = sourcesOut)
		assertEquals(actual = rebuilt.classesOutputDir, expected = classesOut)
		assertEquals(actual = rebuilt.stubsOutputDir, expected = stubsOut)
		assertEquals(actual = rebuilt.incrementalDataOutputDir, expected = incrementalOut)
		assertEquals(actual = rebuilt.mode, expected = AptMode.STUBS_AND_APT)
		assertEquals(actual = rebuilt.detectMemoryLeaks, expected = DetectMemoryLeaksMode.PARANOID)
	}


	@Test
	fun toBuilder_roundTripsCollectionFields() {
		val compileEntry = File("/tmp/compile.jar")
		val javaRoot = File("/tmp/java")
		val processingEntry = File("/tmp/processor.jar")

		val original = KaptOptions.Builder().apply {
			projectBaseDir = File("/tmp/project")
			sourcesOutputDir = File("/tmp/sources")
			classesOutputDir = File("/tmp/classes")
			stubsOutputDir = File("/tmp/stubs")
			compileClasspath += compileEntry
			javaSourceRoots += javaRoot
			processingClasspath += processingEntry
			processors += "com.example.MyProcessor"
			processingOptions["key1"] = "value1"
			javacOptions["-source"] = "11"
		}.build()

		val rebuilt = original.toBuilder().build()

		assertEquals(actual = rebuilt.compileClasspath, expected = listOf(compileEntry))
		assertEquals(actual = rebuilt.javaSourceRoots, expected = listOf(javaRoot))
		assertEquals(actual = rebuilt.processingClasspath, expected = listOf(processingEntry))
		assertEquals(actual = rebuilt.processors, expected = listOf("com.example.MyProcessor"))
		assertEquals(actual = rebuilt.processingOptions, expected = mapOf("key1" to "value1"))
		assertEquals(actual = rebuilt.javacOptions, expected = mapOf("-source" to "11"))
	}


	@Test
	fun toBuilder_roundTripsFlags() {
		val original = KaptOptions.Builder().apply {
			projectBaseDir = File("/tmp/project")
			sourcesOutputDir = File("/tmp/sources")
			classesOutputDir = File("/tmp/classes")
			stubsOutputDir = File("/tmp/stubs")
			flags += KaptFlag.VERBOSE
			flags += KaptFlag.CORRECT_ERROR_TYPES
			flags += KaptFlag.STRICT
		}.build()

		val rebuilt = original.toBuilder().build()

		for (flag in KaptFlag.entries) {
			assertEquals(
				actual = rebuilt[flag],
				expected = original[flag],
				message = "Flag $flag mismatch",
			)
		}

		assertTrue(rebuilt[KaptFlag.VERBOSE])
		assertTrue(rebuilt[KaptFlag.CORRECT_ERROR_TYPES])
		assertTrue(rebuilt[KaptFlag.STRICT])
	}


	@Test
	fun toBuilder_roundTripsDefaultValues() {
		val original = KaptOptions.Builder().apply {
			projectBaseDir = File("/tmp/project")
			sourcesOutputDir = File("/tmp/sources")
			classesOutputDir = File("/tmp/classes")
			stubsOutputDir = File("/tmp/stubs")
		}.build()

		val rebuilt = original.toBuilder().build()

		assertEquals(actual = rebuilt.mode, expected = original.mode)
		assertEquals(actual = rebuilt.detectMemoryLeaks, expected = original.detectMemoryLeaks)
		assertTrue(rebuilt.compileClasspath.isEmpty())
		assertTrue(rebuilt.javaSourceRoots.isEmpty())
		assertTrue(rebuilt.processingClasspath.isEmpty())
		assertTrue(rebuilt.processors.isEmpty())
		assertTrue(rebuilt.processingOptions.isEmpty())
		assertTrue(rebuilt.javacOptions.isEmpty())
	}


	@Test
	fun toBuilder_preservesMultipleCollectionEntries() {
		val files = listOf(File("/tmp/a.jar"), File("/tmp/b.jar"), File("/tmp/c.jar"))

		val original = KaptOptions.Builder().apply {
			projectBaseDir = File("/tmp/project")
			sourcesOutputDir = File("/tmp/sources")
			classesOutputDir = File("/tmp/classes")
			stubsOutputDir = File("/tmp/stubs")
			compileClasspath += files
			processors += listOf("proc.A", "proc.B")
		}.build()

		val rebuilt = original.toBuilder().build()

		assertEquals(actual = rebuilt.compileClasspath, expected = files)
		assertEquals(actual = rebuilt.processors, expected = listOf("proc.A", "proc.B"))
	}
}
