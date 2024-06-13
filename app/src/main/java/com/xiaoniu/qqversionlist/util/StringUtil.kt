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

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object StringUtil {
    private val json = Json { isLenient = true; prettyPrint = true }

    fun String.toPrettyFormat(): String {
        return try {
            val jsonObject = Json.parseToJsonElement(this) as? JsonObject
            jsonObject?.let { json.encodeToString(it) } ?: this
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }
}


