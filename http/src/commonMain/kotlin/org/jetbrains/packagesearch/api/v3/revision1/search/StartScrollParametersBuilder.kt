package org.jetbrains.packagesearch.api.v3.revision1.search

import org.jetbrains.packagesearch.api.v3.revision1.http.DEFAULT_BATCH_SIZE
import org.jetbrains.packagesearch.api.v3.revision1.http.DEFAULT_SCROLL_DURATION
import org.jetbrains.packagesearch.api.v3.revision1.http.SearchPackagesStartScrollRequest

@SearchParametersBuilderDsl
public class StartScrollParametersBuilder {
    public var searchQuery: String? = null
    public var packagesType: List<PackagesType> = emptyList()
    public var batchSize: Int? = null
    public var duration: String? = null

    public fun packagesType(block: PackagesTypeBuilder.() -> Unit) {
        packagesType = buildPackageTypes(block)
    }

    internal fun build(): SearchPackagesStartScrollRequest {
        return SearchPackagesStartScrollRequest(
            packagesType = packagesType,
            searchQuery = searchQuery ?: "",
            batchSize = batchSize ?: DEFAULT_BATCH_SIZE,
            duration = duration ?: DEFAULT_SCROLL_DURATION,
        )
    }
}

public fun buildStartScrollParameters(block: StartScrollParametersBuilder.() -> Unit): SearchPackagesStartScrollRequest =
    StartScrollParametersBuilder().apply(block).build()
