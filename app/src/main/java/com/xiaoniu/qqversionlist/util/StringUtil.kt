/*
    QQ Version Tool for Android™
    Copyright (C) 2023 klxiaoniu

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package com.xiaoniu.qqversionlist.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object StringUtil {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { isLenient = true; prettyPrint = true; prettyPrintIndent = "  " }

    fun String.toPrettyFormat(): String {
        return try {
            val parsedElement = Json.parseToJsonElement(this)
            val prettyJson = formatJsonElement(parsedElement)
            json.encodeToString(prettyJson)
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }

    fun String.getAllAPKUrl(): String {
        val urlPattern =
            """(?i)\b((?:https?://|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))""".toRegex()
        val urls = urlPattern.findAll(this).map { it.value }.toList()
        val apkUrls = urls.filter { it.endsWith(".apk", ignoreCase = true) }
        return if (apkUrls.isEmpty()) "" else "检测到 Android 应用安装包直链：\n\n" + apkUrls.joinToString(
            "\n\n"
        )
    }


    private fun formatJsonElement(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(element.entries.map { (key, value) ->
                key to formatJsonElement(value)
            }.toMap())

            is JsonArray -> JsonArray(element.map { formatJsonElement(it) })
            is JsonPrimitive -> {
                if (element.isString && element.content.isJson()) formatJsonElement(
                    Json.parseToJsonElement(
                        element.content
                    )
                )
                else element
            }

            else -> element
        }
    }

    private fun String.isJson(): Boolean {
        return this.startsWith("{") && this.endsWith("}") || this.startsWith("[") && this.endsWith("]")
    }
}


