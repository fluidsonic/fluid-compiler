package com.github.fluidsonic.fluid.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity


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
