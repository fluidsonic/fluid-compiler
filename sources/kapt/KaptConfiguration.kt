package io.fluidsonic.compiler

import org.jetbrains.kotlin.base.kapt3.*
import org.jetbrains.kotlin.kapt3.base.incremental.*


internal class KaptConfiguration(
	val options: KaptOptions,
	val processors: Collection<IncrementalProcessor>
)
