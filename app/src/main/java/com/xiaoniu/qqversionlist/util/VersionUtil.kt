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
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_QQ_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_TIM_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_WECHAT_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_KUIKLY_FRAMEWORK_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_KUIKLY_FRAMEWORK_TIM_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.data.WeixinVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import com.xiaoniu.qqversionlist.util.StringUtil.getQua
import com.xiaoniu.qqversionlist.util.StringUtil.jsonArrayToList
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.ComparableVersion
import org.jsoup.Jsoup

object VersionUtil {
    fun resolveQQRainbow(thisActivity: MainActivity, responseData: String) {
        val start = (responseData.indexOf("versions64\":[")) + 12
        val end = (responseData.indexOf(";\n" + "      typeof"))
        val totalJson = responseData.substring(start, end)
        thisActivity.qqVersion = totalJson.split("},{").reversed().map {
            val pstart = it.indexOf("{\"versions")
            val pend = it.indexOf(",\"length")
            val json = it.substring(pstart, pend)
            val qqVersionInstall = DataStoreUtil.getStringKV("QQVersionInstall", "")
            Json.decodeFromString<QQVersionBean>(json).apply {
                jsonString = json
                // 标记本机 Android QQ 版本
                this.apply {
                    displayInstall =
                        ComparableVersion(qqVersionInstall) == ComparableVersion(versionNumber)
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
                    isKuiklyInside = ComparableVersion(versionNumber) >= ComparableVersion(
                        EARLIEST_KUIKLY_FRAMEWORK_QQ_VERSION_STABLE
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
        val gson = Gson()
        val jsonData = gson.fromJson(responseData, JsonObject::class.java)

        thisActivity.timVersion = mutableListOf()

        val androidLink = jsonData.get("download_link").asJsonObject.get("android").asString

        // 从 `version_history` 项中获取 Android 版本
        val history = jsonData.getAsJsonArray("version_history")
        history.forEach { versionItem ->
            val version = versionItem.asJsonObject.get("version_code").asString
            val logs = versionItem.asJsonObject.getAsJsonArray("logs")
            val timVersionInstall = DataStoreUtil.getStringKV("TIMVersionInstall", "")
            logs.forEach { logItem ->
                val platform = logItem.asJsonObject.get("platform").asString
                if (platform == "android") {
                    val datetime = logItem.asJsonObject.get("datetime").asString
                    val fix = logItem.asJsonObject.get("fix").asJsonArray
                    val feature = logItem.asJsonObject.get("feature").asJsonArray

                    (thisActivity.timVersion as MutableList<TIMVersionBean>).add(
                        TIMVersionBean(
                            version = version,
                            datetime = datetime,
                            fix = jsonArrayToList(fix),
                            feature = jsonArrayToList(feature),
                            jsonString = gson.toJson(JsonObject().apply {
                                addProperty("version_code", version)
                                addProperty("datetime", datetime)
                                addProperty("fix", fix.toString())
                                addProperty("feature", feature.toString())
                            }).toString(),
                            displayInstall = ComparableVersion(timVersionInstall) == ComparableVersion(
                                version
                            ),
                            isQQNTFramework = ComparableVersion(version) >= ComparableVersion(
                                EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE
                            ),
                            isKuiklyInside = ComparableVersion(version) >= ComparableVersion(
                                EARLIEST_KUIKLY_FRAMEWORK_TIM_VERSION_STABLE
                            )
                        )
                    )
                }
            }
        }

        thisActivity.timVersion[0].link = androidLink
        thisActivity.timVersion[0].jsonString = gson.toJson(JsonObject().apply {
            addProperty("version_code", thisActivity.timVersion[0].version)
            addProperty("datetime", thisActivity.timVersion[0].datetime)
            addProperty("fix", thisActivity.timVersion[0].fix.toString())
            addProperty("feature", thisActivity.timVersion[0].feature.toString())
            addProperty("link", androidLink)
        }).toString()

        if (DataStoreUtil.getBooleanKV(
                "displayFirst", true
            )
        ) thisActivity.timVersion[0].displayType = 1
        DataStoreUtil.putStringKVAsync(
            "TIMVersionBig", thisActivity.timVersion.first().version
        )
    }

    fun resolveWeixinHTML(thisActivity: MainActivity, responseData: String) {
        val document = Jsoup.parse(responseData)
        val androidSection = document.selectFirst("section#android")
        val versions = mutableListOf<WeixinVersionBean>()
        val weixinVersionInstall = DataStoreUtil.getStringKV("WeixinVersionInstall", "")
        if (androidSection != null) {
            val versionItems = androidSection.select("li.faq_section_sublist_item")

            for (item in versionItems) {
                val versionElement = item.selectFirst("span.version")
                val dateElement = item.selectFirst("span:not(.version)")

                if (versionElement != null && dateElement != null) {
                    val version = versionElement.text().trim()
                    val publishDate = dateElement.text().trim().replace("(", "").replace(")", "")
                    versions.add(
                        WeixinVersionBean(
                            version,
                            publishDate,
                            false,
                            ComparableVersion(weixinVersionInstall) == ComparableVersion(version)
                        )
                    )
                }
            }
        }
        thisActivity.weixinVersion = versions

        if (DataStoreUtil.getBooleanKV(
                "displayFirst", true
            )
        ) thisActivity.weixinVersion.first().displayType = 1
        DataStoreUtil.putStringKVAsync(
            "WeixinVersionBig", thisActivity.weixinVersion.first().version
        )
    }

    fun Context.resolveLocalQQ() {
        // 识别本机 Android QQ 版本并放进持久化存储
        val QQPackageInfo = packageManager.getPackageInfo(ANDROID_QQ_PACKAGE_NAME, 0)
        val QQVersionInstall = QQPackageInfo.versionName.toString()
        val QQVersionCodeInstall =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) QQPackageInfo.longVersionCode.toString() else ""
        val QQMetaDataInstall = packageManager.getPackageInfo(
            ANDROID_QQ_PACKAGE_NAME, PackageManager.GET_META_DATA
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
        val TIMPackageInfo = packageManager.getPackageInfo(ANDROID_TIM_PACKAGE_NAME, 0)
        val TIMVersionInstall = TIMPackageInfo.versionName.toString()
        val TIMVersionCodeInstall =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) TIMPackageInfo.longVersionCode.toString() else ""
        val TIMMetaDataInstall = packageManager.getPackageInfo(
            ANDROID_TIM_PACKAGE_NAME, PackageManager.GET_META_DATA
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

    fun Context.resolveLocalWeixin() {
        // 识别本机 Android 微信版本并放进持久化存储
        val weixinPackageInfo = packageManager.getPackageInfo(ANDROID_WECHAT_PACKAGE_NAME, 0)
        val weixinVersionInstall = weixinPackageInfo.versionName.toString()
        val weixinVersionCodeInstall =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) weixinPackageInfo.longVersionCode.toString() else ""
        if (weixinVersionInstall != DataStoreUtil.getStringKV(
                "WeixinVersionInstall", ""
            )
        ) DataStoreUtil.putStringKV("WeixinVersionInstall", weixinVersionInstall)
        if (weixinVersionCodeInstall != DataStoreUtil.getStringKV(
                "WeixinVersionCodeInstall", ""
            )
        ) DataStoreUtil.putStringKV("WeixinVersionCodeInstall", weixinVersionCodeInstall)
    }
}