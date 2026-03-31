package com.pointlessapps.songbook.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.typeOf

@Composable
internal inline fun <reified T : Any> rememberAnySaver(): Saver<T, String> = remember {
    object : Saver<T, String> {
        override fun SaverScope.save(value: T): String = Json.encodeToString(value)
        override fun restore(value: String): T? = Json.decodeFromString(value)
    }
}

@Composable
internal inline fun <reified T : SnapshotStateMap<*, *>> rememberSnapshotMapSaver(): Saver<T, String> =
    remember {
        val json = Json { allowStructuredMapKeys = true }
        val type = typeOf<T>()
        val keySerializer = json.serializersModule.serializer(type.arguments[0].type!!)
        val valueSerializer = json.serializersModule.serializer(type.arguments[1].type!!)
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)

        @Suppress("UNCHECKED_CAST")
        object : Saver<T, String> {
            override fun SaverScope.save(value: T): String =
                json.encodeToString(mapSerializer, (value as Map<Any?, Any?>))

            override fun restore(value: String): T =
                mutableStateMapOf<Any?, Any?>().apply {
                    putAll(json.decodeFromString(mapSerializer, value))
                } as T
        }
    }
