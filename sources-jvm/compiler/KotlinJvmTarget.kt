package io.fluidsonic.compiler


/** JVM bytecode target versions supported by the Kotlin compiler. */
@Suppress("EnumEntryName")
public enum class KotlinJvmTarget(internal val string: String) {

	/** JVM 21 bytecode target. */
	v21("21"),
	/** JVM 22 bytecode target. */
	v22("22"),
	/** JVM 23 bytecode target. */
	v23("23"),
	/** JVM 24 bytecode target. */
	v24("24"),
	;


	override fun toString(): String = string
}
