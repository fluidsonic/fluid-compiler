package io.fluidsonic.compiler

import java.io.*
import kotlin.test.*


class GeneratedFileTest {

	@Test
	fun construction_setsProperties() {
		val file = GeneratedFile(
			content = "class Foo",
			path = File("com/example/Foo.kt"),
		)

		assertEquals(actual = file.content, expected = "class Foo")
		assertEquals(actual = file.path, expected = File("com/example/Foo.kt"))
	}


	@Test
	fun equality_worksForDataClass() {
		val file1 = GeneratedFile(content = "content", path = File("path.kt"))
		val file2 = GeneratedFile(content = "content", path = File("path.kt"))

		assertEquals(actual = file1, expected = file2)
		assertEquals(actual = file1.hashCode(), expected = file2.hashCode())
	}


	@Test
	fun inequality_whenDifferentContent() {
		val file1 = GeneratedFile(content = "a", path = File("path.kt"))
		val file2 = GeneratedFile(content = "b", path = File("path.kt"))

		assertNotEquals(actual = file1, illegal = file2)
	}


	@Test
	fun inequality_whenDifferentPath() {
		val file1 = GeneratedFile(content = "content", path = File("a.kt"))
		val file2 = GeneratedFile(content = "content", path = File("b.kt"))

		assertNotEquals(actual = file1, illegal = file2)
	}
}
