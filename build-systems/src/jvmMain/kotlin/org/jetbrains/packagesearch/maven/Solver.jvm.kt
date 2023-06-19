package org.jetbrains.packagesearch.maven

internal actual fun getenv(it: String): String? = System.getenv(it)
internal actual fun getSystemProp(it: String): String? = System.getProperty(it)