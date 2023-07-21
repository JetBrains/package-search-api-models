package org.jetbrains.packagesearch.gradle

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

/**
 * Based on Gradle module metadata specification 1.0
 * https://github.com/gradle/gradle/blob/f6a98158e75a636245f70d46604fcab3152361e8/subprojects/docs/src/docs/design/gradle-module-metadata-1.0-specification.md
 */

@Serializable
public data class GradleMetadata(
    val formatVersion: String,
    val component: Component? = null,
    val createdBy: CreatedBy? = null,
    val variants: List<Variant>? = null
)

@Serializable
public data class Component(
    val group: String,
    val module: String,
    val version: String,
    val url: String? = null,
)

@Serializable
public data class CreatedBy(
    val gradle: Gradle? = null
)

@Serializable
public data class Gradle(
    val version: String,
    val buildId: String
)

@Serializable
public data class Variant(
    val name: String,
    val attributes: Attributes? = null,
    @SerialName("available-at") val availableAt: AvailableAt? = null,
    val dependencies: List<Dependency>? = null,
    val dependencyConstraints: List<DependencyConstraint>? = null,
    val files: List<File>? = null,
    val capabilities: List<Capability>? = null
)

@Serializable
public data class AvailableAt(
    val url: String,
    val group: String,
    val module: String,
    val version: String
)

@Serializable
public data class Dependency(
    val group: String,
    val module: String,
    val version: Version? = null,
    val excludes: List<Exclude>? = null,
    val reason: String? = null,
    val attributes: Attributes? = null,
    val requestedCapabilities: List<Capability>? = null,
)

@Serializable
public data class Version(
    val requires: String? = null,
    val prefers: String? = null,
    val strictly: String? = null,
    val rejects: List<String>? = null,
)

@Serializable
public data class DependencyConstraint(
    val group: String,
    val module: String,
    val version: Version? = null,
    val reason: String? = null,
    val attributes: Attributes? = null
)

@Serializable
public data class File(
    val name: String,
    val url: String,
    val size: Int,
    val sha1: String,
    val md5: String
)

public typealias Attributes = Map<String, JsonPrimitive>

@Serializable
public data class Capability(
    val group: String,
    val name: String,
    val version: String
)

@Serializable
public data class Exclude(
    val group: String,
    val module: String
)
