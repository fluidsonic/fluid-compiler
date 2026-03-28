package io.fluidsonic.compiler

import org.jetbrains.kotlin.kapt.base.*


/** Creates a mutable [KaptOptions.Builder] with all values copied from this [KaptOptions] instance. */
@Deprecated("KAPT is deprecated. Migrate to KSP.", level = DeprecationLevel.WARNING)
public fun KaptOptions.toBuilder(): KaptOptions.Builder = let { options ->
	KaptOptions.Builder().apply {
		classesOutputDir = options.classesOutputDir
		compileClasspath += options.compileClasspath
		detectMemoryLeaks = options.detectMemoryLeaks
		incrementalDataOutputDir = options.incrementalDataOutputDir
		javaSourceRoots += options.javaSourceRoots
		javacOptions += options.javacOptions
		mode = options.mode
		processingClasspath += options.processingClasspath
		processingOptions += options.processingOptions
		processors += options.processors
		projectBaseDir = options.projectBaseDir
		sourcesOutputDir = options.sourcesOutputDir
		stubsOutputDir = options.stubsOutputDir

		for (flag in KaptFlag.entries)
			if (options[flag])
				flags += flag
			else
				flags -= flag
	}
}
