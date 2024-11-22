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

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import com.xiaoniu.qqversionlist.util.StringUtil.getQua
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.ComparableVersion

object VersionUtil {
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
                    "TIMVersionInstall", ""
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
                            "TIMVersionInstall", ""
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
                                "TIMVersionInstall", ""
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
                "displayFirst", true
            )
        ) thisActivity.timVersion[0].displayType = 1
        DataStoreUtil.putStringKVAsync(
            "TIMVersionBig", thisActivity.timVersion.first().version
        )
    }

    fun Context.resolveLocalQQ() {
        // 识别本机 Android QQ 版本并放进持久化存储
        val QQPackageInfo = packageManager.getPackageInfo("com.tencent.mobileqq", 0)
        val QQVersionInstall = QQPackageInfo.versionName.toString()
        val QQVersionCodeInstall =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) QQPackageInfo.longVersionCode.toString() else ""
        val QQMetaDataInstall = packageManager.getPackageInfo(
            "com.tencent.mobileqq", PackageManager.GET_META_DATA
        )
        val QQAppSettingParamsInstall =
            QQMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params")
        val QQAppSettingParamsPadInstall =
            QQMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params_pad")
        val QQRdmUUIDInstall =
            QQMetaDataInstall.applicationInfo?.metaData?.getString("com.tencent.rdm.uuid")
        val QQTargetInstall = QQMetaDataInstall.applicationInfo?.targetSdkVersion.toString()
        val QQMinInstall = QQMetaDataInstall.applicationInfo?.minSdkVersion.toString()
        val QQCompileInstall =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) QQMetaDataInstall.applicationInfo?.compileSdkVersion.toString() else "")
        val QQQua = getQua(QQPackageInfo)
        if (QQVersionInstall != DataStoreUtil.getStringKV(
                "QQVersionInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQVersionInstall", QQVersionInstall)
        if (QQVersionCodeInstall != DataStoreUtil.getStringKV(
                "QQVersionCodeInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQVersionCodeInstall", QQVersionCodeInstall)
        if (QQAppSettingParamsInstall != null && QQAppSettingParamsInstall != DataStoreUtil.getStringKV(
                "QQAppSettingParamsInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQAppSettingParamsInstall", QQAppSettingParamsInstall)
        if (QQAppSettingParamsPadInstall != null && QQAppSettingParamsPadInstall != DataStoreUtil.getStringKV(
                "QQAppSettingParamsPadInstall", ""
            )
        ) DataStoreUtil.putStringKV(
            "QQAppSettingParamsPadInstall", QQAppSettingParamsPadInstall
        )
        if (QQRdmUUIDInstall != null && QQRdmUUIDInstall != DataStoreUtil.getStringKV(
                "QQRdmUUIDInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQRdmUUIDInstall", QQRdmUUIDInstall)
        if (QQTargetInstall.isNotEmpty() && QQTargetInstall != DataStoreUtil.getStringKV(
                "QQTargetInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQTargetInstall", QQTargetInstall)
        if (QQMinInstall.isNotEmpty() && QQMinInstall != DataStoreUtil.getStringKV(
                "QQMinInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQMinInstall", QQMinInstall)
        if (QQCompileInstall.isNotEmpty() && QQCompileInstall != DataStoreUtil.getStringKV(
                "QQCompileInstall", ""
            )
        ) DataStoreUtil.putStringKV("QQCompileInstall", QQCompileInstall)
        if (QQQua != null && QQQua.replace("\n", "") != DataStoreUtil.getStringKV(
                "QQQua", ""
            )
        ) DataStoreUtil.putStringKV("QQQua", QQQua.replace("\n", ""))
    }

    fun Context.resolveLocalTIM() {
        // 识别本机 Android TIM 版本并放进持久化存储
        val TIMPackageInfo = packageManager.getPackageInfo("com.tencent.tim", 0)
        val TIMVersionInstall = TIMPackageInfo.versionName.toString()
        val TIMVersionCodeInstall =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) TIMPackageInfo.longVersionCode.toString() else ""
        val TIMMetaDataInstall = packageManager.getPackageInfo(
            "com.tencent.tim", PackageManager.GET_META_DATA
        )
        val TIMAppSettingParamsInstall =
            TIMMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params")
        val TIMAppSettingParamsPadInstall =
            TIMMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params_pad")
        val TIMRdmUUIDInstall =
            TIMMetaDataInstall.applicationInfo?.metaData?.getString("com.tencent.rdm.uuid")
        val TIMTargetInstall = TIMMetaDataInstall.applicationInfo?.targetSdkVersion.toString()
        val TIMMinInstall = TIMMetaDataInstall.applicationInfo?.minSdkVersion.toString()
        val TIMCompileInstall =
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) TIMMetaDataInstall.applicationInfo?.compileSdkVersion.toString() else "")
        val TIMQua = getQua(TIMPackageInfo)
        if (TIMTargetInstall.isNotEmpty() && TIMTargetInstall != DataStoreUtil.getStringKV(
                "TIMTargetInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMTargetInstall", TIMTargetInstall)
        if (TIMMinInstall.isNotEmpty() && TIMMinInstall != DataStoreUtil.getStringKV(
                "TIMMinInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMMinInstall", TIMMinInstall)
        if (TIMCompileInstall.isNotEmpty() && TIMCompileInstall != DataStoreUtil.getStringKV(
                "TIMCompileInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMCompileInstall", TIMCompileInstall)
        if (TIMVersionInstall != DataStoreUtil.getStringKV(
                "TIMVersionInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMVersionInstall", TIMVersionInstall)
        if (TIMVersionCodeInstall != DataStoreUtil.getStringKV(
                "TIMVersionCodeInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMVersionCodeInstall", TIMVersionCodeInstall)
        if (TIMAppSettingParamsInstall != null && TIMAppSettingParamsInstall != DataStoreUtil.getStringKV(
                "TIMAppSettingParamsInstall", ""
            )
        ) DataStoreUtil.putStringKV(
            "TIMAppSettingParamsInstall", TIMAppSettingParamsInstall
        )
        if (TIMAppSettingParamsPadInstall != null && TIMAppSettingParamsPadInstall != DataStoreUtil.getStringKV(
                "TIMAppSettingParamsPadInstall", ""
            )
        ) DataStoreUtil.putStringKV(
            "TIMAppSettingParamsPadInstall", TIMAppSettingParamsPadInstall
        )
        if (TIMRdmUUIDInstall != null && TIMRdmUUIDInstall != DataStoreUtil.getStringKV(
                "TIMRdmUUIDInstall", ""
            )
        ) DataStoreUtil.putStringKV("TIMRdmUUIDInstall", TIMRdmUUIDInstall)
        if (TIMQua != null && TIMQua.replace("\n", "") != DataStoreUtil.getStringKV(
                "TIMQua", ""
            )
        ) DataStoreUtil.putStringKV("TIMQua", TIMQua.replace("\n", ""))
    }
}