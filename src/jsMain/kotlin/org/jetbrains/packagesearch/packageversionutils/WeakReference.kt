package org.jetbrains.packagesearch.packageversionutils

@JsName("WekRef")
actual external class WeakReference<T : Any> actual constructor(referred: T) {

    @JsName("deref")
    actual fun get(): T?
}