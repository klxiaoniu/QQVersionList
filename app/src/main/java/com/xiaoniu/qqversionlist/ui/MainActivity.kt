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

package com.xiaoniu.qqversionlist.ui


import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.URLSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.Gson
import com.xiaoniu.qqversionlist.BuildConfig
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.databinding.DialogGuessBinding
import com.xiaoniu.qqversionlist.databinding.DialogLoadingBinding
import com.xiaoniu.qqversionlist.databinding.DialogSettingBinding
import com.xiaoniu.qqversionlist.databinding.SuccessButtonBinding
import com.xiaoniu.qqversionlist.databinding.UserAgreementBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var versionAdapter: VersionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        versionAdapter = VersionAdapter()
        binding.rvContent.apply {
            adapter = versionAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(VerticalSpaceItemDecoration(dpToPx(5)))
        }
        initButtons()
    }


    private fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

// 未来可期的 px to dp 函数
//    private fun Context.pxToDp(px: Int): Int {
//        return (px / resources.displayMetrics.density).toInt()
//    }


    class VerticalSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            with(outRect) {
                // 对于每一项都添加底部间距
                bottom = space
                // 如果不是第一行，则添加顶部间距
                if (parent.getChildAdapterPosition(view) != 0) top = space

            }
        }
    }


    private fun showUADialog(agreed: Boolean) {

        // 屏幕高度获取
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        //用户协议，传参内容表示先前是否同意过协议
        val userAgreementBinding = UserAgreementBinding.inflate(layoutInflater)

        val dialogUA =
            MaterialAlertDialogBuilder(this)
                .setTitle("用户协议")
                .setIcon(R.drawable.file_text_line)
                .setView(userAgreementBinding.root)
                .setCancelable(false)
                .create()

        val constraintSet = ConstraintSet()
        constraintSet.clone(userAgreementBinding.userAgreement)

        // 屏幕方向判断，不同方向分别设置相应的约束布局用户协议子项高度
        val currentConfig = resources.configuration
        if (currentConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) constraintSet.constrainHeight(
            R.id.UA_text, screenHeight / 6
        )
        else if (currentConfig.orientation == Configuration.ORIENTATION_PORTRAIT) constraintSet.constrainHeight(
            R.id.UA_text, screenHeight / 2
        )

        constraintSet.applyTo(userAgreementBinding.userAgreement)

        userAgreementBinding.uaButtonAgree.setOnClickListener {
            DataStoreUtil.putIntAsync("userAgreement", 1)
            dialogUA.dismiss()
            getData()
        }

        userAgreementBinding.uaButtonDisagree.setOnClickListener {
            DataStoreUtil.putIntAsync("userAgreement", 0)
            finish()
        }
        if (agreed) userAgreementBinding.uaButtonDisagree.text = "撤回同意并退出"

        dialogUA.show()
    }


    private fun initButtons() {
        // 删除 version Shared Preferences
        DataStoreUtil.deletePreferenceAsync("version")

        //这里的“getInt: userAgreement”的值代表着用户协议修订版本，后续更新协议版本后也需要在下面一行把“judgeUARead”+1，以此类推
        val judgeUARead = 1
        if (DataStoreUtil.getInt("userAgreement", 0) != judgeUARead)
            showUADialog(false)
        else
            getData()

        // 进度条动画
        // https://github.com/material-components/material-components-android/blob/master/docs/components/ProgressIndicator.md
        binding.progressLine.apply {
            showAnimationBehavior = LinearProgressIndicator.SHOW_NONE
            hideAnimationBehavior = LinearProgressIndicator.HIDE_ESCAPE
        }

        binding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) binding.btnGuess.shrink()
                else if (dy < 0) binding.btnGuess.extend()
            }
        })


        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            //底部左下角按钮动作
            when (menuItem.itemId) {
                R.id.btn_get -> {
                    getData()
                    true
                }

                R.id.btn_about -> {
                    val message =
                        SpannableString(
                            "QQ 版本列表实用工具 for Android\n\n" +
                                    "作者：快乐小牛、有鲫雪狐\n\n" +
                                    "版本：${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})\n\n" +
                                    "Since 2023.8.9\n\nLicensed under AGPL v3\n\n" +
                                    "开源地址"
                        )
                    val urlSpan = URLSpan("https://github.com/klxiaoniu/QQVersionList")
                    message.setSpan(
                        urlSpan,
                        message.length - 4,
                        message.length,
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    MaterialAlertDialogBuilder(this)
                        .setTitle("关于")
                        .setIcon(R.drawable.information_line)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("撤回同意用户协议") { _, _ ->
                            showUADialog(true)
                        }.show().apply {
                            findViewById<TextView>(android.R.id.message)?.movementMethod =
                                LinkMovementMethodCompat.getInstance()
                        }

                    true
                }

                R.id.btn_setting -> {
                    val dialogSettingBinding = DialogSettingBinding.inflate(layoutInflater)

                    dialogSettingBinding.apply {
                        switchDisplayFirst.isChecked =
                            DataStoreUtil.getBoolean("displayFirst", true)
                        longPressCard.isChecked = DataStoreUtil.getBoolean("longPressCard", true)
                        guessNot5.isChecked = DataStoreUtil.getBoolean("guessNot5", false)
                        progressSize.isChecked = DataStoreUtil.getBoolean("progressSize", false)
                        switchGuessTestExtend.isChecked =
                            DataStoreUtil.getBoolean("guessTestExtend", false) // 扩展测试版猜版格式
                    }

                    val dialogSetting = MaterialAlertDialogBuilder(this)
                        .setTitle("设置")
                        .setIcon(R.drawable.settings_line)
                        .setView(dialogSettingBinding.root)
                        .create()
                    dialogSetting.show()

                    dialogSettingBinding.apply {
                        btnSettingOk.setOnClickListener {
                            dialogSetting.dismiss()
                        }
                        switchDisplayFirst.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("displayFirst", isChecked)
                            getData()
                        }
                        longPressCard.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("longPressCard", isChecked)
                        }
                        guessNot5.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("guessNot5", isChecked)
                        }
                        progressSize.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("progressSize", isChecked)
                            getData()
                        }
                        switchGuessTestExtend.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("guessTestExtend", isChecked)
                        }
                    }

                    true
                }

                else -> false
            }
        }

        if (intent.action == "android.intent.action.VIEW"
            && DataStoreUtil.getInt("userAgreement", 0) == judgeUARead
        ) {
            showGuessVersionDialog()
        }
        binding.btnGuess.setOnClickListener {
            showGuessVersionDialog()
        }

    }

    private fun showGuessVersionDialog() {
        val dialogGuessBinding = DialogGuessBinding.inflate(layoutInflater)
        val verBig = DataStoreUtil.getString("versionBig", "")
        dialogGuessBinding.etVersionBig.editText?.setText(verBig)
        val memVersion = DataStoreUtil.getString("versionSelect", "正式版")
        if (memVersion == "测试版" || memVersion == "空格版" || memVersion == "正式版") {
            dialogGuessBinding.spinnerVersion.setText(memVersion, false)
        }
        if (dialogGuessBinding.spinnerVersion.text.toString() == "测试版" || dialogGuessBinding.spinnerVersion.text.toString() == "空格版") {
            dialogGuessBinding.etVersionSmall.isEnabled = true
            dialogGuessBinding.guessDialogWarning.visibility = View.VISIBLE
        } else if (dialogGuessBinding.spinnerVersion.text.toString() == "正式版") {
            dialogGuessBinding.etVersionSmall.isEnabled = false
            dialogGuessBinding.guessDialogWarning.visibility = View.GONE
        }

        dialogGuessBinding.spinnerVersion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val judgeVerSelect = dialogGuessBinding.spinnerVersion.text.toString()
                DataStoreUtil.putStringAsync("versionSelect", judgeVerSelect)
                if (judgeVerSelect == "测试版" || judgeVerSelect == "空格版") {
                    dialogGuessBinding.etVersionSmall.isEnabled = true
                    dialogGuessBinding.guessDialogWarning.visibility = View.VISIBLE
                } else if (judgeVerSelect == "正式版") {
                    dialogGuessBinding.etVersionSmall.isEnabled = false
                    dialogGuessBinding.guessDialogWarning.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

//            舍弃
//            dialogGuessBinding.spinnerVersion.setOnFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(dialogGuessBinding.spinnerVersion.windowToken, 0)
//                }
//            }


        val dialogGuess = MaterialAlertDialogBuilder(this)
            .setTitle("猜版 for Android")
            .setIcon(R.drawable.search_line)
            .setView(dialogGuessBinding.root)
            .setCancelable(false)
            .show()

        dialogGuessBinding.btnGuessStart.setOnClickListener {
            dialogGuessBinding.etVersionBig.clearFocus()
            dialogGuessBinding.spinnerVersion.clearFocus()
            dialogGuessBinding.etVersionSmall.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(dialogGuessBinding.spinnerVersion.windowToken, 0)

            try {
                val versionBig = dialogGuessBinding.etVersionBig.editText?.text.toString()
                val mode = dialogGuessBinding.spinnerVersion.text.toString()
                var versionSmall = 0
                if (mode == "测试版" || mode == "空格版") {
                    versionSmall =
                        dialogGuessBinding.etVersionSmall.editText?.text.toString().toInt()
                }
                if (versionSmall % 5 != 0 && !DataStoreUtil.getBoolean(
                        "guessNot5", false
                    )
                ) throw Exception("小版本号需填 5 的倍数。如有需求，请前往设置解除此限制。")
                if (versionSmall != 0) {
                    DataStoreUtil.putIntAsync("versionSmall", versionSmall)
                }/*我偷懒了，因为我上面也有偷懒逻辑，
                       为了防止 null，我在正式版猜版时默认填入了 0，
                       但是我没处理下面涉及到持久化存储逻辑的语句，就把 0 存进去了，
                       覆盖了原来的 15xxx 的持久化存储*/

                guessUrl(versionBig, versionSmall, mode)

            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            }
        }


        dialogGuessBinding.btnGuessCancel.setOnClickListener {
            dialogGuess.dismiss()
        }

        val memVersionSmall = DataStoreUtil.getInt("versionSmall", -1)
        if (memVersionSmall != -1) {
            dialogGuessBinding.etVersionSmall.editText?.setText(memVersionSmall.toString())
        }
    }

    private fun getData() {
        binding.progressLine.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request =
                    Request.Builder().url("https://im.qq.com/rainbow/androidQQVersionList")
                        .build()
                val response = okHttpClient.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    val start = (responseData.indexOf("versions64\":[")) + 12
                    val end = (responseData.indexOf(";\n" + "      typeof"))
                    val totalJson = responseData.substring(start, end)
                    val qqVersion = totalJson.split("},{").reversed().map {
                        val pstart = it.indexOf("{\"versions")
                        val pend = it.indexOf(",\"length")
                        val json = it.substring(pstart, pend)
                        Gson().fromJson(json, QQVersionBean::class.java).apply {
                            jsonString = json
                        }
                    }
                    withContext(Dispatchers.Main) {
                        versionAdapter.setData(qqVersion)
                        // 舍弃 currentQQVersion = qqVersion.first().versionNumber
                        // 大版本号也放持久化存储了，否则猜版 Shortcut 因为加载过快而获取不到东西
                        DataStoreUtil.putStringAsync(
                            "versionBig", qqVersion.first().versionNumber
                        )
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            } finally {
                withContext(Dispatchers.Main) {
                    binding.progressLine.hide()
                }
            }
        }
    }


    /*获取文件大小（以MB为单位）
      @param urlString 文件的URL字符串
      @param callback 回调函数，接收文件大小（以MB为单位）作为参数*/
    private fun getFileSizeInMB(urlString: String, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"

                val fileSize = connection.contentLength.toDouble()
                val fileSizeInMB = fileSize / (1024 * 1024)

                withContext(Dispatchers.Main) {
                    callback("%.2f".format(fileSizeInMB))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback("Error")
                    dialogError(e)
                }
            }
        }
    }


    // https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.75.XXXXX_64.apk
    private fun guessUrl(versionBig: String, versionSmall: Int, mode: String) {
        // 绑定 AlertDialog 加载对话框布局
        val dialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        val successButtonBinding = SuccessButtonBinding.inflate(layoutInflater)

        var status = STATUS_ONGOING

        val progressDialog =
            MaterialAlertDialogBuilder(this)
                .setView(dialogLoadingBinding.root)
                .setCancelable(false)
                .create()

        fun updateProgressDialogMessage(newMessage: String) {
            dialogLoadingBinding.loadingMessage.text = newMessage
            if (!progressDialog.isShowing) {
                progressDialog.show()// 更新文本后才显示对话框
            }
        }

        var link = ""
        val thread = Thread {
            var vSmall = versionSmall
            val stList = listOf(
                "_64",
                "_64_HB",
                "_64_HB1",
                "_64_HB2",
                "_64_HB3",
                "_64_HD",
                "_64_HD1",
                "_64_HD2",
                "_64_HD3",
                "_64_HD1HB",
                "_HB_64",
                "_HB1_64",
                "_HB2_64",
                "_HB3_64",
                "_HD_64",
                "_HD1_64",
                "_HD2_64",
                "_HD3_64",
                "_HD1HB_64"
            )
            try {
                var sIndex = 0
                while (true) {
                    when (status) {
                        STATUS_ONGOING -> {
                            if (mode == MODE_TEST) {
                                if (link == "" || !DataStoreUtil.getBoolean(
                                        "guessTestExtend",
                                        false
                                    )
                                ) link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                else if (DataStoreUtil.getBoolean("guessTestExtend", false)) {
                                    sIndex += 1
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}.${vSmall}${stList[sIndex]}.apk"
                                }
                            } else if (mode == MODE_UNOFFICIAL) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%20$versionBig.${vSmall}%2064.apk"
                            } else if (mode == MODE_OFFICIAL) {
                                val soList = listOf(
                                    "_64",
                                    "_64_HB",
                                    "_64_HB1",
                                    "_64_HB2",
                                    "_64_HB3",
                                    "_HB_64",
                                    "_HB1_64",
                                    "_HB2_64",
                                    "_HB3_64"
                                )
                                if (link == "") link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}${soList[sIndex]}.apk"
                                else if (sIndex == (soList.size - 1)) {
                                    status = STATUS_END
                                    showToast("未猜测到包")
                                    continue
                                } else {
                                    sIndex += 1
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}${soList[sIndex]}.apk"
                                }

                            }
                            runOnUiThread {
                                updateProgressDialogMessage("正在猜测下载地址：$link")
                            }
                            val okHttpClient = OkHttpClient()
                            val request = Request.Builder().url(link).build()
                            val response = okHttpClient.newCall(request).execute()
                            val success = response.isSuccessful
                            if (success) {
                                status = STATUS_PAUSE
                                getFileSizeInMB(link) { appSize ->
                                    runOnUiThread {
                                        successButtonBinding.root.parent?.let { parent ->
                                            if (parent is ViewGroup) {
                                                parent.removeView(successButtonBinding.root)
                                            }
                                        }

                                        val successMaterialDialog = MaterialAlertDialogBuilder(this)
                                            .setTitle("猜测成功")
                                            .setMessage("下载地址：$link")
                                            .setIcon(R.drawable.check_circle)
                                            .setView(successButtonBinding.root)
                                            .setCancelable(false)
                                            .apply {
                                                if (appSize != "Error" && appSize != "-0.00" && appSize != "0.00") setMessage(
                                                    "下载地址：$link\n\n大小：$appSize MB"
                                                )
                                                else setMessage("下载地址：$link")
                                            }.show()


                                        // 复制并停止按钮点击事件
                                        successButtonBinding.btnCopy.setOnClickListener {
                                            copyText(link)
                                            successMaterialDialog.dismiss()
                                            status = STATUS_END
                                        }

                                        // 继续按钮点击事件
                                        successButtonBinding.btnContinue.setOnClickListener {
                                            // 测试版情况下，未打开扩展猜版或扩展猜版到最后一步时执行小版本号的递增
                                            if (mode == MODE_TEST && (!DataStoreUtil.getBoolean(
                                                    "guessTestExtend",
                                                    false
                                                ) || sIndex == (stList.size - 1))
                                            ) {
                                                vSmall += if (!DataStoreUtil.getBoolean(
                                                        "guessNot5",
                                                        false
                                                    )
                                                ) 5 else 1
                                                sIndex = 0
                                            } else if (mode == MODE_UNOFFICIAL) vSmall += if (!DataStoreUtil.getBoolean(
                                                    "guessNot5",
                                                    false
                                                )
                                            ) 5 else 1
                                            successMaterialDialog.dismiss()
                                            status = STATUS_ONGOING
                                        }

                                        // 停止按钮点击事件
                                        successButtonBinding.btnStop.setOnClickListener {
                                            successMaterialDialog.dismiss()
                                            status = STATUS_END
                                        }

                                        // 分享按钮点击事件
                                        successButtonBinding.btnShare.setOnClickListener {
                                            successMaterialDialog.dismiss()
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    if (appSize != "Error" && appSize != "-0.00" && appSize != "0.00") {
                                                        if (mode == MODE_OFFICIAL) "Android QQ $versionBig 正式版（大小：$appSize MB）\n\n下载地址：$link"
                                                        else "Android QQ $versionBig.$vSmall 测试版（大小：$appSize MB）\n\n下载地址：$link\n\n鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                                    } else {
                                                        if (mode == MODE_OFFICIAL) "Android QQ $versionBig 正式版\n\n下载地址：$link"
                                                        else "Android QQ $versionBig.$vSmall 测试版\n\n下载地址：$link\n\n鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                                    }
                                                )
                                            }
                                            startActivity(
                                                Intent.createChooser(
                                                    shareIntent, "分享到"
                                                )
                                            )
                                            status = STATUS_END
                                        }

                                        // 下载按钮点击事件
                                        successButtonBinding.btnDownload.setOnClickListener {
                                            val requestDownload =
                                                DownloadManager.Request(Uri.parse(link))
                                            if (mode == MODE_TEST || mode == MODE_UNOFFICIAL) {
                                                requestDownload.setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_QQ_${versionBig}.${vSmall}_64.apk"
                                                )
                                            } else if (mode == MODE_OFFICIAL) {
                                                requestDownload.setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_QQ_${versionBig}_64.apk"
                                                )
                                            }
                                            val downloadManager =
                                                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                            downloadManager.enqueue(requestDownload)
                                            successMaterialDialog.dismiss()
                                            status = STATUS_END
                                        }
                                    }
                                }
                            } else {
                                if (mode == MODE_TEST && (!DataStoreUtil.getBoolean(
                                        "guessTestExtend",
                                        false
                                    ) || sIndex == (stList.size - 1)) // 测试版情况下，未打开扩展猜版或扩展猜版到最后一步时执行小版本号的递增
                                ) {
                                    vSmall += if (!DataStoreUtil.getBoolean(
                                            "guessNot5",
                                            false
                                        )
                                    ) 5 else 1
                                    sIndex = 0
                                } else if (mode == MODE_UNOFFICIAL) vSmall += if (!DataStoreUtil.getBoolean(
                                        "guessNot5",
                                        false
                                    )
                                ) 5 else 1
                            }
                        }

                        STATUS_PAUSE -> {
                            sleep(500)
                        }

                        STATUS_END -> {
                            if (mode != MODE_OFFICIAL) showToast("已停止猜测")
                            sIndex = 0
                            progressDialog.dismiss()
                            break
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
                showToast("已停止猜测")
                progressDialog.dismiss()
            }
        }

        dialogLoadingBinding.btnCancel.setOnClickListener {
            status = STATUS_END
            progressDialog.dismiss()
        }

        thread.start()
    }


    companion object {
        const val STATUS_ONGOING = 0
        const val STATUS_PAUSE = 1
        const val STATUS_END = 2

        const val MODE_TEST = "测试版"
        const val MODE_OFFICIAL = "正式版"
        const val MODE_UNOFFICIAL = "空格版"  //空格猜版
    }

}