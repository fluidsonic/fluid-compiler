package io.fluidsonic.compiler

import kotlin.test.*


class KotlinJvmTargetTest {

	@Test
	fun entries_containsAllExpectedTargets() {
		assertEquals(
			actual = KotlinJvmTarget.entries.map { it.name },
			expected = listOf("v21", "v22", "v23", "v24"),
		)
	}


	@Test
	fun toString_returnsVersionString() {
		assertEquals(actual = KotlinJvmTarget.v21.toString(), expected = "21")
		assertEquals(actual = KotlinJvmTarget.v22.toString(), expected = "22")
		assertEquals(actual = KotlinJvmTarget.v23.toString(), expected = "23")
		assertEquals(actual = KotlinJvmTarget.v24.toString(), expected = "24")
	}


	@Test
	fun string_matchesExpectedValue() {
		assertEquals(actual = KotlinJvmTarget.v21.string, expected = "21")
		assertEquals(actual = KotlinJvmTarget.v24.string, expected = "24")
	}
}
