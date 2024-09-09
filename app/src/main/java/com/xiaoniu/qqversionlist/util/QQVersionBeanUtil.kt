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

import com.xiaoniu.qqversionlist.Application.Companion.EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.ui.MainActivity
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.DefaultArtifactVersion

object QQVersionBeanUtil {
    fun qqVersionBeanUtil(thisActivity: MainActivity, responseData: String) {
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
                    EARLIEST_ACCESSIBILITY_QQ_VERSION
                )*/

                this.isQQNTFramework =
                    DefaultArtifactVersion(this.versionNumber) >= DefaultArtifactVersion(
                        EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE
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