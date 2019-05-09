package com.github.fluidsonic.fluid.compiler

import com.github.fluidsonic.fluid.stdlib.*
import java.io.*
import java.net.*


private const val appClassLoaderName = "jdk.internal.loader.ClassLoaders\$AppClassLoader"


private fun ClassLoader.addUrl(url: URL): Boolean {
	try {
		val classPath = urlClassPath ?: return false
		val addUrl = classPath::class.java.getDeclaredMethod("addURL", URL::class.java) ?: return false
		addUrl.invoke(classPath, url)

		return true
	}
	catch (e: Exception) {
		return false
	}
}


private val ClassLoader.urlClassPath: Any?
	get() = try {
		(this is URLClassLoader || this::class.java.name == appClassLoaderName).thenTake {
			this::class.java.getDeclaredField("ucp")
				?.also { it.isAccessible = true }
				?.let { it.get(this) }
		}
	}
	catch (e: Exception) {
		null
	}


@Suppress("UNCHECKED_CAST")
private val ClassLoader.urls: Collection<URL>?
	get() = when {
		this is URLClassLoader ->
			urLs.toList()

		this::class.java.name == appClassLoaderName ->
			try {
				urlClassPath?.let { urlClassPath ->
					urlClassPath::class.java.getDeclaredMethod("getURLs")
						?.invoke(urlClassPath)
						?.let { it as? Array<URL> }
						?.toList()
				}
			}
			catch (e: Exception) {
				null
			}

		else ->
			null
	}


internal fun findAllClasspathEntries(): Set<File> =
	(findSystemPropertyClasspathEntries() +
		findSystemClassLoaderClasspathEntries() +
		findClasspathEntriesUsingManifest())
		.map { it.absoluteFile }
		.toSet()


private fun findSystemClassLoaderClasspathEntries(): Collection<File> =
	ClassLoader.getSystemClassLoader().findAllClasspathEntries()


private fun findClasspathEntriesUsingManifest(): Collection<File> =
	ClassLoader.getSystemClassLoader().getResources("META-INF/MANIFEST.MF")
		.toList()
		.filter { it.protocol == "jar" }
		.map { File(it.path.removePrefix("file:").substringBeforeLast('!')) }


private fun findSystemPropertyClasspathEntries(): Collection<File> =
	System.getProperty("java.class.path").split(':').map(::File)


@Suppress("UNCHECKED_CAST")
private fun ClassLoader.findAllClasspathEntries(): Collection<File> =
	urls?.map { File(it.toURI()) }.orEmpty() + parent?.findAllClasspathEntries().orEmpty()


internal fun loadToolsJarIfNeeded(): Boolean {
	try {
		Class.forName("com.sun.tools.javac.util.Context")
		return true
	}
	catch (e: ClassNotFoundException) {
		// not loaded
	}

	val toolsJar = findToolsJar() ?: return false
	ClassLoader.getSystemClassLoader().addUrl(toolsJar.toURI().toURL())

	try {
		Class.forName("com.sun.tools.javac.util.Context")
		return true
	}
	catch (e: ClassNotFoundException) {
		return false
	}
}


private fun findToolsJar() =
	System.getProperty("java.home").ifEmpty { null }?.let(::File)?.let(::findToolsJar)
		?: System.getenv("JAVA_HOME").ifEmpty { null }?.let(::File)?.let(::findToolsJar)


private fun findToolsJar(javaHome: File): File? {
	javaHome.resolve("lib/tools.jar").takeIf(File::exists)?.let { return it }

	if (javaHome.name.equals("jre", ignoreCase = true))
		findToolsJar(javaHome.parentFile)?.let { return it }

	if (javaHome.name.matches(Regex("jre\\d+")) || javaHome.name == "jre${System.getProperty("java.version")}")
		findToolsJar(javaHome.parentFile.resolve("jdk" + javaHome.name.removePrefix("jre")))

	return null
}
