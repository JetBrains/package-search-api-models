package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.Url

public interface PackageSearchEndpoints {
    public val knownRepositories: Url
    public val packageInfoByIds: Url
    public val packageInfoByIdHashes: Url
    public val searchPackages: Url
    public val searchProjects: Url
    public val getScmsByUrl: Url
    public val mavenPackageInfoByFileHash: Url
}
