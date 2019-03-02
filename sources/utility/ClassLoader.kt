package com.github.fluidsonic.fluid.compiler

import sun.misc.Unsafe
import java.io.File
import java.net.URL
import java.net.URLClassLoader


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
	when {
		this is URLClassLoader ->
			urLs.map { File(it.toURI()) }

		this::class.java.name.startsWith("jdk.internal.loader.ClassLoaders$") ->
			try {
				val field = Unsafe::class.java.getDeclaredField("theUnsafe")!!.also { it.isAccessible = true }
				val unsafe = field.get(null) as Unsafe

				// jdk.internal.loader.ClassLoaders.AppClassLoader.ucp
				val ucpField = this::class.java.getDeclaredField("ucp")!!
				val ucp = unsafe.getObject(this, unsafe.objectFieldOffset(ucpField))!!

				// jdk.internal.loader.URLClassPath.path
				val pathField = ucpField.type.getDeclaredField("path")!!
				val path = unsafe.getObject(ucp, unsafe.objectFieldOffset(pathField)) as List<URL>

				path.map { File(it.toURI()) }
			}
			catch (e: Exception) {
				emptyList<File>()
			}

		else ->
			emptyList()
	} + parent?.findAllClasspathEntries().orEmpty()
