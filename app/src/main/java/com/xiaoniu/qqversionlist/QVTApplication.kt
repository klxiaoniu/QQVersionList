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

package com.xiaoniu.qqversionlist

import android.app.Application
import com.google.android.material.color.DynamicColors

class QVTApplication : Application() {
    companion object {
        lateinit var instance: QVTApplication

        const val SHIPLY_DEFAULT_APPID = "537230561"
        const val SHIPLY_DEFAULT_SDK_VERSION = "1.3.36-RC03"

        const val EARLIEST_ACCESSIBILITY_QQ_VERSION = false
        const val EARLIEST_ACCESSIBILITY_TIM_VERSION = false
        const val EARLIEST_QQNT_FRAMEWORK_QQ_VERSION_STABLE = "8.9.63"
        const val EARLIEST_QQNT_FRAMEWORK_TIM_VERSION_STABLE = false
        const val EARLIEST_UNREAL_ENGINE_QQ_VERSION_STABLE = "8.8.55"
    }

    override fun onCreate() {
        // Android 12+ 动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
        super.onCreate()
    }
}