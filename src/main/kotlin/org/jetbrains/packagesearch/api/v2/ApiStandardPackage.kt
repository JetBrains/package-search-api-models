package org.jetbrains.packagesearch.api.v2

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiStandardPackage(
    @SerialName("group_id") override val groupId: String,
    @SerialName("artifact_id") override val artifactId: String,
    @SerialName("name") val name: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("url") val url: String? = null,
    @SerialName("licenses") val licenses: ApiLicenses? = null,
    @SerialName("scm") val scm: ApiScm? = null,
    @SerialName("mpp") val mpp: ApiMpp? = null,
    @SerialName("platforms") val platforms: List<ApiPlatform>? = null,
    @SerialName("authors") val authors: List<ApiAuthor>? = null,
    @SerialName("latest_version") val latestVersion: ApiStandardVersion,
    @SerialName("versions") override val versions: List<ApiStandardVersion>,
    @SerialName("dependency_rating") val dependencyRating: Double,
    @SerialName("github") val gitHub: ApiGitHub? = null,
    @SerialName("stackoverflow") val stackOverflow: ApiStackOverflow? = null
) : ApiPackage<ApiStandardPackage.ApiStandardVersion> {

    @Serializable
    data class ApiLicenses(
        @SerialName("main_license") val mainLicense: ApiLinkedFile,
        @SerialName("other_licenses") val otherLicenses: List<ApiLinkedFile>? = null
    )

    @Serializable
    data class ApiScm(
        @SerialName("url") val url: String
    )

    @Serializable
    data class ApiMpp(
        @SerialName("module_type") val moduleType: String
    ) {

        companion object {

            const val MODULE_TYPE_ROOT = "root"
            const val MODULE_TYPE_CHILD = "child"
        }
    }

    @Serializable
    data class ApiPlatform(
        @SerialName("type") val type: String,
        @SerialName("targets") val targets: List<String>? = null
    ) {

        companion object {

            @Deprecated("Use the enum")
            const val PLATFORM_TYPE_JVM = "jvm"

            @Deprecated("Use the enum")
            const val PLATFORM_TYPE_JS = "js"

            @Deprecated("Use the enum")
            const val PLATFORM_TYPE_NATIVE = "native"

            @Deprecated("Use the enum")
            const val PLATFORM_TYPE_COMMON = "common"

            @Deprecated("Use the enum")
            const val PLATFORM_TYPE_ANDROID = "android"
        }

        @Serializable
        enum class PlatformType(val serialName: String) {

            @SerialName("js")
            JS("js"),

            @SerialName("jvm")
            JVM("jvm"),

            @SerialName("common")
            COMMON("common"),

            @SerialName("native")
            NATIVE("native"),

            @SerialName("androidJvm")
            ANDROID_JVM("androidJvm"),
            UNSUPPORTED("unsupported");

            companion object {

                fun from(serialName: String) = values().find { it.serialName == serialName } ?: UNSUPPORTED
            }
        }

        @Serializable
        enum class PlatformTarget(val serialName: String) {

            // *********** Kotlin/JS targets ***********
            @SerialName("node")
            NODE("node"),

            @SerialName("browser")
            BROWSER("browser"),

            // *********** Kotlin/Native targets ***********
            // See the org.jetbrains.kotlin.konan.target.KonanTarget class in the Kotlin code
            @SerialName("android_x64")
            ANDROID_X64("android_x64"),

            @SerialName("android_x86")
            ANDROID_X86("android_x86"),

            @SerialName("android_arm32")
            ANDROID_ARM32("android_arm32"),

            @SerialName("android_arm64")
            ANDROID_ARM64("android_arm64"),

            @SerialName("ios_arm32")
            IOS_ARM32("ios_arm32"),

            @SerialName("ios_arm64")
            IOS_ARM64("ios_arm64"),

            @SerialName("ios_x64")
            IOS_X64("ios_x64"),

            @SerialName("watchos_arm32")
            WATCHOS_ARM32("watchos_arm32"),

            @SerialName("watchos_arm64")
            WATCHOS_ARM64("watchos_arm64"),

            @SerialName("watchos_x86")
            WATCHOS_X86("watchos_x86"),

            @SerialName("watchos_x64")
            WATCHOS_X64("watchos_x64"),

            @SerialName("tvos_arm64")
            TVOS_ARM64("tvos_arm64"),

            @SerialName("tvos_x64")
            TVOS_X64("tvos_x64"),

            @SerialName("linux_x64")
            LINUX_X64("linux_x64"),

            @SerialName("mingw_x86")
            MINGW_X86("mingw_x86"),

            @SerialName("mingw_x64")
            MINGW_X64("mingw_x64"),

            @SerialName("macos_x64")
            MACOS_X64("macos_x64"),

            @SerialName("macos_arm64")
            MACOS_ARM64("macos_arm64"),

            @SerialName("linux_arm64")
            LINUX_ARM64("linux_arm64"),

            @SerialName("linux_arm32_hfp")
            LINUX_ARM32_HFP("linux_arm32_hfp"),

            @SerialName("linux_mips32")
            LINUX_MIPS32("linux_mips32"),

            @SerialName("linux_mipsel32")
            LINUX_MIPSEL32("linux_mipsel32"),

            @SerialName("wasm32")
            WASM_32("wasm32"),

            UNSUPPORTED("unsupported");

            companion object {

                fun from(serialName: String) = values().find { it.serialName == serialName } ?: UNSUPPORTED
            }
        }
    }

    @Serializable
    data class ApiAuthor(
        @SerialName("name") val name: String? = null,
        @SerialName("org") val org: String? = null,
        @SerialName("org_url") val orgUrl: String? = null
    )

    @Serializable
    data class ApiStandardVersion(
        @SerialName("version") override val version: String,
        @SerialName("last_changed") val lastChanged: Long,
        @SerialName("stable") val stable: Boolean,
        @SerialName("repository_ids") override val repositoryIds: List<String>,
        @SerialName("artifacts") val artifacts: List<ApiArtifact>
    ) : ApiVersion {

        @Serializable
        data class ApiArtifact(
            @SerialName("sha256") val sha256: String? = null,
            @SerialName("sha1") val sha1: String? = null,
            @SerialName("md5") val md5: String? = null,
            @SerialName("packaging") val packaging: String? = null,
            @SerialName("classifier") val classifier: String? = null
        )
    }

    @Serializable
    data class ApiGitHub(
        @SerialName("description") val description: String? = null,
        @SerialName("is_fork") val isFork: Boolean? = null,
        @SerialName("stars") val stars: Int? = null,
        @SerialName("watchers") val watchers: Int? = null,
        @SerialName("forks") val forks: Int? = null,
        @SerialName("subscribers") val subscribers: Int? = null,
        @SerialName("network") val network: Int? = null,
        @SerialName("community_profile") val communityProfile: ApiCommunityProfile? = null,
        @SerialName("last_checked") val lastChecked: Long
    ) {

        @Serializable
        data class ApiCommunityProfile(
            @SerialName("files") val files: ApiCommunityProfileFiles,
            @SerialName("documentation") val documentation: String? = null,
            @SerialName("description") val description: String? = null,
            @SerialName("health_percentage") val healthPercentage: Int
        ) {

            @Serializable
            data class ApiCommunityProfileFiles(
                @SerialName("license") val license: ApiLinkedFile? = null,
                @SerialName("readme") val readme: ApiLinkedFile? = null,
                @SerialName("code_of_conduct") val codeOfConduct: ApiLinkedFile? = null,
                @SerialName("contributing") val contributing: ApiLinkedFile? = null
            )
        }
    }

    @Serializable
    data class ApiStackOverflow(
        @SerialName("tags") val tags: List<ApiStackOverflowTag>
    ) {

        @Serializable
        data class ApiStackOverflowTag(
            @SerialName("tag") val tag: String,
            @SerialName("count") val count: Int
        )
    }

    @Serializable
    data class ApiLinkedFile(
        @SerialName("name") val name: String? = null,
        @SerialName("url") val url: String,
        @SerialName("html_url") val htmlUrl: String? = null,
        @SerialName("spdx_id") val spdxId: String? = null,
        @SerialName("key") val key: String? = null
    )
}
