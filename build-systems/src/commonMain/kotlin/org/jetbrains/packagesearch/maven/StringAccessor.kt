package org.jetbrains.packagesearch.maven

import org.jetbrains.packagesearch.maven.StringAccessor.ObjectAccessor
import org.jetbrains.packagesearch.maven.StringAccessor.SimpleAccessor
import kotlin.jvm.JvmInline

// should be generated...
fun ProjectModel.asAccessor(): ObjectAccessor = object : ObjectAccessor {
    override operator fun get(key: String) = when (key) {
        "groupId" -> SimpleAccessor(groupId)
        "artifactId" -> SimpleAccessor(artifactId)
        "version" -> SimpleAccessor(version)
        "name" -> SimpleAccessor(name)
        "description" -> SimpleAccessor(description)
        "url" -> SimpleAccessor(url)
        "packaging" -> SimpleAccessor(packaging)
        "organization" -> organization?.asAccessor()
        "parent" -> parent?.asAccessor()
        "properties" -> object : ObjectAccessor {
            override fun get(key: String) = SimpleAccessor(properties[key])
        }
        else -> null
    }
}

fun Organization.asAccessor(): StringAccessor = object : ObjectAccessor {
    override fun get(key: String) = when (key) {
        "name" -> SimpleAccessor(name)
        "url" -> SimpleAccessor(url)
        else -> null
    }
}

fun Parent.asAccessor(): StringAccessor = object : ObjectAccessor {
    override fun get(key: String) = when (key) {
        "groupId" -> SimpleAccessor(groupId)
        "artifactId" -> SimpleAccessor(artifactId)
        "version" -> SimpleAccessor(version)
        "relativePath" -> SimpleAccessor(relativePath)
        else -> null
    }
}

sealed interface StringAccessor {
    interface ObjectAccessor: StringAccessor {
        operator fun get(key: String): StringAccessor?
    }
    @JvmInline
    value class SimpleAccessor(val value: String?) : StringAccessor
}
