package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.Url

interface PackageSearchEndpoints {
    val knownRepositories: Url
    val packageInfoByIds: Url
    val packageInfoByIdHashes: Url
    val searchPackages: Url
    val getScmsByUrl: Url
    val mavenPackageInfoByFileHash: Url
}
