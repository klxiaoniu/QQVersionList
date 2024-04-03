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

    fun Activity.dialogError(e: Exception) {
        runOnUiThread {

            val errorDialog =
                MaterialAlertDialogBuilder(this)
                    .setTitle("程序出错，可前往 GitHub 反馈")
                    .setIcon(R.drawable.error_warning_line)
                    .setMessage(e.stackTraceToString())
                    .setPositiveButton("确定", null)
                    .setCancelable(false)
                    .setNeutralButton("复制", null)
                    .create()

            errorDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                copyText("" + e.stackTraceToString())
            }

            errorDialog.show()
        }
    }

}