package org.jetbrains.packagesearch.packageversionutils

class WeakValueMap<K, V : Any> : MutableIterable<MutableMap.MutableEntry<K, V>> {

    private val innerMap = mutableMapOf<K, WeakReference<V>>()

    private fun <T> cleaningSequence(transform: (K, V) -> T) = sequence {
        val iterator = innerMap.iterator()
        while (iterator.hasNext()) {
            val (key, reference) = iterator.next()
            val value: V? = reference.get()
            if (value != null) yield(transform(key, value))
            else iterator.remove()
        }
    }

    private fun asMutableEntry(key: K, value: V): MutableMap.MutableEntry<K, V> =
        object : MutableMap.MutableEntry<K, V> {
            override val key = key
            override val value: V = value
            override fun setValue(newValue: V): V {
                innerMap[key] = WeakReference(newValue)
                return value
            }
        }

    val entries
        get() = cleaningSequence { k, v -> asMutableEntry(k, v) }

    val keys
        get() = innerMap.keys

    val size: Int
        get() = innerMap.size

    val values
        get() = cleaningSequence { _, v -> v }

    fun clear() = innerMap.clear()

    fun isEmpty() = innerMap.isEmpty()

    fun remove(key: K) =
        innerMap.remove(key)?.get()

    fun putAll(from: Map<out K, V>) =
        innerMap.putAll(from.mapValues { WeakReference(it.value) })

    operator fun set(key: K, value: V): V? =
        innerMap.put(key, WeakReference(value))?.get()

    operator fun get(key: K): V? {
        val reference = innerMap[key]
        if (reference != null) {
            val value = reference.get()
            if (value == null) innerMap.remove(key)
            return value
        }
        return null
    }

    fun containsValue(value: V): Boolean =
        value in cleaningSequence { _, v -> v }

    fun containsKey(key: K): Boolean =
        get(key) != null

    operator fun contains(key: K) = containsKey(key)

    override fun iterator() =
        object : MutableIterator<MutableMap.MutableEntry<K, V>> {
            val innerIterator = entries.iterator()
            var lasKey: K? = null
            override fun hasNext() = innerIterator.hasNext()
            override fun next() = innerIterator.next().also { lasKey = it.key }
            override fun remove() {
                innerMap.remove(lasKey)
            }
        }

}

fun <K, V : Any> WeakValueMap<K, V>.getOrPut(key: K, value: () -> V): V =
    get(key) ?: value().also { set(key, it) }