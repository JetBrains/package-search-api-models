package org.jetbrains.packagesearch.api.v3.http

import io.ktor.http.Url

public fun main() {

    fun printUrl(url: Url) = println(url.toString())

    PackageSearchEndpoints.PROD.apply {
        printUrl(health)
        printUrl(nextScroll)
        printUrl(knownRepositories)
        printUrl(packageInfoByIdHashes)
        printUrl(packageInfoByIds)
        printUrl(refreshPackagesInfo)
        printUrl(searchPackages)
        printUrl(searchProjects)
        printUrl(startScroll)
    }
}
/*
https://api.prod.package-search.services.jetbrains.com/health
https://api.prod.package-search.services.jetbrains.com/search-packages/scroll/next
https://api.prod.package-search.services.jetbrains.com/known-repositories
https://api.prod.package-search.services.jetbrains.com/package-info-by-id-hashes
https://api.prod.package-search.services.jetbrains.com/package-info-by-ids
https://api.prod.package-search.services.jetbrains.com/refresh-packages-info
https://api.prod.package-search.services.jetbrains.com/search-packages
https://api.prod.package-search.services.jetbrains.com/search-projects
https://api.prod.package-search.services.jetbrains.com/search-packages/scroll/start
https://api.prod.package-search.services.jetbrains.com/health
https://api.prod.package-search.services.jetbrains.com/search-packages/scroll/next
https://api.prod.package-search.services.jetbrains.com/known-repositories
https://api.prod.package-search.services.jetbrains.com/package-info-by-id-hashes
https://api.prod.package-search.services.jetbrains.com/package-info-by-ids
https://api.prod.package-search.services.jetbrains.com/refresh-packages-info
https://api.prod.package-search.services.jetbrains.com/search-packages
https://api.prod.package-search.services.jetbrains.com/search-projects
https://api.prod.package-search.services.jetbrains.com/search-packages/scroll/

 */