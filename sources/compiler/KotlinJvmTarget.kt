package io.fluidsonic.compiler


enum class KotlinJvmTarget(internal val string: String) {

	v1_6("1.6"),
	v1_8("1.8");


	override fun toString(): String = string
}
