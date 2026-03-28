package io.fluidsonic.compiler

import java.io.*


internal fun findAllClasspathEntries(): Set<File> =
	(findSystemPropertyClasspathEntries() + findClasspathEntriesUsingManifest())
		.map { it.absoluteFile }
		.toSet()


private fun findClasspathEntriesUsingManifest(): Collection<File> =
	ClassLoader.getSystemClassLoader().getResources("META-INF/MANIFEST.MF")
		.toList()
		.filter { it.protocol == "jar" }
		.map { File(it.path.removePrefix("file:").substringBeforeLast('!')) }


private fun findSystemPropertyClasspathEntries(): Collection<File> =
	System.getProperty("java.class.path").split(':').map(::File)
