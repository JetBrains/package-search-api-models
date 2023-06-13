package org.jetbrains.packagesearch.packageversionutils

expect class WeakReference<T: Any>(referred: T) {
    fun get(): T?
}