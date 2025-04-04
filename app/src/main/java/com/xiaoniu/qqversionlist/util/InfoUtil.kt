// SPDX-License-Identifier: AGPL-3.0-or-later

/*
    Qverbow Util
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
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.SpannableString
import android.text.style.URLSpan
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.BuildConfig
import com.xiaoniu.qqversionlist.QverbowApplication
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.security.MessageDigest

object InfoUtil {
    fun showToast(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(QverbowApplication.instance, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun showToast(@StringRes textResId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(QverbowApplication.instance, textResId, Toast.LENGTH_SHORT).show()
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
    fun Context.dialogError(
        e: Exception,
        isCustomMessage: Boolean = false,
        isShowSystemNotifSetting: Boolean = false,
        isShowOfficialRepo: Boolean = false
    ) {
        val activity = findActivity()
        activity?.runOnUiThread {
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
                    } else if (isShowOfficialRepo) {
                        setNegativeButton(R.string.done, null)
                        setPositiveButton(R.string.toGitHubRelease) { _, _ ->
                            val browserIntent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/klxiaoniu/QQVersionList/releases")
                                )
                            browserIntent.apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(browserIntent)
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

    fun Context.qverbowAboutText(): SpannableString {
        return SpannableString(
            "${getString(R.string.aboutAppName)}\n\n" +
                    "${getString(R.string.version)}${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                    "${getString(R.string.aboutAuthor)}快乐小牛、有鲫雪狐\n" +
                    "${getString(R.string.aboutContributor)}Col_or、bggRGjQaUbCoE、MinaGe、zwJimRaynor\n" +
                    "${getString(R.string.aboutSpecialThanksTo)}owo233、钟路帆\n" +
                    "${getString(R.string.aboutOpenSourceRepo)}GitHub\n" +
                    "${getString(R.string.aboutGetUpdate)}GitHub Releases、Obtainium\n" +
                    "${getString(R.string.facilitateI18n)}Crowdin\n\n" +
                    "Since 2023.8.9"
        ).apply {
            listOf(
                "https://github.com/klxiaoniu" to "快乐小牛",
                "https://github.com/ArcticFoxPro" to "有鲫雪狐",
                "https://github.com/color597" to "Col_or",
                "https://github.com/bggRGjQaUbCoE" to "bggRGjQaUbCoE",
                "https://github.com/citmina" to "MinaGe",
                "https://github.com/zwJimRaynor" to "zwJimRaynor",
                "https://github.com/callng" to "owo233",
                "https://github.com/Hill-98" to "钟路帆",
                "https://github.com/klxiaoniu/QQVersionList" to "GitHub",
                "https://github.com/klxiaoniu/QQVersionList/releases" to "GitHub Releases",
                "https://github.com/klxiaoniu/QQVersionList/blob/master/ReadmeAssets/Get-it-on-Obtainium.md" to "Obtainium",
                "https://crowdin.com/project/qqversionstool" to "Crowdin"
            ).forEach { (url, text) ->
                val start = indexOf(text)
                val end = start + text.length
                setSpan(URLSpan(url), start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /**
     * 在 `Context` 中查找 `Activity`。
     *
     * 该函数通过不断地获取 `baseContext` 来遍历 `ContextWrapper` 链，直到找到一个 `Activity` 或无法继续遍历。
     * 这种方法用于获取当前上下文相关的 `Activity` 实例，以便执行某些操作。
     *
     * @return 如果找到 `Activity` 则返回该 `Activity` 实例，否则返回null。
     */
    fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    /**
     * 获取 Qverbow 的 SM3 哈希值
     * SM3 是中国国家密码管理局发布的密码杂凑算法，用于生成消息的杂凑值
     * 此函数读取应用程序的源目录，并计算该目录下 APK 文件的 SM3 杂凑值
     *
     * @return 应用程序 APK 文件的 SM3 杂凑值的十六进制字符串表示
     */
    fun Context.getQverbowSM3(): String {
        val appSourceDir = packageManager.getApplicationInfo(packageName, 0).sourceDir
        val messageDigest = MessageDigest.getInstance("SM3")
        val fileInputStream = FileInputStream(appSourceDir)
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) messageDigest.update(
            buffer, 0, bytesRead
        )
        return messageDigest.digest().joinToString("") { "%02X".format(it) }
    }

    /**
     * 使用 Chrome 自定义标签页打开URL
     *
     * 此函数构建一个 Chrome 自定义标签页的意图，并使用它来打开给定的URL
     * 它利用 Android 的 CustomTabsIntent 功能来实现这一点
     *
     * @param url 要打开的 URL 字符串
     */
    fun Context.openUrlWithChromeCustomTab(url: String){
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(this, Uri.parse(url))
    }
}