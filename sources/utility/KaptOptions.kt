package com.github.fluidsonic.fluid.compiler

import org.jetbrains.kotlin.base.kapt3.*


fun KaptOptions.toBuilder(): KaptOptions.Builder = let { options ->
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

		for (flag in KaptFlag.values())
			if (options[flag])
				flags += flag
			else
				flags -= flag
	}
}
