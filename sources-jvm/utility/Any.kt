package io.fluidsonic.compiler


internal inline fun <Result> Boolean.thenTake(block: () -> Result): Result? =
	if (this) block() else null
