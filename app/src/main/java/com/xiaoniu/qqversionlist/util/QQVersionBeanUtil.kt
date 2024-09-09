package com.xiaoniu.qqversionlist.util

import com.xiaoniu.qqversionlist.TipTimeApplication.Companion.EARLIEST_QQNT_FRAMEWORK_VERSION_STABLE
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object QQVersionBeanUtil {
    fun qqVersionBeanUtil(thisActivity: MainActivity, responseData: String){
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
                this.displayInstall = (DataStoreUtil.getString(
                    "QQVersionInstall", ""
                ) == this.versionNumber)
                this.isAccessibility = false
                // 无障碍标记
                /*DefaultArtifactVersion(this.versionNumber) >= DefaultArtifactVersion(
                    EARLIEST_ACCESSIBILITY_VERSION
                )*/

                this.isQQNTFramework =
                    DefaultArtifactVersion(this.versionNumber) >= DefaultArtifactVersion(
                        EARLIEST_QQNT_FRAMEWORK_VERSION_STABLE
                    )
            }
        }
        if (DataStoreUtil.getBoolean(
                "displayFirst", true
            )
        ) thisActivity.qqVersion[0].displayType = 1
        // 舍弃 currentQQVersion = qqVersion.first().versionNumber
        // 大版本号也放持久化存储了，否则猜版 Shortcut 因为加载过快而获取不到东西
        DataStoreUtil.putStringAsync(
            "versionBig", thisActivity.qqVersion.first().versionNumber
        )
    }
}