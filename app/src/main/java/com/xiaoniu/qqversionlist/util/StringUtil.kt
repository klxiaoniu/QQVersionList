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

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.Strictness

object StringUtil {
    private val gsonBuilder = GsonBuilder().setStrictness(Strictness.LENIENT).setPrettyPrinting()
    fun String.toPrettyFormat(): String {
        return try {
            val jsonObject = JsonParser.parseString(this).asJsonObject
            gsonBuilder.create().toJson(jsonObject)
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }
}