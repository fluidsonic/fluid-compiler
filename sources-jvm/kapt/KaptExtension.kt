package io.fluidsonic.compiler

import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.kapt.*
import org.jetbrains.kotlin.kapt.base.*
import org.jetbrains.kotlin.kapt.base.incremental.*
import org.jetbrains.kotlin.kapt.util.*


internal class KaptExtension(
	compilerConfiguration: CompilerConfiguration,
	logger: MessageCollectorBackedKaptLogger,
	options: KaptOptions,
	processors: Collection<IncrementalProcessor>
) : AbstractKaptExtension(
	compilerConfiguration = compilerConfiguration,
	logger = logger,
	options = options
) {

	private val processors = LoadedProcessors(
		processors = processors.toList(),
		classLoader = this::class.java.classLoader
	)


	override fun loadProcessors() =
		processors
}
