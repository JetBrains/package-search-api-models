package cache

import kotlinx.document.database.DataStore
import kotlinx.document.database.KotlinxDocumentDatabase
import kotlinx.document.database.getObjectCollection

internal class CacheDB(val dataStore: DataStore) {
    val db = KotlinxDocumentDatabase {
        store = dataStore
    }

    suspend fun apiRepositoryCache() =
        db.getObjectCollection<ApiRepositoryCacheEntry>("API_REPOSITORIES")

    suspend fun apiPackagesCache() =
        db.getObjectCollection<ApiPackageCacheEntry>("PACKAGES").apply {
            getAllIndexNames().ifEmpty {
                createIndex(ApiPackageCacheEntry::id.name)
                createIndex(ApiPackageCacheEntry::idHash.name)
            }
        }

    suspend fun searchPackageCache()=
        db.getObjectCollection<SearchPackageRequestCacheEntry>("SEARCH_PACKAGES").apply {
            getAllIndexNames().ifEmpty {
                createIndex(SearchPackageRequestCacheEntry::searchQuery.name)
            }
        }

    suspend fun scrollStartPackageCache() =
        db.getObjectCollection<SearchPackageScrollCacheEntry>("SCROLL_PACKAGES")

    suspend fun apiProjectsCache() =
        db.getObjectCollection<ApiProjectsCacheEntry>("API_PROJECTS").apply {
            getAllIndexNames().ifEmpty {
                createIndex(ApiProjectsCacheEntry::queryString.name)
            }
        }


}