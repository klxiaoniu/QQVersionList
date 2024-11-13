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

package com.xiaoniu.qqversionlist.util

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.QVTApplication
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object InfoUtil {
    fun showToast(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(QVTApplication.instance, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(@StringRes textResId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(QVTApplication.instance, textResId, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 在 Activity 中显示错误对话框
     *
     * 此函数用于在应用程序中向用户显示错误信息对话框它可以在对话框中显示自定义的错误消息，
     * 或者显示从异常对象中获取的堆栈跟踪信息对话框包含一个“完成”按钮和一个“复制”按钮，
     * 用户可以通过“复制”按钮来复制错误信息
     *
     * 如何使用：
     * 1. 将此函数作为 Activity 的一部分进行调用。
     * 2. 传入一个 Exception 实例，通常是在捕获异常时传递。
     * 3. 可选地，设置 `isCustomMessage` 为 `true` 以将其作为自定义错误消息而不显示跟踪堆栈。
     *
     * @param e 异常对象，包含了错误的信息和堆栈跟踪
     * @param isCustomMessage 布尔值，表示是否使用自定义的错误消息（不在前台显示跟踪堆栈），默认为 false
     * @param isShowSystemNotifSetting 布尔值，表示是否显示前往系统通知设置按钮，默认为 false
     */
    fun Activity.dialogError(
        e: Exception,
        isCustomMessage: Boolean = false,
        isShowSystemNotifSetting: Boolean = false
    ) {
        runOnUiThread {
            val message = if (isCustomMessage) e.message else buildString {
                appendLine("如需反馈，请前往 GitHub 仓库报告 Issue(s) 并随附以下信息：\n")
                appendLine(e.stackTraceToString())
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.applicationError)
                .setIcon(R.drawable.alert_line)
                .setCancelable(false)
                .setNeutralButton(R.string.copy, null)
                .setMessage(message)
                .apply {
                    if (isShowSystemNotifSetting && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        setNegativeButton(R.string.done, null)
                        setPositiveButton(R.string.toSystemSetting) { _, _ ->
                            val intent = Intent()
                            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                            intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                            startActivity(intent)
                        }
                    } else setPositiveButton(R.string.done, null)
                }
                .create()
                .apply {
                    setOnShowListener {
                        getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                            if (isCustomMessage) e.message?.let { it1 -> copyText(it1) }
                            else copyText(e.stackTraceToString())
                        }
                    }
                    show()
                }
        }
    }
}