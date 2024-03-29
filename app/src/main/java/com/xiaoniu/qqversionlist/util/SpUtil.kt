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

import androidx.appcompat.app.AppCompatActivity
import com.xiaoniu.qqversionlist.TipTimeApplication

object SpUtil {
    private fun getSp() =
        TipTimeApplication.instance.getSharedPreferences("data", AppCompatActivity.MODE_PRIVATE)

    fun getInt(key: String, defValue: Int = 0) =
        getSp().getInt(key, defValue)

    fun putInt(key: String, value: Int) =
        getSp().edit().putInt(key, value).apply()

    fun getString(key: String, defValue: String = "") =
        getSp().getString(key, defValue)

    fun putString(key: String, value: String) =
        getSp().edit().putString(key, value).apply()

    fun getBoolean(key: String, defValue: Boolean) =
        getSp().getBoolean(key, defValue)

    fun putBoolean(key: String, value: Boolean) =
        getSp().edit().putBoolean(key, value).apply()

    fun deleteSp(key: String) =
        getSp().edit().remove(key).apply()

}