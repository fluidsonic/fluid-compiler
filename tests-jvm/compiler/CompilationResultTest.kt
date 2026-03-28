package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.*
import kotlin.test.*


class CompilationResultTest {

	@Test
	fun construction_withEmptyResults() {
		val result = CompilationResult(
			exitCode = ExitCode.OK,
			generatedFiles = emptyList(),
			messages = emptyList(),
		)

		assertEquals(actual = result.exitCode, expected = ExitCode.OK)
		assertTrue(result.generatedFiles.isEmpty())
		assertTrue(result.messages.isEmpty())
	}


	@Test
	fun properties_areAccessible() {
		val result = CompilationResult(
			exitCode = ExitCode.COMPILATION_ERROR,
			generatedFiles = emptyList(),
			messages = emptyList(),
		)

		assertEquals(actual = result.exitCode, expected = ExitCode.COMPILATION_ERROR)
	}


	@Test
	fun equality_worksForDataClass() {
		val result1 = CompilationResult(
			exitCode = ExitCode.OK,
			generatedFiles = emptyList(),
			messages = emptyList(),
		)
		val result2 = CompilationResult(
			exitCode = ExitCode.OK,
			generatedFiles = emptyList(),
			messages = emptyList(),
		)

		assertEquals(actual = result1, expected = result2)
	}
}
