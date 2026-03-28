package io.fluidsonic.compiler

import org.jetbrains.kotlin.kapt.base.*
import javax.annotation.processing.*


internal class KaptConfiguration(
	val options: KaptOptions,
	val processors: Collection<Processor>
)
