package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object GradleVariantSerializer : JsonContentPolymorphicSerializer<ApiGradlePackage.ApiVariant>(ApiGradlePackage.ApiVariant::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ApiGradlePackage.ApiVariant> = when {
        "available-at" in element.jsonObject ->
            ApiGradlePackage.ApiVariant.WithAvailableAt.serializer()
        "files" in element.jsonObject && "dependencies" in element.jsonObject ->
            ApiGradlePackage.ApiVariant.WithFiles.serializer()
        else -> error("Unable to determine the type to deserialize:\n${element.jsonObject}")
    }
}
