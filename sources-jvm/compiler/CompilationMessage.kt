package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.messages.*


/** Represents a message emitted during Kotlin compilation. */
public data class CompilationMessage(
	/** The source location where the message originated, or `null` if not available. */
	val location: CompilerMessageSourceLocation?,
	/** The message text. */
	val message: String,
	/** The severity level of the message. */
	val severity: CompilerMessageSeverity
) {

	override fun toString(): String = buildString {
		append(severity.presentableName)

		if (location != null) {
			append(" [at ")
			append(location)
			append("]")
		}

		append(": ")
		append(message)
	}
}
