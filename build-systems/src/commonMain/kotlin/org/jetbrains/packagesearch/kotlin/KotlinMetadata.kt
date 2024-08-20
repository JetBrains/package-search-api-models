package org.jetbrains.packagesearch.kotlin;

import kotlinx.serialization.Serializable

@Serializable
public data class KotlinMetadata(
    val schemaVersion: String,
    val buildSystem: String,
    val buildSystemVersion: String,
    val buildPlugin: String,
    val buildPluginVersion: String,
    val projectSettings: ProjectSettings,
    val projectTargets: List<ProjectTarget>
)

@Serializable
public data class ProjectSettings(
    val isHmppEnabled: Boolean,
    val isCompatibilityMetadataVariantEnabled: Boolean,
    val isKPMEnabled: Boolean
)

@Serializable
public data class ProjectTarget(
    val target: String,
    val platformType: String,
    val extras: Extras? = null
)

@Serializable
public data class Extras(
    val native: NativeExtras? = null,
    val js: JsExtras? = null,
    val jvm: JvmExtras? = null
)

@Serializable
public data class NativeExtras(
    val konanTarget: String,
    val konanVersion: String,
    val konanAbiVersion: String
)

@Serializable
public data class JsExtras(
    val isBrowserConfigured: Boolean,
    val isNodejsConfigured: Boolean
)

@Serializable
public data class JvmExtras(
    val jvmTarget: String,
    val withJavaEnabled: Boolean
)