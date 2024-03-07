package com.xiaoniu.qqversionlist.util

import android.util.Log

object LogUtil {
    fun Any.log(): Any {
        Log.i("QQVersionList", this.toString())
        return this
    }
}