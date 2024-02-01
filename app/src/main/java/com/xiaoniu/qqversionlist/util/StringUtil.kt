package com.xiaoniu.qqversionlist.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

object StringUtil {
    fun String.getVersionBig(): String {
        val regex = Regex("\"versionNumber\":\"(.+?)\"")
        return regex.find(this)!!.groupValues[1]
    }

    fun String.getSize(): String {
        val regex = Regex("\"size\":\"(.+?)\"")
        return regex.find(this)!!.groupValues[1]
    }

    fun String.toPrettyFormat(): String {
        return try {
            val jsonParser = JsonParser()
            val jsonObject = jsonParser.parse(this).asJsonObject
            val gson = GsonBuilder().setLenient().setPrettyPrinting().create()
            gson.toJson(jsonObject)
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }

}