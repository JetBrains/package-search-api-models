import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

fun KotlinDependencyHandler.npm(dependencyProvider: Provider<MinimalExternalModuleDependency>): Dependency =
    npm(
        name = dependencyProvider.get().name,
        version = dependencyProvider.get().version ?: error("Version is required for ${dependencyProvider.get().name}")
    )