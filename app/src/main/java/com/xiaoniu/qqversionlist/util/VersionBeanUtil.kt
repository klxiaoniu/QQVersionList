/*
    Qverbow Util
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
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.ComparableVersion

object VersionBeanUtil {
    fun resolveQQRainbow(thisActivity: MainActivity, responseData: String) {
        val start = (responseData.indexOf("versions64\":[")) + 12
        val end = (responseData.indexOf(";\n" + "      typeof"))
        val totalJson = responseData.substring(start, end)
        thisActivity.qqVersion = totalJson.split("},{").reversed().map {
            val pstart = it.indexOf("{\"versions")
            val pend = it.indexOf(",\"length")
            val json = it.substring(pstart, pend)
            Json.decodeFromString<QQVersionBean>(json).apply {
                jsonString = json
                // 标记本机 Android QQ 版本
                this.apply {
                    displayInstall =
                        (DataStoreUtil.getStringKV("QQVersionInstall", "") == versionNumber)
                    isAccessibility = false
                    // 无障碍标记
                    /*ComparableVersion(versionNumber) >= ComparableVersion(
                        EARLIEST_ACCESSIBILITY_QQ_VERSION
                    )*/

                    isQQNTFramework = ComparableVersion(versionNumber) >= ComparableVersion(
                        EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
                    )
                    isUnrealEngine = ComparableVersion(versionNumber) >= ComparableVersion(
                        EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE
                    )
                }
            }
        }
        if (DataStoreUtil.getBooleanKV(
                "displayFirst", true
            )
        ) thisActivity.qqVersion[0].displayType = 1
        // 舍弃 currentQQVersion = qqVersion.first().versionNumber
        // 大版本号也放持久化存储了，否则扫版 Shortcut 因为加载过快而获取不到东西
        DataStoreUtil.putStringKVAsync(
            "versionBig", thisActivity.qqVersion.first().versionNumber
        )
    }

    fun resolveTIMRainbow(thisActivity: MainActivity, responseData: String) {
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
                ) == androidVersion),
                isQQNTFramework = ComparableVersion(androidVersion) >= ComparableVersion(
                    EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
                )
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
                        ) == version),
                        isQQNTFramework = ComparableVersion(version) >= ComparableVersion(
                            EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
                        )
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
                            ) == version),
                            isQQNTFramework = ComparableVersion(version) >= ComparableVersion(
                                EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
                            )
                        )
                    )
                }
            }
        }

        // 去除重复的版本号
        thisActivity.timVersion =
            thisActivity.timVersion.distinctBy { it.jsonString.toPrettyFormat() }
        if (thisActivity.timVersion[0].version == thisActivity.timVersion[1].version && thisActivity.timVersion[0].fix == "") (thisActivity.timVersion as MutableList<TIMVersionBean>).removeAt(
            0
        )

        if (DataStoreUtil.getBooleanKV(
                "displayFirst",
                true
            )
        ) thisActivity.timVersion[0].displayType = 1
        DataStoreUtil.putStringKVAsync(
            "TIMVersionBig", thisActivity.timVersion.first().version
        )
    }
}