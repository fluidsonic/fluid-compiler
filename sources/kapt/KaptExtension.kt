package com.github.fluidsonic.fluid.compiler

import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.kapt3.AbstractKapt3Extension
import org.jetbrains.kotlin.kapt3.base.LoadedProcessors
import org.jetbrains.kotlin.kapt3.util.MessageCollectorBackedKaptLogger
import javax.annotation.processing.Processor


internal class KaptExtension(
	compilerConfiguration: CompilerConfiguration,
	logger: MessageCollectorBackedKaptLogger,
	options: KaptOptions,
	processors: Collection<Processor>
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
