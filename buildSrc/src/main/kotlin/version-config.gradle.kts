group = "org.jetbrains.packagesearch"

val GITHUB_REF: String? = System.getenv("GITHUB_REF")

version = when {
    GITHUB_REF?.startsWith("refs/tags/") == true -> GITHUB_REF.substringAfter("refs/tags/")
    else -> "3.1.0-SNAPSHOT"
}

