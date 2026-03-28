package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.messages.*
import kotlin.test.*


class CompilationMessageTest {

	@Test
	fun toString_withoutLocation_formatsSeverityAndMessage() {
		val message = CompilationMessage(
			location = null,
			message = "something went wrong",
			severity = CompilerMessageSeverity.ERROR,
		)

		assertEquals(
			actual = message.toString(),
			expected = "error: something went wrong",
		)
	}


	@Test
	fun toString_withLocation_includesLocationInBrackets() {
		val location = CompilerMessageLocation.create("Hello.kt", 10, 5, null)
		val message = CompilationMessage(
			location = location,
			message = "unused variable",
			severity = CompilerMessageSeverity.WARNING,
		)

		val result = message.toString()
		assertTrue(result.startsWith("warning"), "Expected to start with 'warning', got: $result")
		assertTrue(result.contains("[at "), "Expected to contain '[at ', got: $result")
		assertTrue(result.contains("]: unused variable"), "Expected to contain ']: unused variable', got: $result")
	}


	@Test
	fun equality_worksForDataClass() {
		val message1 = CompilationMessage(
			location = null,
			message = "test message",
			severity = CompilerMessageSeverity.INFO,
		)
		val message2 = CompilationMessage(
			location = null,
			message = "test message",
			severity = CompilerMessageSeverity.INFO,
		)

		assertEquals(actual = message1, expected = message2)
		assertEquals(actual = message1.hashCode(), expected = message2.hashCode())
	}


	@Test
	fun inequality_whenDifferentSeverity() {
		val message1 = CompilationMessage(
			location = null,
			message = "test",
			severity = CompilerMessageSeverity.ERROR,
		)
		val message2 = CompilationMessage(
			location = null,
			message = "test",
			severity = CompilerMessageSeverity.WARNING,
		)

		assertNotEquals(actual = message1, illegal = message2)
	}
}
