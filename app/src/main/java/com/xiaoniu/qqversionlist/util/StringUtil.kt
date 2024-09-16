/*
    QQ Versions Tool for Android™
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

    /**
     * 将当前字符串转换为格式化的 JSON 字符串。
     *
     * 此函数尝试将当前字符串解析为 JSON 元素，并将其格式化为更易读的格式。
     * 如果解析或格式化过程中发生异常，则返回原始字符串。
     *
     * @return 格式化后的 JSON 字符串，如果解析失败则返回原始字符串。
     */
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

    /**
     * 从字符串中提取所有以 `.apk` 结尾的 URL 链接
     *
     * @return 返回一个包含所有提取到的 `.apk` 文件 URL 链接的列表如果未找到任何 `.apk` 链接，则返回 null
     */
    fun String.getAllAPKUrl(): List<String>? {
        val urlPattern =
            """(?i)\b((?:https?://|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’]))""".toRegex()
        val urls = urlPattern.findAll(this).map { it.value }.toList()
        val apkUrls = urls.filter { it.endsWith(".apk", ignoreCase = true) }.toSet().toList()
        return if (apkUrls.isEmpty()) null else apkUrls
    }

    /**
     * 格式化 Json 元素的私有方法
     * 此方法用于递归地格式化 Json 对象、数组和原始类型的元素
     * 对于字符串类型的 Json 原始元素，如果其内容本身就是有效的 Json，也会进行格式化
     *
     * @param element 要格式化的 Json 元素
     * @return 格式化后的 Json 元素
     */
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

    /**
     * 判断字符串是否为 JSON 格式
     *
     * JSON(JavaScript Object Notation)是一种轻量级的数据交换格式，常见于 Web 服务和客户端之间的数据传输
     * 本函数通过检查字符串是否以左大括号'{'或左方括号'['开始，并以相应的右括号'}'或']'结束，
     * 来简单判断字符串是否为 JSON 格式。这种方法仅适用于格式非常标准且结构简单的JSON字符串的初步判断，
     * **对于复杂的 JSON 结构或包含转义字符的情况则不适用**。
     *
     * @return 如果字符串为 JSON 格式，则返回 true；否则返回 false
     */
    private fun String.isJson(): Boolean {
        return this.startsWith("{") && this.endsWith("}") || this.startsWith("[") && this.endsWith("]")
    }

    /**
     * 修剪字符串末尾的指定后缀。
     *
     * 该函数接受两个参数：一个原始字符串`str`和一个需要修剪的后缀字符串`suffix`。
     * 如果原始字符串`str`的末尾包含了后缀字符串`suffix`，则将这部分后缀字符串修剪掉；
     * 否则，返回原始字符串`str`。
     *
     * @param str 原始字符串。
     * @param suffix 需要修剪的后缀字符串。
     * @return 修剪掉末尾后缀后的字符串，如果原始字符串没有以该后缀结束，则返回原始字符串。
     */
    fun String.trimSubstringAtEnd(suffix: String): String {
        return if (this.endsWith(suffix)) this.substring(0, this.length - suffix.length) else this
    }

    /**
     * 修剪字符串开头的指定前缀。
     *
     * 该函数接受两个参数：一个原始字符串`str`和一个需要修剪的前缀字符串`prefix`。
     * 如果原始字符串`str`的开头包含了前缀字符串`prefix`，则将这部分前缀字符串修剪掉；
     * 否则，返回原始字符串`str`。
     *
     * @param prefix 需要修剪的前缀字符串。
     * @return 修剪掉开头前缀后的字符串，如果原始字符串没有以该前缀开始，则返回原始字符串。
     */
    fun String.trimSubstringAtStart(prefix: String): String {
        return if (this.startsWith(prefix)) this.substring(prefix.length) else this
    }
}


