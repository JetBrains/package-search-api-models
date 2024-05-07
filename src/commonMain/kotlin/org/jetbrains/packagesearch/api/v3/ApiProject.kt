package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.Serializable

@Serializable
public data class ApiProject(
    val id: String,
    // [maven:io.ktor:ktor-client-cio, maven:io.ktor:ktor-client-content-negotiation]
    val packageIds: List<String>,
    val name: String,
    val description: String,
    val tags: List<String>,
    val exampleScmIds: List<String>,
    val platformsForLatestStableVersion: List<String>, // macosArm64, mingwX64, ecc...
    val platformsForLatestVersion: List<String> // macosArm64, mingwX64, ecc...
)
