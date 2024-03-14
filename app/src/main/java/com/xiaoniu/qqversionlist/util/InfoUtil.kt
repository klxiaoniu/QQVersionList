package com.xiaoniu.qqversionlist.util

import android.app.Activity
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText

object InfoUtil {
    fun Activity.showToast(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun Activity.dialogError(e: Exception) {
        runOnUiThread {
            MaterialAlertDialogBuilder(this).setTitle("程序出错，联系小牛")
                .setIcon(R.drawable.error_warning_line).setMessage(e.toString())
                .setPositiveButton("确定", null).setNeutralButton("复制") { _, _ ->
                    copyText(e.toString())
                }.show()
        }
    }

}