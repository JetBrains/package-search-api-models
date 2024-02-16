package org.jetbrains.packagesearch.api.v3.http

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.Flow
import org.jetbrains.packagesearch.api.v3.ApiPackage
import org.jetbrains.packagesearch.api.v3.ApiProject
import org.jetbrains.packagesearch.api.v3.ApiRepository

public interface PackageSearchApi {
    public suspend fun getKnownRepositories(): List<ApiRepository>
    public suspend fun getPackageInfoByIds(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun getPackageInfoByIdHashes(ids: Set<String>): Map<String, ApiPackage>
    public suspend fun searchPackages(request: SearchPackagesRequest): List<ApiPackage>
    public suspend fun startScroll(request: SearchPackagesStartScrollRequest): SearchPackagesScrollResponse
    public suspend fun nextScroll(request: SearchPackagesNextScrollRequest): SearchPackagesScrollResponse
    public suspend fun searchProjects(request: SearchProjectRequest): List<ApiProject>
    public fun isOnlineFlow(pollingInterval: Duration = 30.seconds): Flow<Boolean>
}