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

import android.content.Context
import androidx.appcompat.app.AppCompatActivity

object SpUtil {
    private fun getSp(context: Context) =
        context.getSharedPreferences("data", AppCompatActivity.MODE_PRIVATE)

    fun getInt(context: Context, key: String, defValue: Int = 0) =
        getSp(context).getInt(key, defValue)

    fun putInt(context: Context, key: String, value: Int) =
        getSp(context).edit().putInt(key, value).apply()

    fun getString(context: Context, key: String, defValue: String = "") =
        getSp(context).getString(key, defValue)

    fun putString(context: Context, key: String, value: String) =
        getSp(context).edit().putString(key, value).apply()

    fun getBoolean(context: Context, key: String, defValue: Boolean = true) =
        getSp(context).getBoolean(key, defValue)

    fun putBoolean(context: Context, key: String, value: Boolean) =
        getSp(context).edit().putBoolean(key, value).apply()

    fun deleteSp(context: Context, key: String) =
        getSp(context).edit().remove(key).apply()

}