package com.xiaoniu.qqversionlist

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

class Util {
    companion object {
        fun String.getVersionBig(): String {
            val regex = Regex("\"versionNumber\":\"(.+?)\"")
            val vName = regex.find(this)!!.groupValues[1]
            return vName
        }

        fun String.getSize(): String {
            val regex = Regex("\"size\":\"(.+?)\"")
            val size = regex.find(this)!!.groupValues[1]
            return size
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
}