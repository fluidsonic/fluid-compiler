package com.github.fluidsonic.fluid.compiler

import org.jetbrains.kotlin.base.kapt3.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.kapt3.*
import org.jetbrains.kotlin.kapt3.base.*
import org.jetbrains.kotlin.kapt3.base.incremental.*
import org.jetbrains.kotlin.kapt3.util.*


internal class KaptExtension(
	compilerConfiguration: CompilerConfiguration,
	logger: MessageCollectorBackedKaptLogger,
	options: KaptOptions,
	processors: Collection<IncrementalProcessor>
) : AbstractKapt3Extension(
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
