package com.ahmetyildiz.quakealert.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class StringMapConverter {

    @TypeConverter
    fun fromMap(map: Map<String, String>): String = Json.encodeToString(map)

    @TypeConverter
    fun toMap(json: String): Map<String, String> = Json.decodeFromString(json)
}
