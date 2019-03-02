package com.github.fluidsonic.fluid.compiler

import org.jetbrains.kotlin.base.kapt3.KaptOptions
import javax.annotation.processing.Processor


internal class KaptConfiguration(
	val options: KaptOptions,
	val processors: Collection<Processor>
)
