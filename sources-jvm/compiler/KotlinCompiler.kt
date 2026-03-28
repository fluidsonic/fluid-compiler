package io.fluidsonic.compiler

import org.jetbrains.kotlin.kapt.base.*
import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.*
import org.jetbrains.kotlin.com.intellij.ide.highlighter.*
import org.jetbrains.kotlin.com.intellij.openapi.application.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.*
import java.io.*
import javax.annotation.processing.*
import kotlin.io.path.*


/** Compiles Kotlin code programmatically using the Kotlin compiler embeddable. */
public class KotlinCompiler {

	@PublishedApi
	internal val arguments: K2JVMCompilerArguments = K2JVMCompilerArguments().apply {
		compileJava = true
		useJavac = true
	}

	private var includesCurrentClasspath = false

	@PublishedApi
	internal val kaptOptions: KaptOptions.Builder = KaptOptions.Builder()

	@PublishedApi
	internal var kaptOptionsModified: Boolean = false

	internal val processors = mutableListOf<Processor>()


	/** Compiles the configured sources and returns the compilation result. */
	public fun compile(): CompilationResult {
		// TODO lots of backup here unless we make K2JVMCompilerArguments copyable - but then we have to update the copy method with every compiler update…
		val initialClasspath = arguments.classpath
		val initialNoStdlib = arguments.noStdlib
		val initialPluginClasspaths = arguments.pluginClasspaths
		val initialFreeArgs = arguments.freeArgs

		val usesKapt = processors.isNotEmpty()
		val needsDummyKotlinFile = arguments.buildFile == null && !arguments.script && hasOnlyJavaSources(arguments.freeArgs)

		val temporaryOutputDirectory = if (arguments.destination.isNullOrEmpty())
			createTempDirectory().also { arguments.destination = it.toString() }
		else null
		val temporaryGeneratedSourcesDirectory = if (usesKapt && kaptOptions.sourcesOutputDir == null)
			createTempDirectory().also { kaptOptions.sourcesOutputDir = it.toFile() }
		else null
		val temporaryGeneratedClassesDirectory = if (usesKapt && kaptOptions.classesOutputDir == null)
			createTempDirectory().also { kaptOptions.classesOutputDir = it.toFile() }
		else null
		val temporaryGeneratedStubsDirectory = if (usesKapt && kaptOptions.stubsOutputDir == null)
			createTempDirectory().also { kaptOptions.stubsOutputDir = it.toFile() }
		else null
		val dummyKotlinFile = if (needsDummyKotlinFile)
			createTempFile(suffix = ".kt").normalize().also { arguments.freeArgs += it.toString() }
		else null

		try {
			if (usesKapt)
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
			val kaptConfiguration = if (usesKapt)
				KaptConfiguration(
					options = try {
						kaptOptions.build()
					}
					catch (e: Exception) {
						throw IllegalStateException("Kapt configured incorrectly: ${e.message}", e)
					},
					processors = processors
				)
			else null

			val exitCode = withKaptConfiguration(kaptConfiguration) {
				K2JVMCompiler().exec(
					messageCollector = FilteringMessageCollector(messageCollector, CompilerMessageSeverity.VERBOSE::contains),
					services = Services.EMPTY,
					arguments = arguments
				)
			}

			val generatedFiles = temporaryGeneratedSourcesDirectory?.toFile()?.walkTopDown()
				?.filter { it.isFile }
				?.map { file ->
					GeneratedFile(
						content = file.readText(),
						path = file.relativeTo(temporaryGeneratedSourcesDirectory.toFile())
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
					temporaryOutputDirectory.toFile().deleteRecursively()
					arguments.destination = null
				}
				if (temporaryGeneratedSourcesDirectory != null) {
					temporaryGeneratedSourcesDirectory.toFile().deleteRecursively()
					kaptOptions.sourcesOutputDir = null
				}
				if (temporaryGeneratedClassesDirectory != null) {
					temporaryGeneratedClassesDirectory.toFile().deleteRecursively()
					kaptOptions.classesOutputDir = null
				}
				if (temporaryGeneratedStubsDirectory != null) {
					temporaryGeneratedStubsDirectory.toFile().deleteRecursively()
					kaptOptions.stubsOutputDir = null
				}

				dummyKotlinFile?.deleteIfExists()
			}
			catch (e: Exception) {
				println("Failed deleting temporary file or directory: $e")
			}
		}
	}


	/** Configures the underlying [K2JVMCompilerArguments] directly. */
	public inline fun arguments(block: K2JVMCompilerArguments.() -> Unit): KotlinCompiler = apply {
		arguments.block()
	}


	/** Sets the output directory for compiled classes. */
	public fun destination(destination: File): KotlinCompiler = apply {
		arguments.destination = destination.canonicalPath
	}


	/** Sets the output directory for compiled classes. */
	public fun destination(destination: String): KotlinCompiler =
		destination(File(destination))


	/** Includes the current process classpath in the compilation classpath. */
	public fun includesCurrentClasspath(includesCurrentClasspath: Boolean = true): KotlinCompiler = apply {
		this.includesCurrentClasspath = includesCurrentClasspath
	}


	/** Sets the JVM bytecode target version. */
	public fun jvmTarget(jvmTarget: KotlinJvmTarget): KotlinCompiler = apply {
		arguments.jvmTarget = jvmTarget.string
	}


	/** Configures KAPT annotation processing options. */
	@Deprecated("KAPT is deprecated. Migrate to KSP.", level = DeprecationLevel.WARNING)
	public inline fun kaptOptions(block: KaptOptions.Builder.() -> Unit): KotlinCompiler = apply {
		kaptOptionsModified = true
		kaptOptions.block()
	}


	/** Sets the Kotlin home directory for the compiler. */
	public fun kotlinHome(kotlinHome: File): KotlinCompiler = apply {
		arguments.kotlinHome = kotlinHome.canonicalPath
	}


	/** Sets the Kotlin home directory for the compiler. */
	public fun kotlinHome(kotlinHome: String): KotlinCompiler =
		kotlinHome(File(kotlinHome))


	/** Sets the module name for the compilation. */
	public fun moduleName(moduleName: String): KotlinCompiler = apply {
		arguments.moduleName = moduleName
	}


	/** Adds annotation processors for KAPT. */
	@Deprecated("KAPT is deprecated. Migrate to KSP.", level = DeprecationLevel.WARNING)
	public fun processors(vararg processors: Processor): KotlinCompiler =
		@Suppress("DEPRECATION") processors(processors.toList())


	/** Adds annotation processors for KAPT. */
	@Deprecated("KAPT is deprecated. Migrate to KSP.", level = DeprecationLevel.WARNING)
	public fun processors(processors: Iterable<Processor>): KotlinCompiler = apply {
		this.processors += processors
	}


	/** Adds source files to compile. */
	public fun sources(vararg sources: File): KotlinCompiler =
		sources(sources.toList())


	/** Adds source files to compile. */
	public fun sources(sourceFiles: Iterable<File>): KotlinCompiler = apply {
		arguments.freeArgs += sourceFiles.map { it.canonicalPath }
	}


	/** Adds source file paths to compile. */
	public fun sources(vararg sources: String): KotlinCompiler =
		sources(sources.map(::File))


	/** Adds source file paths to compile. */
	@JvmName("sourcesAsString")
	public fun sources(sources: Iterable<String>): KotlinCompiler =
		sources(sources.map(::File))


	public companion object {

		private val currentClasspath = findAllClasspathEntries().filter(File::exists).toSet()


		private val servicesPath = KotlinCompiler::class.java.let { clazz ->
			PathManager.getResourceRoot(clazz, "/" + clazz.name.replace('.', '/') + ".class")
				?.let { File(it).absoluteFile }
				?.let { file ->
					if (file.isFile) file // in JAR
					else resolveResourcesDirectory(file)
				}?.canonicalPath
		} ?: File("resources").canonicalPath // fall back to working directory = project path


		private fun resolveResourcesDirectory(classOutputDir: File): File {
			// IntelliJ IDEA: resources are a sibling of the classes directory
			val ideaResources = classOutputDir.parentFile.resolve("resources")
			if (ideaResources.isDirectory) return ideaResources

			// Kotlin Multiplatform: classes at build/classes/kotlin/<target>/<sourceSet>,
			// resources at build/processedResources/<target>/<sourceSet>
			val sourceSet = classOutputDir.name // e.g. "main"
			val target = classOutputDir.parentFile?.name // e.g. "jvm"
			if (target != null) {
				val buildDir = classOutputDir.parentFile?.parentFile?.parentFile?.parentFile // build/
				if (buildDir != null) {
					val kmpResources = buildDir.resolve("processedResources").resolve(target).resolve(sourceSet)
					if (kmpResources.isDirectory) return kmpResources
				}
			}

			return ideaResources // fall back to the original behavior
		}
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
