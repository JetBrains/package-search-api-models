# Package Search shared API models

This module contains the API models for the Package Search APIs. The models are annotated for use
with `kotlinx.serialization`, and should at all
times match the [Swagger specs](./swagger) for the same version.

API Models v1 are available in the `v1` package, but they had no well-defined specs.

## Using the API models

If you're developing inside Package Search's codebase, you can access `:server:api-models` directly as a project, or by
its coordinates. The API
models are published on the [public Package Search Maven repository](https://packages.jetbrains.team/maven/p/kpm/public)
on Space for
other teams to access, too.
In order to use the models from the repo, you need to declare the repository in your `build.gradle[.kts]`:

```kotlin
repositories {
    maven("https://packages.jetbrains.team/maven/p/kpm/public")
}
```      

Then add the dependency:

```kotlin
dependencies {
    implementation("org.jetbrains.packagesearch:api-models:[version]")
}
```