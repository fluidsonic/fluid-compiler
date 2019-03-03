package com.github.fluidsonic.fluid.compiler

import com.github.fluidsonic.fluid.stdlib.*
import org.jetbrains.kotlin.base.kapt3.KaptOptions
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.VERBOSE
import org.jetbrains.kotlin.cli.common.messages.FilteringMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.com.intellij.openapi.application.PathManager
import org.jetbrains.kotlin.config.Services
import org.jetbrains.kotlin.incremental.destinationAsFile
import java.io.File
import javax.annotation.processing.Processor


class KotlinCompiler {

	@PublishedApi
	internal val arguments = K2JVMCompilerArguments()

	private var includesCurrentClasspath = false

	@PublishedApi
	internal val kaptOptions = KaptOptions.Builder()

	@PublishedApi
	internal var kaptOptionsModified = false

	internal val processors = mutableListOf<Processor>()


	fun compile(): CompilationResult {
		val usesKapt = processors.isNotEmpty()

		val temporaryOutputDirectory = arguments.destination.isNullOrEmpty().thenTake {
			createTempDir().also { arguments.destinationAsFile = it }
		}
		val temporaryGeneratedSourcesDirectory = (usesKapt && kaptOptions.sourcesOutputDir == null).thenTake {
			createTempDir().also { kaptOptions.sourcesOutputDir = it }
		}
		val temporaryGeneratedClassesDirectory = (usesKapt && kaptOptions.classesOutputDir == null).thenTake {
			createTempDir().also { kaptOptions.classesOutputDir = it }
		}
		val temporaryGeneratedStubsDirectory = (usesKapt && kaptOptions.stubsOutputDir == null).thenTake {
			createTempDir().also { kaptOptions.stubsOutputDir = it }
		}

		// TODO lots of backup here unless we make K2JVMCompilerArguments copyable - but then we have to update the copy method with every compiler update…
		val initialClasspath = arguments.classpath
		val initialNoStdlib = arguments.noStdlib
		val initialPluginClasspaths = arguments.pluginClasspaths

		try {
			arguments.pluginClasspaths = (arguments.pluginClasspaths.orEmpty()
				.filter { it != servicesPath } + servicesPath).toTypedArray()

			if (includesCurrentClasspath) {
				arguments.classpath = arguments.classpath
					?.split(':')
					?.toSet()
					.orEmpty()
					.let { it + currentClasspath }
					.joinToString(":")

				if (arguments.kotlinHome.isNullOrEmpty())
					arguments.noStdlib = true
			}

			val messageCollector = InMemoryMessageCollector()
			val kaptConfiguration = usesKapt.thenTake {
				KaptConfiguration(
					options = try {
						kaptOptions.build()
					}
					catch (e: Exception) {
						throw IllegalStateException("Kapt configured incorrectly: ${e.message}", e)
					},
					processors = processors.toList()
				)
			}

			val exitCode = withKaptConfiguration(kaptConfiguration) {
				K2JVMCompiler().exec(
					messageCollector = FilteringMessageCollector(messageCollector, VERBOSE::contains),
					services = Services.EMPTY,
					arguments = arguments
				)
			}

			val generatedFiles = temporaryGeneratedSourcesDirectory?.walkTopDown()
				?.filter { it.isFile }
				?.map { file ->
					GeneratedFile(
						content = file.readText(),
						path = file.relativeTo(temporaryGeneratedSourcesDirectory)
					)
				}
				?.toList()
				.orEmpty()

			return CompilationResult(
				exitCode = exitCode,
				generatedFiles = generatedFiles,
				messages = messageCollector.messages
			)
		}
		finally {
			arguments.classpath = initialClasspath
			arguments.noStdlib = initialNoStdlib
			arguments.pluginClasspaths = initialPluginClasspaths

			try {
				if (temporaryOutputDirectory != null) {
					temporaryOutputDirectory.deleteRecursively()
					arguments.destination = null
				}
				if (temporaryGeneratedSourcesDirectory != null) {
					temporaryGeneratedSourcesDirectory.deleteRecursively()
					kaptOptions.sourcesOutputDir = null
				}
				if (temporaryGeneratedClassesDirectory != null) {
					temporaryGeneratedClassesDirectory.deleteRecursively()
					kaptOptions.classesOutputDir = null
				}
				if (temporaryGeneratedStubsDirectory != null) {
					temporaryGeneratedStubsDirectory.deleteRecursively()
					kaptOptions.stubsOutputDir = null
				}
			}
			catch (e: Exception) {
				println("Failed deleting temporary directory: $e")
			}
		}
	}


	inline fun arguments(block: K2JVMCompilerArguments.() -> Unit): KotlinCompiler = apply {
		arguments.block()
	}


	fun destinationDirectory(destinationDirectory: File): KotlinCompiler = apply {
		arguments.destinationAsFile = destinationDirectory
	}


	fun includesCurrentClasspath(includesCurrentClasspath: Boolean = true): KotlinCompiler = apply {
		this.includesCurrentClasspath = includesCurrentClasspath
	}


	fun jvmTarget(jvmTarget: KotlinJvmTarget): KotlinCompiler = apply {
		arguments.jvmTarget = jvmTarget.string
	}


	inline fun kaptOptions(block: KaptOptions.Builder.() -> Unit): KotlinCompiler = apply {
		kaptOptionsModified = true
		kaptOptions.block()
	}


	fun kotlinHome(kotlinHome: File): KotlinCompiler = apply {
		arguments.kotlinHome = kotlinHome.canonicalPath
	}


	fun moduleName(moduleName: String): KotlinCompiler = apply {
		arguments.moduleName = moduleName
	}


	fun processors(vararg processors: Processor): KotlinCompiler =
		processors(processors.toList())


	fun processors(processors: Iterable<Processor>): KotlinCompiler = apply {
		this.processors += processors
	}


	fun sourceFiles(vararg sourceFiles: File): KotlinCompiler =
		sourceFiles(sourceFiles.toList())


	fun sourceFiles(sourceFiles: Iterable<File>): KotlinCompiler = apply {
		arguments.freeArgs += sourceFiles.map { it.canonicalPath }
	}


	companion object {

		private val currentClasspath = findAllClasspathEntries().filter(File::exists).toSet()


		private val servicesPath = KotlinCompiler::class.java.let { clazz ->
			PathManager.getResourceRoot(clazz, "/" + clazz.name.replace('.', '/') + ".class")
				?.let { File(it).absoluteFile }
				?.let { file ->
					if (file.isFile) file // in JAR
					else file.parentFile.resolve("resources") // run from IntelliJ IDEA
				}
				?.let { it.absolutePath }
		} ?: "resources" // fall back to working directory = project path
	}
}
