package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.*


public data class CompilationResult(
	val exitCode: ExitCode,
	val generatedFiles: Collection<GeneratedFile>,
	val messages: List<CompilationMessage>
)
