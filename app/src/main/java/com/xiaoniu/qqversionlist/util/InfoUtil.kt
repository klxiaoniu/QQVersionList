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

    fun Activity.dialogError(e: Exception, isCustomMessage: Boolean = false) {
        runOnUiThread {
            val message = if (isCustomMessage) e.message else buildString {
                appendLine("如需反馈，请前往 GitHub 仓库报告 Issue(s) 并随附以下信息：\n")
                appendLine(e.stackTraceToString())
            }

            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.applicationError)
                .setIcon(R.drawable.alert_line)
                .setPositiveButton(R.string.done, null)
                .setCancelable(false)
                .setNeutralButton(R.string.copy, null)
                .setMessage(message)
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