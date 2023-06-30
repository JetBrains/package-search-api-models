package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.jetbrains.packagesearch.api.v3.ApiMavenPackage.ApiVariant

object GradleVariantSerializer : JsonContentPolymorphicSerializer<ApiVariant>(ApiVariant::class) {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ApiVariant> = when {
        "available-at" in element.jsonObject ->
            ApiVariant.WithAvailableAt.serializer()
        "files" in element.jsonObject && "dependencies" in element.jsonObject ->
            ApiVariant.WithFiles.serializer()
        else -> error("Unable to determine the type to deserialize:\n${element.jsonObject}")
    }
}
