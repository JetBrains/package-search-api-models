package org.jetbrains.packagesearch.packageversionutils

public class WeakValueMap<K, V : Any> : MutableIterable<MutableMap.MutableEntry<K, V>> {

    private val innerMap = mutableMapOf<K, WeakReference<V>>()

    private fun <T> cleaningSequence(transform: (K, V) -> T) = sequence {
        val iterator = innerMap.iterator()
        while (iterator.hasNext()) {
            val (key, reference) = iterator.next()
            val value: V? = reference.get()
            if (value != null) {
                yield(transform(key, value))
            } else {
                iterator.remove()
            }
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

    public val entries: Sequence<MutableMap.MutableEntry<K, V>>
        get() = cleaningSequence { k, v -> asMutableEntry(k, v) }

    public val keys: MutableSet<K>
        get() = innerMap.keys

    public val size: Int
        get() = innerMap.size

    public val values: Sequence<V>
        get() = cleaningSequence { _, v -> v }

    public fun clear(): Unit = innerMap.clear()

    public fun isEmpty(): Boolean = innerMap.isEmpty()

    public fun remove(key: K): V? =
        innerMap.remove(key)?.get()

    public fun putAll(from: Map<out K, V>): Unit =
        innerMap.putAll(from.mapValues { WeakReference(it.value) })

    public operator fun set(key: K, value: V): V? =
        innerMap.put(key, WeakReference(value))?.get()

    public operator fun get(key: K): V? {
        val reference = innerMap[key]
        if (reference != null) {
            val value = reference.get()
            if (value == null) innerMap.remove(key)
            return value
        }
        return null
    }

    public fun containsValue(value: V): Boolean =
        value in cleaningSequence { _, v -> v }

    public fun containsKey(key: K): Boolean =
        get(key) != null

    public operator fun contains(key: K): Boolean = containsKey(key)

    public override fun iterator(): MutableIterator<MutableMap.MutableEntry<K, V>> =
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

public fun <K, V : Any> WeakValueMap<K, V>.getOrPut(key: K, value: () -> V): V =
    get(key) ?: value().also { set(key, it) }
