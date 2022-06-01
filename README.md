# Package Search shared API models

This module contains the API models for the Package Search APIs. The models are annotated for use
with `kotlinx.serialization`, and should at all
times match the [Swagger specs](./swagger) for the same version.

API Models v1 are available in the `v1` package, but they had no well-defined specs.

## Using the API models

```kotlin
repositories {
    maven("https://packages.jetbrains.team/maven/p/kpm/public")
}
```      

Then add the dependency:

```kotlin
dependencies {
    implementation("org.jetbrains.packagesearch:pkgs-api-models:[version]")
}
```