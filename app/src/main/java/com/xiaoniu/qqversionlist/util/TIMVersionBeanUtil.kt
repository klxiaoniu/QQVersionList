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

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

object TIMVersionBeanUtil {
    fun timVersionBeanProcessor(thisActivity: MainActivity, responseData: String) {
        val start = (responseData.indexOf("var params= ")) + 12
        val end = (responseData.indexOf(";\n" + "      typeof"))
        val jsonString = responseData.substring(start, end)
        val gson = Gson()
        val jsonData = gson.fromJson(jsonString, JsonObject::class.java)

        thisActivity.timVersion = mutableListOf()

        val download = jsonData.getAsJsonObject("app").getAsJsonObject("download")
        val androidVersion = download.get("androidVersion").asString
        val androidDatetime = download.get("androidDatetime").asString

        (thisActivity.timVersion as MutableList<TIMVersionBean>).add(
            TIMVersionBean(
                version = androidVersion,
                datetime = androidDatetime,
                fix = "",
                new = "",
                jsonString = gson.toJson(JsonObject().apply {
                    addProperty("version", androidVersion)
                    addProperty("datetime", androidDatetime)
                    addProperty("fix", "")
                    addProperty("new", "")
                }).toString(),
                displayInstall = (DataStoreUtil.getStringKV(
                    "TIMVersionInstall",
                    ""
                ) == androidVersion)
            )
        )

        // 从 latest 项中获取 Android 版本
        val latest = jsonData.getAsJsonObject("app").getAsJsonArray("latest")
        latest.forEach { item ->
            val platform = item.asJsonObject.get("platform").asString
            if (platform == "Android") {
                val version = item.asJsonObject.get("version").asString
                val datetime = item.asJsonObject.get("datetime").asString
                val fix = item.asJsonObject.get("fix").asString
                val newFeature = item.asJsonObject.get("new").asString

                (thisActivity.timVersion as MutableList<TIMVersionBean>).add(
                    TIMVersionBean(
                        version = version,
                        datetime = datetime,
                        fix = fix,
                        new = newFeature,
                        jsonString = gson.toJson(JsonObject().apply {
                            addProperty("version", version)
                            addProperty("datetime", datetime)
                            addProperty("fix", fix)
                            addProperty("new", newFeature)
                        }).toString(),
                        displayInstall = (DataStoreUtil.getStringKV(
                            "QQVersionInstall",
                            ""
                        ) == version)
                    )
                )
            }
        }

        // 从 history 项中获取 Android 版本
        val history = jsonData.getAsJsonObject("app").getAsJsonArray("history")
        history.forEach { versionItem ->
            val version = versionItem.asJsonObject.get("version").asString
            val logs = versionItem.asJsonObject.getAsJsonArray("logs")
            logs.forEach { logItem ->
                val platform = logItem.asJsonObject.get("platform").asString
                if (platform == "Android") {
                    val datetime = logItem.asJsonObject.get("datetime").asString
                    val fix = logItem.asJsonObject.get("fix").asString
                    val newFeature = logItem.asJsonObject.get("new").asString

                    (thisActivity.timVersion as MutableList<TIMVersionBean>).add(
                        TIMVersionBean(
                            version = version,
                            datetime = datetime,
                            fix = fix,
                            new = newFeature,
                            jsonString = gson.toJson(JsonObject().apply {
                                addProperty("version", version)
                                addProperty("datetime", datetime)
                                addProperty("fix", fix)
                                addProperty("new", newFeature)
                            }).toString(),
                            displayInstall = (DataStoreUtil.getStringKV(
                                "QQVersionInstall",
                                ""
                            ) == version)
                        )
                    )
                }
            }
        }

        // 去除重复的版本号
        thisActivity.timVersion = thisActivity.timVersion.distinctBy { it.jsonString.toPrettyFormat() }

        if (DataStoreUtil.getBooleanKV(
                "displayFirst", true
            )
        ) thisActivity.timVersion[0].displayType = 1
        DataStoreUtil.putStringKVAsync(
            "TIMVersionBig", thisActivity.timVersion.first().version
        )
    }
}