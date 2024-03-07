package com.xiaoniu.qqversionlist.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

object StringUtil {
    fun String.toPrettyFormat(): String {
        return try {
            val jsonObject = JsonParser.parseString(this).asJsonObject
            val gson = GsonBuilder().setLenient().setPrettyPrinting().create()
            gson.toJson(jsonObject)
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }

}