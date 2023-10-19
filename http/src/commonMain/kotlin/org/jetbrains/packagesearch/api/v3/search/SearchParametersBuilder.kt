package org.jetbrains.packagesearch.api.v3.search

import org.jetbrains.packagesearch.api.v3.http.SearchPackagesRequest

@DslMarker
public annotation class SearchParametersBuilderDsl

@SearchParametersBuilderDsl
public class SearchParametersBuilder {

    public var searchQuery: String? = null
    public var stableOnly: Boolean = true
    public var packagesType: List<PackagesType> = emptyList()

    public fun packagesType(block: PackagesTypeBuilder.() -> Unit) {
        packagesType = buildPackageTypes(block)
    }

    internal fun build(): SearchPackagesRequest {
        val query = searchQuery
        val errorText = "Search query is null or blank."
        requireNotNull(query) { errorText }
        require(query.isNotBlank()) { errorText }
        return SearchPackagesRequest(
            packagesType = packagesType,
            searchQuery = query,
//            stableOnly = stableOnly
        )
    }
}

public fun buildSearchParameters(block: SearchParametersBuilder.() -> Unit): SearchPackagesRequest =
    SearchParametersBuilder().apply(block).build()
