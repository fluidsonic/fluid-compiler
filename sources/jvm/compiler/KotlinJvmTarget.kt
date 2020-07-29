package io.fluidsonic.compiler


@Suppress("EnumEntryName")
public enum class KotlinJvmTarget(internal val string: String) {

	v1_6("1.6"),
	v1_8("1.8"),
	v9("9"),
	v10("10"),
	v11("11"),
	v12("12"),
	v13("13"),
	v14("14");


	override fun toString(): String = string
}
