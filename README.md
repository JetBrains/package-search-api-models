# Package Search shared API models

This module contains the API models for the Package Search APIs. The models are annotated for use
with `kotlinx.serialization`, and should at all
times match the [Swagger specs](./swagger) for the same version.

API Models v1 are available in the `v1` package, but they had no well-defined specs.

## Using the API models

If you're developing inside Package Search's codebase, you can access `:server:api-models` directly as a project, or by
its coordinates. The API
models are published on an [internal Maven repository](https://packages.jetbrains.team/maven/packagesearch) on Space for
other teams to access, too.
In order to use the models from the repo, you need to declare the repository in your `build.gradle[.kts]`:

```kotlin
repositories {
    maven { setUrl("https://packages.jetbrains.team/maven/packagesearch") }
}
```      

Then add the dependency:

```kotlin
dependencies {
    implementation("com.jetbrains.packagesearch:api-models:[version]")
}
```

## Updating the models

When updating the models with minor changes, which are compatible with previous versions of the models, you should just
increment the revision part of
the version. If you make any change that requires code recompilation by users, but keep the same major API version,
increment the minor part. If
there's a major version change, reset minor and revision to zero.

When you are ready to publish a new version, you can do so by running the [_Publish artifacts to Space Packages
(manual)_](https://buildserver.labs.intellij.net/buildConfiguration/kpm_api_models_publish?mode=branches) job on
TeamCity, setting
the `MAVEN_ARTIFACT_VERSION` argument as shown in the gif below. Don't forget to update the CHANGELOG file accordingly,
too.

![](images/updating-api-models-on-tc.gif)
