// SPDX-License-Identifier: AGPL-3.0-or-later

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

package com.xiaoniu.qqversionlist

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.tencent.kona.crypto.KonaCryptoProvider
import java.security.Security

class QverbowApplication : Application() {
    companion object {
        lateinit var instance: QverbowApplication

        const val SHIPLY_DEFAULT_APPID = "537230561"
        const val SHIPLY_DEFAULT_SDK_VERSION = "1.3.36-RC03"
        const val SHIPLY_APPID_QQ = "4cd6974be1"
        const val SHIPLY_APPID_TIM = "ad6b501b0e"
        const val SHIPLY_SIGN_ID_QQ = "0ccc46ca-154c-4c6b-8b0b-4d8537ffcbcc"
        const val SHIPLY_SIGN_ID_TIM = "33641818-aee7-445a-82d4-b7d0bce3a85a"

        const val ANDROID_QQ_PACKAGE_NAME = "com.tencent.mobileqq"
        const val ANDROID_TIM_PACKAGE_NAME = "com.tencent.tim"
        const val ANDROID_WECHAT_PACKAGE_NAME = "com.tencent.mm"
        const val ANDROID_WECOM_PACKAGE_NAME = "com.tencent.wework"
        const val ANDROID_WETYPE_PACKAGE_NAME = "com.tencent.wetype"
        const val ANDROID_QIDIAN_PACKAGE_NAME = "com.tencent.qidian"

        const val EARLIEST_ACCESSIBILITY_QQ_VERSION = false
        const val EARLIEST_ACCESSIBILITY_TIM_VERSION = false
        const val EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE = "8.9.63"
        const val EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE = "4.0.0"
        const val EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE = "8.8.55"
        const val EARLIEST_KUIKLY_FRAMEWORK_QQ_VERSION_STABLE = "8.9.50"
        const val EARLIEST_KUIKLY_FRAMEWORK_TIM_VERSION_STABLE = "4.0.0"

        const val ZHIPU_TOKEN = "ZhipuAIMaaSPlatformToken"
        const val GITHUB_TOKEN = "GitHubPersonalAccessToken"
    }

    override fun onCreate() {
        // Android 12+ 动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
        super.onCreate()
        Security.addProvider(KonaCryptoProvider()) // 腾讯 Kona 国密套件需要在应用启动时添加 Provider
    }
}