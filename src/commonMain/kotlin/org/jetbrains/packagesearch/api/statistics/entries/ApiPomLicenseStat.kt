package org.jetbrains.packagesearch.api.statistics.entries

import kotlinx.serialization.Serializable

@Serializable
data class ApiPomLicenseStat(val license: String, val count: Int)
