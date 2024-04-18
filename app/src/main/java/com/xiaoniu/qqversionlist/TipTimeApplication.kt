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

package com.xiaoniu.qqversionlist

import android.app.Application
import com.google.android.material.color.DynamicColors


class TipTimeApplication : Application() {
    companion object {
        lateinit var instance: TipTimeApplication
    }

    override fun onCreate() {
        // Android 12+ 动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this)
        instance = this
        super.onCreate()
        /*
        val rootDir = MMKV.initialize(this)
        println("mmkv root: $rootDir")

        if (!MMKVUtil.getBoolean("migration_completed", false)) {
              MMKVUtil.importSPToMMKV()
              MMKVUtil.putBoolean("migration_completed", true) // 设置迁移完成标志
        }*/
    }
}