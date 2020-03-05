package io.fluidsonic.compiler

import org.jetbrains.kotlin.base.kapt3.*
import javax.annotation.processing.*


internal class KaptConfiguration(
	val options: KaptOptions,
	val processors: Collection<Processor>
)
