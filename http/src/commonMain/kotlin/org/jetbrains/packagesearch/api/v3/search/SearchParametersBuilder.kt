package org.jetbrains.packagesearch.api.v3.search

@DslMarker
annotation class SearchParametersBuilderDsl

@SearchParametersBuilderDsl
class SearchParametersBuilder internal constructor() {

    var searchQuery: String? = null
    var packagesType: List<PackagesType> = emptyList()

    fun packagesType(block: PackagesTypeBuilder.() -> Unit) {
        packagesType = buildPackagesType(block)
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

fun buildSearchParameters(block: SearchParametersBuilder.() -> Unit) =
    SearchParametersBuilder().apply(block).build()
