package io.fluidsonic.compiler

import kotlin.test.*


class ClassLoaderTest {

	@Test
	fun findAllClasspathEntries_returnsNonEmptySet() {
		val entries = findAllClasspathEntries()
		assertTrue(entries.isNotEmpty())
	}


	@Test
	fun findAllClasspathEntries_returnsAbsoluteFiles() {
		val entries = findAllClasspathEntries()
		for (entry in entries) {
			assertTrue(entry.isAbsolute, "Expected absolute path: $entry")
		}
	}


	@Test
	fun findAllClasspathEntries_returnsFiles() {
		val entries = findAllClasspathEntries()
		for (entry in entries) {
			assertTrue(entry.isAbsolute, "Expected absolute path: $entry")
		}
	}
}
