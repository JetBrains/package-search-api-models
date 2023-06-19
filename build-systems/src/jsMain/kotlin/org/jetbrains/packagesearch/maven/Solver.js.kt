package org.jetbrains.packagesearch.maven

internal actual fun getenv(it: String): String? =
    process?.env?.get(it)

internal external interface Process {
    val env: Map<String, String>?
}

internal fun isProcessAvailable() =
    js("typeof process !== 'undefined' && process.env !== 'undefined'")
        .unsafeCast<Boolean>()

internal val process
    get() = if (isProcessAvailable()) js("process").unsafeCast<Process>() else null

internal actual fun getSystemProp(it: String): String? = null