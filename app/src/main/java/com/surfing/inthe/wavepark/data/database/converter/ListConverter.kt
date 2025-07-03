package com.surfing.inthe.wavepark.data.database.converter

import androidx.room.TypeConverter

class ListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",")
    }

    @TypeConverter
    fun listToString(list: List<String>): String {
        return list.joinToString(",")
    }
}