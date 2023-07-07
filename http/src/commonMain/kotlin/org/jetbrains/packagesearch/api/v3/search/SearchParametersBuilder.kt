package org.jetbrains.packagesearch.api.v3.search

@DslMarker
public annotation class SearchParametersBuilderDsl

@SearchParametersBuilderDsl
public class SearchParametersBuilder internal constructor() {

    public var searchQuery: String? = null
    public var packagesType: List<PackagesType> = emptyList()

    public fun packagesType(block: PackagesTypeBuilder.() -> Unit) {
        packagesType = buildPackageTypes(block)
    }

    internal fun build(): SearchParameters {
        val query = searchQuery
        val errorText = "Search query is null or blank."
        requireNotNull(query) { errorText }
        require(query.isNotBlank()) { errorText }
        return SearchParameters(
            packagesType = packagesType,
            searchQuery = query,
        )
    }
}

public fun buildSearchParameters(block: SearchParametersBuilder.() -> Unit): SearchParameters =
    SearchParametersBuilder().apply(block).build()
