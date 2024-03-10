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

    fun getDisplayFirst(context: Context, key: String, defValue: Boolean = true) =
        getSp(context).getBoolean(key, defValue)
    fun putDisplayFirst(context: Context, key: String, value: Boolean) =
        getSp(context).edit().putBoolean(key, value).apply()
}