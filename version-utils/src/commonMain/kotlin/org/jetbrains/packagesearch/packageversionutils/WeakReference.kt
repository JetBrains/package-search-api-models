package org.jetbrains.packagesearch.packageversionutils

public expect class WeakReference<T : Any>(referred: T) {
    public fun get(): T?
}
