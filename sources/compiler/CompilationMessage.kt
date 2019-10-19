package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.messages.*


data class CompilationMessage(
	val location: CompilerMessageLocation?,
	val message: String,
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
