package org.jetbrains.packagesearch.api.v3.revision1.http

import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.Flow
import org.jetbrains.packagesearch.api.v3.revision1.ApiPackage
import org.jetbrains.packagesearch.api.v3.revision1.ApiProject
import org.jetbrains.packagesearch.api.v3.revision1.ApiRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public interface PackageSearchApi {
    public suspend fun getKnownRepositories(requestBuilder: (HttpRequestBuilder.() -> Unit)? = null): List<ApiRepository>

    public suspend fun getPackageInfoByIds(
        request: GetPackageInfoRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage>

    public suspend fun getPackageInfoByIdHashes(
        request: GetPackageInfoRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): Map<String, ApiPackage>

    public suspend fun searchPackages(
        request: SearchPackagesRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiPackage>

    public suspend fun startScroll(
        request: SearchPackagesStartScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse

    public suspend fun nextScroll(
        request: SearchPackagesNextScrollRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): SearchPackagesScrollResponse

    public suspend fun searchProjects(
        request: SearchProjectRequest,
        requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    ): List<ApiProject>

    public fun isOnlineFlow(pollingInterval: Duration = 30.seconds): Flow<Boolean>
}
