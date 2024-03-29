plugins {
    `version-catalog`
    `maven-publish`
    `version-config`
}

catalog {
    versionCatalog {
        from(files("../packagesearch-api-models.versions.toml"))
    }
}

publishing {
    publications {
        register<MavenPublication>("versionCatalog") {
            from(components["versionCatalog"])
            artifactId = "packagesearch-api-models-version-catalog"
        }
    }
}