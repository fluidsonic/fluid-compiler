package com.github.fluidsonic.fluid.compiler

import com.github.fluidsonic.fluid.stdlib.*
import org.jetbrains.kotlin.base.kapt3.*
import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.*
import org.jetbrains.kotlin.cli.jvm.*
import org.jetbrains.kotlin.com.intellij.ide.highlighter.*
import org.jetbrains.kotlin.com.intellij.openapi.application.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.*
import org.jetbrains.kotlin.kapt3.base.incremental.*
import java.io.*
import javax.annotation.processing.*


class KotlinCompiler {

	@PublishedApi
	internal val arguments = K2JVMCompilerArguments().apply {
		compileJava = true
		useJavac = true
	}

	private var includesCurrentClasspath = false

	@PublishedApi
	internal val kaptOptions = KaptOptions.Builder()

	@PublishedApi
	internal var kaptOptionsModified = false

	internal val processors = mutableListOf<Processor>()


	fun compile(): CompilationResult {
		// TODO lots of backup here unless we make K2JVMCompilerArguments copyable - but then we have to update the copy method with every compiler update…
		val initialClasspath = arguments.classpath
		val initialNoStdlib = arguments.noStdlib
		val initialPluginClasspaths = arguments.pluginClasspaths
		val initialFreeArgs = arguments.freeArgs

		val usesKapt = processors.isNotEmpty()
		val needsDummyKotlinFile = arguments.buildFile == null && !arguments.script && hasOnlyJavaSources(arguments.freeArgs)

		val temporaryOutputDirectory = arguments.destination.isNullOrEmpty().thenTake {
			createTempDir().also { arguments.destination = it.path }
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
		val dummyKotlinFile = needsDummyKotlinFile.thenTake {
			createTempFile(suffix = ".kt").also { arguments.freeArgs += it.canonicalPath }
		}

		try {
			if (!loadToolsJarIfNeeded())
				error("tools.jar is missing in the current classpath and cannot be found in JAVA HOME. Please add it manually to your project.")

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
					processors = processors.map { IncrementalProcessor(processor = it, kind = DeclaredProcType.NON_INCREMENTAL) }
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
			arguments.freeArgs = initialFreeArgs
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

				dummyKotlinFile?.delete()
			}
			catch (e: Exception) {
				println("Failed deleting temporary file or directory: $e")
			}
		}
	}


	inline fun arguments(block: K2JVMCompilerArguments.() -> Unit): KotlinCompiler = apply {
		arguments.block()
	}


	fun destination(destination: File): KotlinCompiler = apply {
		arguments.destination = destination.canonicalPath
	}


	fun destination(destination: String): KotlinCompiler =
		destination(File(destination))


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


	fun kotlinHome(kotlinHome: String): KotlinCompiler =
		kotlinHome(File(kotlinHome))


	fun moduleName(moduleName: String): KotlinCompiler = apply {
		arguments.moduleName = moduleName
	}


	fun processors(vararg processors: Processor): KotlinCompiler =
		processors(processors.toList())


	fun processors(processors: Iterable<Processor>): KotlinCompiler = apply {
		this.processors += processors
	}


	fun sources(vararg sources: File): KotlinCompiler =
		sources(sources.toList())


	fun sources(sourceFiles: Iterable<File>): KotlinCompiler = apply {
		arguments.freeArgs += sourceFiles.map { it.canonicalPath }
	}


	fun sources(vararg sources: String): KotlinCompiler =
		sources(sources.map(::File))


	@JvmName("sourcesAsString")
	fun sources(sources: Iterable<String>): KotlinCompiler =
		sources(sources.map(::File))


	companion object {

		private val currentClasspath = findAllClasspathEntries().filter(File::exists).toSet()


		private val servicesPath = KotlinCompiler::class.java.let { clazz ->
			PathManager.getResourceRoot(clazz, "/" + clazz.name.replace('.', '/') + ".class")
				?.let { File(it).absoluteFile }
				?.let { file ->
					if (file.isFile) file // in JAR
					else file.parentFile.resolve("resources") // run from IntelliJ IDEA
				}
				?.let { it.canonicalPath }
		} ?: File("resources").canonicalPath // fall back to working directory = project path
	}
}


private fun hasOnlyJavaSources(paths: Collection<String>): Boolean {
	var hasJavaSources = false

	for (path in paths)
		for (file in File(path).walkTopDown().filter(File::isFile))
			when (file.extension) {
				JavaFileType.INSTANCE.defaultExtension -> hasJavaSources = true
				KotlinFileType.EXTENSION, "kts" -> return false
			}

	return hasJavaSources
}
