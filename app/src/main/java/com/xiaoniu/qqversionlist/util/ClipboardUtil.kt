package com.xiaoniu.qqversionlist.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import androidx.appcompat.app.AppCompatActivity
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

object ClipboardUtil {
    fun Activity.copyText(text: String) {
        val clipboardManager =
            getSystemService(AppCompatActivity.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
        showToast("已复制：$text")
    }
}