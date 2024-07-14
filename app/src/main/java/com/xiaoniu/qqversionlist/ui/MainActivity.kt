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


//import android.util.Base64
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.URLSpan
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.xiaoniu.qqversionlist.BuildConfig
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.databinding.DialogGuessBinding
import com.xiaoniu.qqversionlist.databinding.DialogLoadingBinding
import com.xiaoniu.qqversionlist.databinding.DialogPersonalizationBinding
import com.xiaoniu.qqversionlist.databinding.DialogSettingBinding
import com.xiaoniu.qqversionlist.databinding.DialogShiplyBackBinding
import com.xiaoniu.qqversionlist.databinding.DialogSuffixDefineBinding
import com.xiaoniu.qqversionlist.databinding.DialogTencentShiplyBinding
import com.xiaoniu.qqversionlist.databinding.SuccessButtonBinding
import com.xiaoniu.qqversionlist.databinding.UserAgreementBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.StringUtil.getAllAPKUrl
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import com.xiaoniu.qqversionlist.util.TencentShiplyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.GZIPInputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var versionAdapter: VersionAdapter
    private lateinit var qqVersion: List<QQVersionBean>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SDK_INT <= Build.VERSION_CODES.Q) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
                view.setPadding(insets.left, 0, insets.right, 0)
                binding.apply {
                    bottomAppBar.updatePadding(0, 0, 0, insets.bottom)
                    btnGuess.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = insets.bottom / 2
                    }
                }
                windowInsets
            }
        }

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (SDK_INT >= Build.VERSION_CODES.Q) window.apply {
            isNavigationBarContrastEnforced = false
            isStatusBarContrastEnforced = false
        }

        if (SDK_INT <= Build.VERSION_CODES.Q) window.apply {
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }

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


    private fun showUADialog(agreed: Boolean, UATarget: Int) {

        // 屏幕高度获取
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        //用户协议，传参内容表示先前是否同意过协议
        //谁动了代码不动注释？

        val userAgreementBinding = UserAgreementBinding.inflate(layoutInflater)

        val dialogUA = MaterialAlertDialogBuilder(this)
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
            DataStoreUtil.putIntAsync("userAgreement", UATarget)
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
        val judgeUATarget = 2 // 2024.5.30 第二版
        if (DataStoreUtil.getInt("userAgreement", 0) < judgeUATarget) showUADialog(
            false, judgeUATarget
        )
        else getData()

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
                    val message = SpannableString(
                        "QQ 版本列表实用工具 for Android\n\n" +
                                "提供 Android QQ 版本列表的查看和对 Android QQ 下载链接的枚举法猜测。\n\n" +
                                "版本：${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})\n" +
                                "作者：快乐小牛、有鲫雪狐\n" +
                                "贡献者：Col_or、bggRGjQaUbCoE、GMerge\n" +
                                "特别感谢：owo233\n"+
                                "开源地址：GitHub\n" +
                                "开源协议：AGPL v3\n" +
                                "获取更新：GitHub Releases、Obtainium、九七通知中心\n\n" +
                                "Since 2023.8.9"
                    ).apply {
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu"),
                            indexOf("快乐小牛"),
                            indexOf("快乐小牛") + "快乐小牛".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/ArcticFoxPro"),
                            indexOf("有鲫雪狐"),
                            indexOf("有鲫雪狐") + "有鲫雪狐".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/color597"),
                            indexOf("Col_or"),
                            indexOf("Col_or") + "Col_or".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/bggRGjQaUbCoE"),
                            indexOf("bggRGjQaUbCoE"),
                            indexOf("bggRGjQaUbCoE") + "bggRGjQaUbCoE".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/egmsia01"),
                            indexOf("GMerge"),
                            indexOf("GMerge") + "GMerge".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/callng"),
                            indexOf("owo233"),
                            indexOf("owo233") + "owo233".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu/QQVersionList"),
                            indexOf("GitHub"),
                            indexOf("GitHub") + "GitHub".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu/QQVersionList/blob/master/LICENSE"),
                            indexOf("AGPL v3"),
                            indexOf("AGPL v3") + "AGPL v3".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu/QQVersionList/releases"),
                            indexOf("GitHub Releases"),
                            indexOf("GitHub Releases") + "GitHub Releases".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu/QQVersionList/blob/master/ReadmeAssets/Get-it-on-Obtainium.md"),
                            indexOf("Obtainium"),
                            indexOf("Obtainium") + "Obtainium".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            URLSpan("https://github.com/klxiaoniu/QQVersionList/blob/master/ReadmeAssets/Get-it-on-JiuQi-NotifCenter-WeChatMiniProgram.md"),
                            indexOf("九七通知中心"),
                            indexOf("九七通知中心") + "九七通知中心".length,
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    MaterialAlertDialogBuilder(this)
                        .setTitle("关于")
                        .setIcon(R.drawable.information_line)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .setNegativeButton("撤回同意用户协议") { _, _ ->
                            showUADialog(true, judgeUATarget)
                        }.show().apply {
                            findViewById<TextView>(android.R.id.message)?.movementMethod =
                                LinkMovementMethodCompat.getInstance()
                        }

                    true
                }

                R.id.btn_setting -> {
                    val dialogSettingBinding = DialogSettingBinding.inflate(layoutInflater)

                    dialogSettingBinding.apply {
                        longPressCard.isChecked = DataStoreUtil.getBoolean("longPressCard", true)
                        guessNot5.isChecked = DataStoreUtil.getBoolean("guessNot5", false)
                        switchGuessTestExtend.isChecked =
                            DataStoreUtil.getBoolean("guessTestExtend", false) // 扩展测试版猜版格式
                        downloadOnSystemManager.isChecked =
                            DataStoreUtil.getBoolean("downloadOnSystemManager", false)
                    }

                    val dialogSetting = MaterialAlertDialogBuilder(this)
                        .setTitle("设置")
                        .setIcon(R.drawable.settings_line)
                        .setView(dialogSettingBinding.root)
                        .show()

                    dialogSettingBinding.apply {
                        btnSettingOk.setOnClickListener {
                            dialogSetting.dismiss()
                        }
                        longPressCard.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("longPressCard", isChecked)
                        }
                        guessNot5.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("guessNot5", isChecked)
                        }
                        dialogPersonalization.setOnClickListener {
                            val dialogPersonalization =
                                DialogPersonalizationBinding.inflate(layoutInflater)

                            dialogPersonalization.root.parent?.let { parent ->
                                if (parent is ViewGroup) {
                                    parent.removeView(dialogPersonalization.root)
                                }
                            }

                            val dialogPer = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("个性化")
                                .setIcon(R.drawable.palette_line)
                                .setView(dialogPersonalization.root)
                                .show()

                            dialogPersonalization.apply {
                                switchDisplayFirst.isChecked =
                                    DataStoreUtil.getBoolean("displayFirst", true)
                                progressSize.isChecked =
                                    DataStoreUtil.getBoolean("progressSize", false)
                                versionTcloud.isChecked =
                                    DataStoreUtil.getBoolean("versionTCloud", true)

                                switchDisplayFirst.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanAsync("displayFirst", isChecked)
                                    qqVersion = qqVersion.mapIndexed { index, qqVersionBean ->
                                        if (index == 0) qqVersionBean.copy(
                                            displayType = if (isChecked) 1 else 0
                                        )
                                        else qqVersionBean
                                    }
                                    versionAdapter.submitList(qqVersion)

                                }
                                progressSize.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanAsync("progressSize", isChecked)
                                    versionAdapter.updateItemProperty("isShowProgressSize")
                                }
                                versionTcloud.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanAsync("versionTCloud", isChecked)
                                    versionAdapter.updateItemProperty("isTCloud")
                                }
                                btnPersonalizationOk.setOnClickListener {
                                    dialogPer.dismiss()
                                }
                            }

                        }

                        switchGuessTestExtend.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("guessTestExtend", isChecked)
                        }
                        downloadOnSystemManager.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanAsync("downloadOnSystemManager", isChecked)
                        }
//                        settingSuffixSave.setOnClickListener { _ ->
//                            val suffixDefine = settingSuffixDefine.editText?.text.toString()
//                            DataStoreUtil.putStringAsync("suffixDefine", suffixDefine)
//                            showToast("已保存")
//                        }
                        dialogSuffixDefineClick.setOnClickListener {
                            val dialogSuffixDefine =
                                DialogSuffixDefineBinding.inflate(layoutInflater)

                            dialogSuffixDefine.root.parent?.let { parent ->
                                if (parent is ViewGroup) {
                                    parent.removeView(dialogSuffixDefine.root)
                                }
                            }

                            val dialogSuffix = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle("猜版后缀设置")
                                .setIcon(R.drawable.settings_line)
                                .setView(dialogSuffixDefine.root)
                                .setCancelable(false)
                                .create()

                            val screenHeight = Resources.getSystem().displayMetrics.heightPixels

                            val constraintSet = ConstraintSet()
                            constraintSet.clone(dialogSuffixDefine.dialogSuffixDefineContainer)

                            // 屏幕方向判断，不同方向分别设置相应的约束布局滚动列表子项高度
                            val currentConfig = resources.configuration
                            if (currentConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) constraintSet.constrainHeight(
                                R.id.suffix_define_check_group_all, screenHeight / 6
                            )
                            else if (currentConfig.orientation == Configuration.ORIENTATION_PORTRAIT) constraintSet.constrainHeight(
                                R.id.suffix_define_check_group_all, screenHeight / 3
                            )

                            constraintSet.applyTo(dialogSuffixDefine.dialogSuffixDefineContainer)

                            dialogSuffixDefine.apply {
                                suffixDefineCheckbox64hb.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HB", true)
                                suffixDefineCheckboxHb64.isChecked =
                                    DataStoreUtil.getBoolean("suffixHB64", true)
                                suffixDefineCheckbox64hb1.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HB1", true)
                                suffixDefineCheckboxHb164.isChecked =
                                    DataStoreUtil.getBoolean("suffixHB164", true)
                                suffixDefineCheckbox64hb2.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HB2", true)
                                suffixDefineCheckboxHb264.isChecked =
                                    DataStoreUtil.getBoolean("suffixHB264", true)
                                suffixDefineCheckbox64hb3.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HB3", true)
                                suffixDefineCheckboxHb364.isChecked =
                                    DataStoreUtil.getBoolean("suffixHB364", true)

                                suffixDefineCheckbox64hd.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HD", true)
                                suffixDefineCheckboxHd64.isChecked =
                                    DataStoreUtil.getBoolean("suffixHD64", true)
                                suffixDefineCheckbox64hd1.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HD1", true)
                                suffixDefineCheckboxHd164.isChecked =
                                    DataStoreUtil.getBoolean("suffixHD164", true)
                                suffixDefineCheckbox64hd2.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HD2", true)
                                suffixDefineCheckboxHd264.isChecked =
                                    DataStoreUtil.getBoolean("suffixHD264", true)
                                suffixDefineCheckbox64hd3.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HD3", true)
                                suffixDefineCheckboxHd364.isChecked =
                                    DataStoreUtil.getBoolean("suffixHD364", true)

                                suffixDefineCheckbox64hd1hb.isChecked =
                                    DataStoreUtil.getBoolean("suffix64HD1HB", true)
                                suffixDefineCheckboxHd1hb64.isChecked =
                                    DataStoreUtil.getBoolean("suffixHD1HB64", true)


                                dialogSuffix.show()

//                            dialogSuffixDefine.settingSuffixDefine.editText?.setText(
//                                DataStoreUtil.getStringAsync("suffixDefine", "")
//                            )

                                // 异步读取字符串，防止超长字符串造成阻塞
                                settingSuffixDefine.apply {
                                    isEnabled = false
                                    btnSuffixSave.isEnabled = false
                                    lifecycleScope.launch {
                                        val suffixDefine = withContext(Dispatchers.IO) {
                                            DataStoreUtil.getStringAsync("suffixDefine", "").await()
                                        }
                                        editText?.setText(
                                            suffixDefine
                                        )
                                        isEnabled = true
                                        btnSuffixSave.isEnabled = true
                                    }
                                }

                                btnSuffixSave.setOnClickListener {
                                    val suffixDefine = settingSuffixDefine.editText?.text.toString()
                                    DataStoreUtil.apply {
                                        putStringAsync("suffixDefine", suffixDefine)

                                        putBooleanAsync(
                                            "suffix64HB", suffixDefineCheckbox64hb.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHB64", suffixDefineCheckboxHb64.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HB1", suffixDefineCheckbox64hb1.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHB164", suffixDefineCheckboxHb164.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HB2", suffixDefineCheckbox64hb2.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHB264", suffixDefineCheckboxHb264.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HB3", suffixDefineCheckbox64hb3.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHB364", suffixDefineCheckboxHb364.isChecked
                                        )

                                        putBooleanAsync(
                                            "suffix64HD", suffixDefineCheckbox64hd.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHD64", suffixDefineCheckboxHd64.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HD1", suffixDefineCheckbox64hd1.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHD164", suffixDefineCheckboxHd164.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HD2", suffixDefineCheckbox64hd2.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHD264", suffixDefineCheckboxHd264.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HD3", suffixDefineCheckbox64hd3.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffixHD364", suffixDefineCheckboxHd364.isChecked
                                        )

                                        putBooleanAsync(
                                            "suffixHD1HB64", suffixDefineCheckboxHd1hb64.isChecked
                                        )
                                        putBooleanAsync(
                                            "suffix64HD1HB", suffixDefineCheckbox64hd1hb.isChecked
                                        )
                                    }

                                    showToast("已保存")
                                    dialogSuffix.dismiss()
                                }

                                btnSuffixCancel.setOnClickListener {
                                    dialogSuffix.dismiss()
                                }
                            }
                        }
                    }
                    true
                }

                R.id.btn_tencent_shiply -> {
                    val dialogTencentShiplyBinding =
                        DialogTencentShiplyBinding.inflate(layoutInflater)

                    val shiplyDialog = MaterialAlertDialogBuilder(this)
                        .setTitle("Shiply 平台更新获取（实验性）")
                        .setIcon(R.drawable.flask_line)
                        .setView(dialogTencentShiplyBinding.root)
                        .setCancelable(false)
                        .show()

                    dialogTencentShiplyBinding.apply {
                        DataStoreUtil.apply {
                            shiplyUin.editText?.setText(getString("shiplyUin", ""))
                            shiplyAppid.editText?.setText(getString("shiplyAppid", ""))
                            shiplyVersion.editText?.setText(
                                getString("shiplyVersion", "")
                            )
                        }

                        btnShiplyCancel.setOnClickListener {
                            shiplyDialog.dismiss()
                        }

                        btnShiplyStart.setOnClickListener {
                            shiplyUin.clearFocus()
                            shiplyAppid.clearFocus()
                            shiplyVersion.clearFocus()

                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(shiplyVersion.windowToken, 0)

                            class MissingParameterException(message: String) : Exception(message)

                            try {
                                val spec = CircularProgressIndicatorSpec(
                                    this@MainActivity,
                                    null,
                                    0,
                                    com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
                                )
                                val progressIndicatorDrawable =
                                    IndeterminateDrawable.createCircularDrawable(
                                        this@MainActivity,
                                        spec
                                    )

                                btnShiplyStart.isEnabled = false
                                btnShiplyStart.icon = progressIndicatorDrawable

                                if (shiplyUin.editText?.text.toString()
                                        .isEmpty()
                                ) throw MissingParameterException("uin 信息用于请求 TDS 腾讯端服务 Shiply 发布平台的对象 QQ 号，缺失 uin 参数将无法获取 Shiply 平台返回数据。")
                                if (shiplyVersion.editText?.text.toString()
                                        .isEmpty()
                                ) throw MissingParameterException("请求 TDS 腾讯端服务 Shiply 发布平台需要 QQ 版本号参数，缺失版本号参数将无法获取 Shiply 平台返回数据。")

                                if (shiplyVersion.editText?.text.toString()
                                        .isEmpty()
                                ) {
                                    DataStoreUtil.apply {
                                        putString(
                                            "shiplyVersion",
                                            shiplyVersion.editText?.text.toString()
                                        )
                                        putString("shiplyUin", shiplyUin.editText?.text.toString())
                                    }
                                    tencentShiplyStart(
                                        btnShiplyStart,
                                        shiplyVersion.editText?.text.toString(),
                                        shiplyUin.editText?.text.toString()
                                    )
                                } else {
                                    DataStoreUtil.apply {
                                        putString(
                                            "shiplyVersion",
                                            shiplyVersion.editText?.text.toString()
                                        )
                                        putString(
                                            "shiplyAppid",
                                            shiplyAppid.editText?.text.toString()
                                        )
                                        putString("shiplyUin", shiplyUin.editText?.text.toString())
                                    }
                                    tencentShiplyStart(
                                        btnShiplyStart,
                                        shiplyVersion.editText?.text.toString(),
                                        shiplyUin.editText?.text.toString(),
                                        shiplyAppid.editText?.text.toString()
                                    )
                                }
                            } catch (e: MissingParameterException) {
                                dialogError(e, true)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                dialogError(e)
                            }
                        }
                    }
                    true
                }

                else -> false
            }
        }

        if (intent.action == "android.intent.action.VIEW" && DataStoreUtil.getInt(
                "userAgreement",
                0
            ) >= judgeUATarget
        ) showGuessVersionDialog()
        binding.btnGuess.setOnClickListener {
            showGuessVersionDialog()
        }

    }

    private fun showGuessVersionDialog() {
        val dialogGuessBinding = DialogGuessBinding.inflate(layoutInflater)
        val verBig = DataStoreUtil.getString("versionBig", "")
        dialogGuessBinding.etVersionBig.editText?.setText(verBig)
        val memVersion = DataStoreUtil.getString("versionSelect", "正式版")
        if (memVersion == "测试版" || memVersion == "空格猜版" || memVersion == "正式版" || memVersion == "微信猜版") dialogGuessBinding.spinnerVersion.setText(
            memVersion,
            false
        )
        if (dialogGuessBinding.spinnerVersion.text.toString() == "测试版" || dialogGuessBinding.spinnerVersion.text.toString() == "空格猜版") dialogGuessBinding.apply {
            etVersionSmall.isEnabled = true
            etVersionSmall.visibility = View.VISIBLE
            guessDialogWarning.visibility = View.VISIBLE
            etVersion16code.visibility = View.GONE
            etVersionTrue.visibility = View.GONE
            tvWarning.text =
                "鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。根据相关条款，您使用本软件时应当已了解并同意，因下载或使用 QQ 测试版而可能产生的任何直接或间接损失、损害以及其他不利后果，均由您自行承担全部责任。"
            dialogGuessBinding.etVersionBig.helperText = "填写格式为 x.y.z"
        } else if (dialogGuessBinding.spinnerVersion.text.toString() == "正式版") dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            etVersionSmall.visibility = View.VISIBLE
            guessDialogWarning.visibility = View.GONE
            etVersion16code.visibility = View.GONE
            etVersionTrue.visibility = View.GONE
            etVersionBig.helperText = "填写格式为 x.y.z"
        } else if (dialogGuessBinding.spinnerVersion.text.toString() == "微信猜版") dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            guessDialogWarning.visibility = View.VISIBLE
            etVersionSmall.visibility = View.GONE
            etVersionTrue.visibility = View.VISIBLE
            etVersion16code.visibility = View.VISIBLE
            tvWarning.text =
                "微信猜版功能为 QQ 版本列表实用工具附带的实验性功能，可能存在不可预知的稳定性问题。请明确并确保自身具备足够的风险识别和承受能力。"
            etVersionBig.helperText = "无需填写小数点"
        }


        dialogGuessBinding.spinnerVersion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val judgeVerSelect = dialogGuessBinding.spinnerVersion.text.toString()
                DataStoreUtil.putStringAsync("versionSelect", judgeVerSelect)
                when (judgeVerSelect) {
                    "测试版", "空格猜版" -> dialogGuessBinding.apply {
                        etVersionSmall.isEnabled = true
                        etVersionSmall.visibility = View.VISIBLE
                        guessDialogWarning.visibility = View.VISIBLE
                        etVersion16code.visibility = View.GONE
                        etVersionTrue.visibility = View.GONE
                        tvWarning.text =
                            "鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。根据相关条款，您使用本软件时应当已了解并同意，因下载或使用 QQ 测试版而可能产生的任何直接或间接损失、损害以及其他不利后果，均由您自行承担全部责任。"
                        etVersionBig.helperText = "填写格式为 x.y.z"
                    }

                    "正式版" -> dialogGuessBinding.apply {
                        etVersionSmall.visibility = View.VISIBLE
                        etVersionSmall.isEnabled = false
                        guessDialogWarning.visibility = View.GONE
                        etVersion16code.visibility = View.GONE
                        etVersionTrue.visibility = View.GONE
                        etVersionBig.helperText = "填写格式为 x.y.z"
                    }

                    "微信猜版" -> dialogGuessBinding.apply {
                        etVersionSmall.isEnabled = false
                        etVersionSmall.visibility = View.GONE
                        guessDialogWarning.visibility = View.VISIBLE
                        etVersion16code.visibility = View.VISIBLE
                        etVersionTrue.visibility = View.VISIBLE
                        tvWarning.text =
                            "微信猜版功能为 QQ 版本列表实用工具附带的实验性功能，可能存在不可预知的稳定性问题。请明确并确保自身具备足够的风险识别和承受能力。"
                        etVersionBig.helperText = "无需填写小数点"
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

//            舍弃
//            dialogGuessBinding.spinnerVersion.setOnFocusChangeListener { _, hasFocus ->
//                if (hasFocus) {
//                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                    imm.hideSoftInputFromWindow(dialogGuessBinding.spinnerVersion.windowToken, 0)
//                }
//            }


        val dialogGuess = MaterialAlertDialogBuilder(this)
            .setTitle("猜版 Extended")
            .setIcon(R.drawable.search_line)
            .setView(dialogGuessBinding.root)
            .setCancelable(false)
            .show()

        class MissingVersionException(message: String) : Exception(message)
        class InvalidMultipleException(message: String) : Exception(message)

        dialogGuessBinding.btnGuessStart.setOnClickListener {
            dialogGuessBinding.apply {
                etVersionBig.clearFocus()
                spinnerVersion.clearFocus()
                etVersionSmall.clearFocus()
                etVersion16code.clearFocus()
                etVersionTrue.clearFocus()
            }
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(dialogGuessBinding.spinnerVersion.windowToken, 0)

            try {
                if (dialogGuessBinding.etVersionBig.editText?.text.toString()
                        .isEmpty()
                ) throw MissingVersionException("猜版需要填写主版本号，否则无法执行猜版。")
                val versionBig = dialogGuessBinding.etVersionBig.editText?.text.toString()
                val mode = dialogGuessBinding.spinnerVersion.text.toString()
                var versionSmall = 0
                var version16code = 0.toString()
                var versionTrue = 0
                if (mode == "测试版" || mode == "空格猜版") {
                    if (dialogGuessBinding.etVersionSmall.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        "测试版猜版（含空格猜版）需要填写小版本号，否则无法猜测测试版。"
                    )
                    else {
                        versionSmall =
                            dialogGuessBinding.etVersionSmall.editText?.text.toString().toInt()
                        if (versionSmall % 5 != 0 && !DataStoreUtil.getBoolean(
                                "guessNot5", false
                            )
                        ) throw InvalidMultipleException("小版本号需填 5 的倍数。如有需求，请前往设置解除此限制。")
                        if (versionSmall != 0) DataStoreUtil.putIntAsync(
                            "versionSmall", versionSmall
                        )
                    }

                } else if (mode == "微信猜版") {
                    if (dialogGuessBinding.etVersionTrue.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        "微信猜版需要填写真实版本号，否则无法猜测微信版本。"
                    )
                    else if (dialogGuessBinding.etVersion16code.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        "微信猜版需要填写十六进制代码，否则无法猜测微信版本。"
                    )
                    else {
                        versionTrue =
                            dialogGuessBinding.etVersionTrue.editText?.text.toString().toInt()
                        version16code = dialogGuessBinding.etVersion16code.editText?.text.toString()
                        if (version16code != 0.toString()) DataStoreUtil.putStringAsync(
                            "version16code", version16code
                        )
                        if (versionTrue != 0) DataStoreUtil.putIntAsync("versionTrue", versionTrue)
                    }
                }
                guessUrl(versionBig, versionSmall, versionTrue, version16code, mode)
            } catch (e: MissingVersionException) {
                dialogError(e, true)
            } catch (e: InvalidMultipleException) {
                dialogError(e, true)
            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            }
        }



        dialogGuessBinding.btnGuessCancel.setOnClickListener {
            dialogGuess.dismiss()
        }

        val memVersionSmall = DataStoreUtil.getInt("versionSmall", -1)
        if (memVersionSmall != -1) dialogGuessBinding.etVersionSmall.editText?.setText(
            memVersionSmall.toString()
        )
        val memVersion16code = DataStoreUtil.getString("version16code", "-1")
        if (memVersion16code != "-1") dialogGuessBinding.etVersion16code.editText?.setText(
            memVersion16code
        )
        val memVersionTrue = DataStoreUtil.getInt("versionTrue", -1)
        if (memVersionTrue != -1) dialogGuessBinding.etVersionTrue.editText?.setText(memVersionTrue.toString())
    }


    private fun getData() {
        binding.progressLine.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 识别本机 Android QQ 版本并放进持久化存储
                val QQPackageInfo = packageManager.getPackageInfo("com.tencent.mobileqq", 0)
                val QQVersionInstall = QQPackageInfo.versionName
                if (QQVersionInstall != DataStoreUtil.getString(
                        "QQVersionInstall",
                        ""
                    )
                ) DataStoreUtil.putString("QQVersionInstall", QQVersionInstall)
            } catch (e: Exception) {
                DataStoreUtil.putStringAsync("QQVersionInstall", "")
            } finally {
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
                        qqVersion = totalJson.split("},{").reversed().map {
                            val pstart = it.indexOf("{\"versions")
                            val pend = it.indexOf(",\"length")
                            val json = it.substring(pstart, pend)
                            Json.decodeFromString<QQVersionBean>(json).apply {
                                jsonString = json
                                // 标记本机 Android QQ 版本
                                this.displayInstall = (DataStoreUtil.getString(
                                    "QQVersionInstall", ""
                                ) == this.versionNumber)
                            }
                        }
                        if (DataStoreUtil.getBoolean(
                                "displayFirst", true
                            )
                        ) qqVersion[0].displayType = 1
                        withContext(Dispatchers.Main) {
                            versionAdapter.submitList(qqVersion)
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
    private fun guessUrl(
        versionBig: String,
        versionSmall: Int,
        versionTrue: Int,
        version16codeStr: String,
        mode: String
    ) {
        // 绑定 AlertDialog 加载对话框布局
        val dialogLoadingBinding = DialogLoadingBinding.inflate(layoutInflater)
        val successButtonBinding = SuccessButtonBinding.inflate(layoutInflater)

        var status = STATUS_ONGOING

        val progressDialog = MaterialAlertDialogBuilder(this)
            .setView(dialogLoadingBinding.root)
            .setCancelable(false)
            .create()

        fun updateProgressDialogMessage(newMessage: String) {
            dialogLoadingBinding.loadingMessage.text = newMessage
            if (!progressDialog.isShowing) progressDialog.show() // 更新文本后才显示对话框
        }

        var link = ""
        val thread = Thread {
            var vSmall = versionSmall
            var v16codeStr = version16codeStr
            val guessNot5 = DataStoreUtil.getBoolean("guessNot5", false)
            val guessTestExtend = DataStoreUtil.getBoolean("guessTestExtend", false)
            val downloadOnSystemManager = DataStoreUtil.getBoolean("downloadOnSystemManager", false)
            val defineSuf = DataStoreUtil.getString("suffixDefine", "")
            val defineSufList = defineSuf.split(", ")
            val suf64hb =
                if (DataStoreUtil.getBoolean("suffix64HB", true)) listOf("_64_HB") else emptyList()
            val sufHb64 =
                if (DataStoreUtil.getBoolean("suffixHB64", true)) listOf("_HB_64") else emptyList()
            val suf64hb1 = if (DataStoreUtil.getBoolean(
                    "suffix64HB1", true
                )
            ) listOf("_64_HB1") else emptyList()
            val sufHb164 = if (DataStoreUtil.getBoolean(
                    "suffixHB164", true
                )
            ) listOf("_HB1_64") else emptyList()
            val suf64hb2 = if (DataStoreUtil.getBoolean(
                    "suffix64HB2", true
                )
            ) listOf("_64_HB2") else emptyList()
            val sufHb264 = if (DataStoreUtil.getBoolean(
                    "suffixHB264", true
                )
            ) listOf("_HB2_64") else emptyList()
            val suf64hb3 = if (DataStoreUtil.getBoolean(
                    "suffix64HB3", true
                )
            ) listOf("_64_HB3") else emptyList()
            val sufHb364 = if (DataStoreUtil.getBoolean(
                    "suffixHB364", true
                )
            ) listOf("_HB3_64") else emptyList()
            val suf64hd =
                if (DataStoreUtil.getBoolean("suffix64HD", true)) listOf("_64_HD") else emptyList()
            val sufHd64 =
                if (DataStoreUtil.getBoolean("suffixHD64", true)) listOf("_HD_64") else emptyList()
            val suf64hd1 = if (DataStoreUtil.getBoolean(
                    "suffix64HD1", true
                )
            ) listOf("_64_HD1") else emptyList()
            val sufHd164 = if (DataStoreUtil.getBoolean(
                    "suffixHD164", true
                )
            ) listOf("_HD1_64") else emptyList()
            val suf64hd2 = if (DataStoreUtil.getBoolean(
                    "suffix64HD2", true
                )
            ) listOf("_64_HD2") else emptyList()
            val sufHd264 = if (DataStoreUtil.getBoolean(
                    "suffixHD264", true
                )
            ) listOf("_HD2_64") else emptyList()
            val suf64hd3 = if (DataStoreUtil.getBoolean(
                    "suffix64HD3", true
                )
            ) listOf("_64_HD3") else emptyList()
            val sufHd364 = if (DataStoreUtil.getBoolean(
                    "suffixHD364", true
                )
            ) listOf("_HD3_64") else emptyList()
            val suf64hd1hb = if (DataStoreUtil.getBoolean(
                    "suffix64HD1HB", true
                )
            ) listOf("_64_HD1HB") else emptyList()
            val sufHd1hb64 = if (DataStoreUtil.getBoolean(
                    "suffixHD1HB64", true
                )
            ) listOf("_HD1HB_64") else emptyList()

            val stListPre = listOf("_64") + arrayListOf(
                suf64hb,
                suf64hb1,
                suf64hb2,
                suf64hb3,
                suf64hd,
                suf64hd1,
                suf64hd2,
                suf64hd3,
                suf64hd1hb,
                sufHb64,
                sufHb164,
                sufHb264,
                sufHb364,
                sufHd64,
                sufHd164,
                sufHd264,
                sufHd364,
                sufHd1hb64
            ).flatten()

            /*
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
             */
            val stList = if (defineSufList != listOf("")) stListPre + defineSufList else stListPre
            try {
                var sIndex = 0
                while (true) when (status) {
                    STATUS_ONGOING -> {
                        if (mode == MODE_TEST) {
                            if (link == "" || !guessTestExtend) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                if (guessTestExtend) sIndex += 1
                            } else {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}.${vSmall}${stList[sIndex]}.apk"
                                sIndex += 1
                            }
                        } else if (mode == MODE_UNOFFICIAL) link =
                            "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%20$versionBig.${vSmall}%2064.apk"
                        else if (mode == MODE_OFFICIAL) {
                            val soListPre = listOf(
                                "_64",
                                "_64_HB",
                                "_64_HB1",
                                "_64_HB2",
                                "_64_HB3",
                                "_HB_64",
                                "_HB1_64",
                                "_HB2_64",
                                "_HB3_64",
                                "_64_BBPJ",
                                "_BBPJ_64"
                            )
                            val soList =
                                if (defineSufList != listOf("")) soListPre + defineSufList else soListPre
                            if (link == "") {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}${soList[sIndex]}.apk"
                                sIndex += 1
                            } else if (sIndex == (soList.size)) {
                                status = STATUS_END
                                showToast("未猜测到包")
                                continue
                            } else {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}${soList[sIndex]}.apk"
                                sIndex += 1
                            }

                        } else if (mode == MODE_WECHAT) {
                            // https://dldir1.qq.com/weixin/android/weixin8049android2600_0x2800318a_arm64.apk
                            link =
                                "https://dldir1.qq.com/weixin/android/weixin${versionBig}android${versionTrue}_0x${v16codeStr}_arm64.apk"
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
                                        if (parent is ViewGroup) parent.removeView(
                                            successButtonBinding.root
                                        )
                                    }

                                    val successMaterialDialog = MaterialAlertDialogBuilder(this)
                                        .setTitle("猜测成功")
                                        .setMessage("下载地址：$link")
                                        .setIcon(R.drawable.check_circle)
                                        .setView(successButtonBinding.root)
                                        .setCancelable(false).apply {
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
                                        if (mode == MODE_TEST && (!guessTestExtend || sIndex == (stList.size))) {
                                            vSmall += if (!guessNot5) 5 else 1
                                            sIndex = 0
                                        } else if (mode == MODE_UNOFFICIAL) vSmall += if (!guessNot5) 5 else 1
                                        else if (mode == MODE_WECHAT) {
                                            val version16code = v16codeStr.toInt(16) + 1
                                            v16codeStr = version16code.toString(16)
                                        }
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
                                                    when (mode) {
                                                        MODE_OFFICIAL -> "Android QQ $versionBig 正式版（大小：$appSize MB）\n\n下载地址：$link"
                                                        MODE_WECHAT -> "Android 微信 $versionBig（$vSmall）（大小：$appSize MB）\n\n下载地址：$link"
                                                        else -> "Android QQ $versionBig.$vSmall 测试版（大小：$appSize MB）\n\n下载地址：$link\n\n鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                                    }
                                                } else {
                                                    when (mode) {
                                                        MODE_OFFICIAL -> "Android QQ $versionBig 正式版\n\n下载地址：$link"
                                                        MODE_WECHAT -> "Android 微信 $versionBig（$vSmall)\n\n下载地址：$link"
                                                        else -> "Android QQ $versionBig.$vSmall 测试版\n\n下载地址：$link\n\n鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                                    }
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
                                        successMaterialDialog.dismiss()
                                        status = STATUS_END
                                        if (downloadOnSystemManager) {
                                            val requestDownload =
                                                DownloadManager.Request(Uri.parse(link))
                                            requestDownload.apply {
                                                when (mode) {
                                                    MODE_TEST, MODE_UNOFFICIAL -> {
                                                        setDestinationInExternalPublicDir(
                                                            Environment.DIRECTORY_DOWNLOADS,
                                                            "Android_QQ_${versionBig}.${vSmall}_64.apk"
                                                        )
                                                    }

                                                    MODE_OFFICIAL -> {
                                                        setDestinationInExternalPublicDir(
                                                            Environment.DIRECTORY_DOWNLOADS,
                                                            "Android_QQ_${versionBig}_64.apk"
                                                        )
                                                    }

                                                    MODE_WECHAT -> {
                                                        setDestinationInExternalPublicDir(
                                                            Environment.DIRECTORY_DOWNLOADS,
                                                            "Android_微信_${versionBig}.${versionTrue}.apk"
                                                        )
                                                    }
                                                }
                                            }
                                            val downloadManager =
                                                getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                            downloadManager.enqueue(requestDownload)
                                        } else {
                                            // 这里不用 Chrome Custom Tab 的原因是 Chrome 不知道咋回事有概率卡在“等待下载”状态
                                            val browserIntent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                            browserIntent.apply {
                                                addCategory(Intent.CATEGORY_BROWSABLE)
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            startActivity(browserIntent)
                                        }
                                    }
                                }
                            }
                        } else {
                            if (mode == MODE_TEST && (!guessTestExtend || sIndex == (stList.size)) // 测试版情况下，未打开扩展猜版或扩展猜版到最后一步时执行小版本号的递增
                            ) {
                                vSmall += if (!guessNot5) 5 else 1
                                sIndex = 0
                            } else if (mode == MODE_UNOFFICIAL) vSmall += if (!guessNot5) 5 else 1
                            else if (mode == MODE_WECHAT) {
                                val version16code = v16codeStr.toInt(16) + 1
                                v16codeStr = version16code.toString(16)
                            }
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

    private fun tencentShiplyStart(
        btn: MaterialButton,
        shiplyVersion: String,
        shiplyUin: String,
        shiplyAppid: String = "537230561"
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            class MissingCipherTextException(message: String) : Exception(message)
            try {
                // 参考：https://github.com/callng/GQUL
                val shiplyKey = TencentShiplyUtil.generateAESKey()
                val shiplyData = TencentShiplyUtil.generateJsonString(
                    shiplyVersion, shiplyUin, shiplyAppid
                )
                val shiplyEncode = TencentShiplyUtil.aesEncrypt(shiplyData, shiplyKey)
                val shiplyRsaPublicKey =
                    TencentShiplyUtil.base64ToRsaPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/rT6ULqXC32dgz4t/Vv4WS9pTks5Z2fPmbTHIXEVeiOEnjOpPBHOi1AUz+Ykqjk11ZyjidUwDyIaC/VtaC5Z7Bt/W+CFluDer7LiiDa6j77if5dbcvWUrJbgvhKqaEhWnMDXT1pAG2KxL/pNFAYguSLpOh9pK97G8umUMkkwWkwIDAQAB")
                if (shiplyRsaPublicKey == null) runOnUiThread { showToast("生成 RSA 公钥失败") } // 应该不会失败吧
                else {
                    val shiplyEncode2 = TencentShiplyUtil.rsaEncrypt(
                        shiplyKey, shiplyRsaPublicKey
                    )
                    val shiplyPost = mapOf(
                        "req_list" to listOf(
                            mapOf(
                                "cipher_text" to Base64.encodeToString(
                                    shiplyEncode, Base64.NO_WRAP
                                ), "public_key_version" to 1, "pull_key" to Base64.encodeToString(
                                    shiplyEncode2, Base64.NO_WRAP
                                )
                            )
                        )
                    )
                    val shiplyResult = TencentShiplyUtil.postJsonWithOkHttp(
                        "https://rdelivery.qq.com/v3/config/batchpull", shiplyPost
                    )
                    val shiplyText = TencentShiplyUtil.getCipherText(shiplyResult)
                    if (!shiplyText.isNullOrEmpty()) {
                        val shiplyDecode = TencentShiplyUtil.aesDecrypt(
                            Base64.decode(shiplyText, Base64.NO_WRAP), shiplyKey
                        )
                        val gzipInputStream = GZIPInputStream(
                            ByteArrayInputStream(shiplyDecode)
                        )
                        val bufferedReader = BufferedReader(
                            InputStreamReader(gzipInputStream)
                        )
                        val decompressedStringBuilder = StringBuilder()

                        bufferedReader.lineSequence().forEach { line ->
                            decompressedStringBuilder.append(line)
                        }

                        val shiplyDecodeString = decompressedStringBuilder.toString()
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val shiplyDecodeStringJson =
                            gson.toJson(gson.fromJson(shiplyDecodeString, JsonElement::class.java))

                        runOnUiThread {
                            val dialogShiplyBackBinding =
                                DialogShiplyBackBinding.inflate(layoutInflater)

                            dialogShiplyBackBinding.root.parent?.let { parent ->
                                if (parent is ViewGroup) parent.removeView(dialogShiplyBackBinding.root)
                            }

                            val shiplyApkUrl =
                                shiplyDecodeStringJson.toPrettyFormat().getAllAPKUrl()

                            dialogShiplyBackBinding.apply {
                                MaterialAlertDialogBuilder(this@MainActivity).setView(
                                    dialogShiplyBackBinding.root
                                ).setTitle("Shiply 平台返回内容").setIcon(R.drawable.flask_line)
                                    .show().apply {
                                        shiplyUrlRecyclerView.layoutManager =
                                            LinearLayoutManager(this@MainActivity)
                                        if (shiplyApkUrl != null) {
                                            shiplyUrlBackTitle.visibility = View.VISIBLE
                                            shiplyUrlRecyclerView.visibility = View.VISIBLE
                                            shiplyUrlRecyclerView.adapter =
                                                ShiplyUrlListAdapter(shiplyApkUrl)
                                        } else {
                                            shiplyUrlBackTitle.visibility = View.GONE
                                            shiplyUrlRecyclerView.visibility = View.GONE
                                        }
                                        shiplyBackText.text =
                                            shiplyDecodeStringJson.toPrettyFormat()
                                    }
                            }
                        }
                    } else throw MissingCipherTextException("TDS 腾讯端服务 Shiply 发布平台返回 JSON 内容中未包含 \"cipher_text\" 键值对。")
                }
            } catch (e: MissingCipherTextException) {
                e.printStackTrace()
                dialogError(e, true)
            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            } finally {
                runOnUiThread {
                    btn.icon = null
                    btn.isEnabled = true
                }
            }
        }
    }


    companion object {
        const val STATUS_ONGOING = 0
        const val STATUS_PAUSE = 1
        const val STATUS_END = 2

        const val MODE_TEST = "测试版"
        const val MODE_OFFICIAL = "正式版"
        const val MODE_UNOFFICIAL = "空格猜版"
        const val MODE_WECHAT = "微信猜版"
    }

}
