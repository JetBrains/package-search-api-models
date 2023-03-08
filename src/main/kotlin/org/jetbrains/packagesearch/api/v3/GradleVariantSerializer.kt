package org.jetbrains.packagesearch.api.v3

import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object GradleVariantSerializer : JsonContentPolymorphicSerializer<GradlePackage.Variant>(GradlePackage.Variant::class) {

    override fun selectDeserializer(element: JsonElement) = when {
        "available-at" in element.jsonObject -> GradlePackage.Variant.WithAvailableAt.serializer()
        "files" in element.jsonObject && "dependencies" in element.jsonObject -> GradlePackage.Variant.WithFiles.serializer()
        else -> error("Unable to determine the type to deserialize:\n${element.jsonObject}")
    }
}
