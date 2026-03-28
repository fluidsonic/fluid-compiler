package io.fluidsonic.compiler

import org.jetbrains.kotlin.cli.common.*
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.config.*
import org.jetbrains.kotlin.com.intellij.openapi.project.*
import org.jetbrains.kotlin.compiler.plugin.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.fir.extensions.*
import org.jetbrains.kotlin.kapt.*
import org.jetbrains.kotlin.kapt.base.*
import org.jetbrains.kotlin.kapt.base.incremental.*
import org.jetbrains.kotlin.kapt.util.*


private val kaptConfiguration = ThreadLocal<KaptConfiguration?>()

// Guard against re-entrant invocation from FirKaptAnalysisHandlerExtension.contextForStubGeneration,
// which re-runs the K2 pipeline internally to generate stubs and would otherwise trigger a
// second (infinite) round of KAPT processing.
private val kaptAnalysisInProgress = ThreadLocal<Boolean>()


internal fun currentKaptConfiguration(): KaptConfiguration? = kaptConfiguration.get()


internal inline fun <R> withKaptConfiguration(configuration: KaptConfiguration?, block: () -> R): R {
	configuration ?: return block()

	val previousConfiguration = kaptConfiguration.get()
	kaptConfiguration.set(configuration)

	try {
		return block()
	}
	finally {
		kaptConfiguration.set(previousConfiguration)
	}
}


@OptIn(ExperimentalCompilerApi::class)
internal class KaptCompilerPluginRegistrar : CompilerPluginRegistrar() {

	override val pluginId: String = "io.fluidsonic.compiler.kapt"
	override val supportsK2: Boolean = true

	override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
		FirAnalysisHandlerExtension.registerExtension(FluidFirKaptExtension())
	}
}


internal class FluidFirKaptExtension : FirKaptAnalysisHandlerExtension() {

	override fun isApplicable(configuration: CompilerConfiguration): Boolean =
		currentKaptConfiguration() != null && kaptAnalysisInProgress.get() != true

	override fun doAnalysis(project: Project, configuration: CompilerConfiguration): Boolean {
		val kaptConfig = currentKaptConfiguration() ?: return true
		if (kaptAnalysisInProgress.get() == true) return true

		val contentRoots = configuration[CLIConfigurationKeys.CONTENT_ROOTS] ?: emptyList()

		@Suppress("DEPRECATION")
		val optionsBuilder = kaptConfig.options.toBuilder().apply {
			if (mode == AptMode.WITH_COMPILATION) {
				// K2 KAPT does not support WITH_COMPILATION (combined compilation + processing).
				// Translate to STUBS_AND_APT which generates stubs and then runs annotation processing.
				mode = AptMode.STUBS_AND_APT
			}
			compileClasspath.addAll(contentRoots.filterIsInstance<JvmClasspathRoot>().map { it.file })
			javaSourceRoots.addAll(contentRoots.filterIsInstance<JavaSourceRoot>().map { it.file })
			classesOutputDir = classesOutputDir ?: configuration.get(JVMConfigurationKeys.OUTPUT_DIRECTORY)
			if (processingClasspath.isEmpty()) {
				// Add a sentinel entry so FirKaptAnalysisHandlerExtension.checkOptions() does not
				// bail out early. The actual processors are provided by overriding loadProcessors().
				runCatching {
					processingClasspath.add(
						java.io.File(FluidFirKaptExtension::class.java.protectionDomain!!.codeSource.location.toURI())
					)
				}
			}
		}

		val messageCollector = configuration.get(CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY)
			?: PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, optionsBuilder.flags.contains(KaptFlag.VERBOSE))

		val kaptLogger = MessageCollectorBackedKaptLogger(
			isVerbose = optionsBuilder.flags.contains(KaptFlag.VERBOSE),
			isInfoAsWarnings = optionsBuilder.flags.contains(KaptFlag.INFO_AS_WARNINGS),
			messageCollector = messageCollector,
		)

		this.logger = kaptLogger
		this.options = optionsBuilder.build()

		configuration.put(KAPT_OPTIONS, optionsBuilder)

		kaptAnalysisInProgress.set(true)
		try {
			return super.doAnalysis(project, configuration)
		}
		finally {
			kaptAnalysisInProgress.set(false)
		}
	}

	override fun loadProcessors(): LoadedProcessors {
		val kaptConfig = currentKaptConfiguration()
			?: return super.loadProcessors()

		return LoadedProcessors(
			processors = kaptConfig.processors.map { processor ->
				IncrementalProcessor(processor, DeclaredProcType.NON_INCREMENTAL, this.logger)
			},
			classLoader = FluidFirKaptExtension::class.java.classLoader,
		)
	}
}
