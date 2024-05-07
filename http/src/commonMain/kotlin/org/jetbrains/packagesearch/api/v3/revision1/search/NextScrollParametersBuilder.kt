package org.jetbrains.packagesearch.api.v3.revision1.search

import org.jetbrains.packagesearch.api.v3.revision1.http.DEFAULT_SCROLL_DURATION
import org.jetbrains.packagesearch.api.v3.revision1.http.SearchPackagesNextScrollRequest

@SearchParametersBuilderDsl
public class NextScrollParametersBuilder {
    public var scrollId: String? = null
    public var duration: String? = null

    internal fun build(): SearchPackagesNextScrollRequest {
        val scrollIdCopy = scrollId
        requireNotNull(scrollIdCopy) { "To get next batch, scrollId must be specified." }

        return SearchPackagesNextScrollRequest(
            scrollId = scrollIdCopy,
            duration = duration ?: DEFAULT_SCROLL_DURATION,
        )
    }
}

public fun buildNextScrollParameters(block: NextScrollParametersBuilder.() -> Unit): SearchPackagesNextScrollRequest =
    NextScrollParametersBuilder().apply(block).build()
