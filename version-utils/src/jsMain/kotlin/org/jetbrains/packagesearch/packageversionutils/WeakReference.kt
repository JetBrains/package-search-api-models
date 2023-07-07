package org.jetbrains.packagesearch.packageversionutils

@JsName("WekRef")
public actual external class WeakReference<T : Any> public actual constructor(referred: T) {

    @JsName("deref")
    public actual fun get(): T?
}
