package com.tylerthrailkill.sniper.helpers

import io.ktor.client.features.json.serializer.*
import kotlinx.serialization.json.Json

object Serialization {
    val json:Json by lazy {
        Json {
            ignoreUnknownKeys = true
        }
    }
    val ktorSerializer = KotlinxSerializer(Json {
        prettyPrint = true
        isLenient = true
    })
}
