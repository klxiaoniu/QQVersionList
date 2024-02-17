package com.xiaoniu.qqversionlist

import android.app.Application
import androidx.core.provider.FontRequest
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors

class TipTimeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Android 12+ 动态颜色
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}