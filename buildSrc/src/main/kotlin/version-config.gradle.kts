group = "org.jetbrains.packagesearch"
version = "3.0.0"

val GITHUB_REF: String? = System.getenv("GITHUB_REF")
val devBranches = listOf("v3", "dev")
version = when {
    GITHUB_REF == null -> version
    GITHUB_REF.startsWith("refs/tags/") -> GITHUB_REF.substringAfter("refs/tags/")
    GITHUB_REF.startsWith("refs/heads") && devBranches.any { it in GITHUB_REF } ->
        "$version-SNAPSHOT"
    else -> version
}