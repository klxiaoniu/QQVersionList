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

package com.xiaoniu.qqversionlist.ui


import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
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
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.paris.extensions.style
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.xiaoniu.qqversionlist.BuildConfig
import com.xiaoniu.qqversionlist.QVTApplication.Companion.SHIPLY_DEFAULT_APPID
import com.xiaoniu.qqversionlist.QVTApplication.Companion.SHIPLY_DEFAULT_SDK_VERSION
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.databinding.DialogAboutBinding
import com.xiaoniu.qqversionlist.databinding.DialogGuessBinding
import com.xiaoniu.qqversionlist.databinding.DialogLoadingBinding
import com.xiaoniu.qqversionlist.databinding.DialogPersonalizationBinding
import com.xiaoniu.qqversionlist.databinding.DialogSettingBinding
import com.xiaoniu.qqversionlist.databinding.DialogShiplyBackBinding
import com.xiaoniu.qqversionlist.databinding.DialogShiplyBinding
import com.xiaoniu.qqversionlist.databinding.DialogSuffixDefineBinding
import com.xiaoniu.qqversionlist.databinding.SuccessButtonBinding
import com.xiaoniu.qqversionlist.databinding.UserAgreementBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.QQVersionBeanUtil.qqVersionBeanUtil
import com.xiaoniu.qqversionlist.util.ShiplyUtil
import com.xiaoniu.qqversionlist.util.StringUtil.getAllAPKUrl
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import com.xiaoniu.qqversionlist.util.TIMVersionBeanUtil.timVersionBeanUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.util.Locale
import java.util.zip.GZIPInputStream


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var qqVersionAdapter: QQVersionAdapter
    lateinit var timVersionAdapter: TIMVersionAdapter
    lateinit var localQQAdapter: LocalQQAdapter
    lateinit var localTIMAdapter: LocalTIMAdapter
    lateinit var qqVersion: List<QQVersionBean>
    lateinit var timVersion: List<TIMVersionBean>
    private lateinit var qqVersionListFragment: QQVersionListFragment
    private lateinit var timVersionListFragment: TIMVersionListFragment
    private lateinit var rvPagerAdapter: VersionListPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val viewRoot = binding.root
        setContentView(viewRoot)

        setContext(this)

        if (SDK_INT <= Build.VERSION_CODES.Q) {
            ViewCompat.setOnApplyWindowInsetsListener(viewRoot) { view, windowInsets ->
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
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        qqVersionAdapter = QQVersionAdapter()
        timVersionAdapter = TIMVersionAdapter()
        localQQAdapter = LocalQQAdapter()
        localTIMAdapter = LocalTIMAdapter()
        binding.rvPager.adapter = VersionListPagerAdapter(this)
        rvPagerAdapter = binding.rvPager.adapter as VersionListPagerAdapter
        qqVersionListFragment = QQVersionListFragment()
        timVersionListFragment = TIMVersionListFragment()
        initButtons()

        if (!BuildConfig.VERSION_NAME.endsWith("Release")) binding.materialToolbar.setNavigationIcon(
            R.drawable.git_commit_line
        )
    }

    /**
     * @param UATarget 用户协议版本
     **/
    private fun showUADialog(agreed: Boolean, UATarget: Int) {

        // 屏幕高度获取
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

        //用户协议，传参内容表示先前是否同意过协议
        //谁动了代码不动注释？

        val userAgreementBinding = UserAgreementBinding.inflate(layoutInflater)

        val dialogUA = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.userAgreement)
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
            DataStoreUtil.putIntKVAsync("userAgreement", UATarget)
            dialogUA.dismiss()
            getData()
        }

        userAgreementBinding.uaButtonDisagree.setOnClickListener {
            DataStoreUtil.putIntKVAsync("userAgreement", 0)
            finish()
        }
        if (agreed) userAgreementBinding.uaButtonDisagree.setText(R.string.withdrawConsentAndExit)

        dialogUA.show()
    }


    private fun initButtons() {
        // 删除 version Shared Preferences
        DataStoreUtil.deleteKVAsync("version")

        /**
         * 这里的 `val judgeUATarget = 2` 的值代表着用户协议修订版本，
         * 后续更新协议版本后也需要在下面一行把“judgeUATarget” + 1，以此类推
         **/
        val judgeUATarget = 2 // 2024.5.30 第二版
        if (DataStoreUtil.getIntKV("userAgreement", 0) < judgeUATarget) showUADialog(
            false, judgeUATarget
        )
        else getData()

        // 进度条动画
        // https://github.com/material-components/material-components-android/blob/master/docs/components/ProgressIndicator.md
        binding.progressLine.apply {
            showAnimationBehavior = LinearProgressIndicator.SHOW_NONE
            hideAnimationBehavior = LinearProgressIndicator.HIDE_ESCAPE
        }

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            //底部左下角按钮动作
            when (menuItem.itemId) {
                R.id.btn_get -> {
                    getData(menuItem)
                    true
                }

                R.id.btn_about -> {
                    val dialogAboutBinding = DialogAboutBinding.inflate(layoutInflater)

                    dialogAboutBinding.apply {

                        val aboutDialog = MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle(R.string.about)
                            .setIcon(R.drawable.information_line)
                            .setView(root)
                            .show().apply {
                                aboutText.movementMethod =
                                    LinkMovementMethodCompat.getInstance()

                                aboutText.text = SpannableString(
                                    "${getString(R.string.aboutAppName)}\n\n" +
                                            "${getString(R.string.aboutDescription)}\n\n" +
                                            "${getString(R.string.version)}${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n" +
                                            "${getString(R.string.aboutAuthor)}快乐小牛、有鲫雪狐\n" +
                                            "${getString(R.string.aboutContributor)}Col_or、bggRGjQaUbCoE、GMerge、zwJimRaynor\n" +
                                            "${getString(R.string.aboutSpecialThanksTo)}owo233\n" +
                                            "${getString(R.string.aboutOpenSourceRepo)}GitHub\n" +
                                            "${getString(R.string.aboutGetUpdate)}GitHub Releases、Obtainium、九七通知中心\n" +
                                            "${getString(R.string.facilitateI18n)}Crowdin\n\n" +
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
                                        URLSpan("https://github.com/zwJimRaynor"),
                                        indexOf("zwJimRaynor"),
                                        indexOf("zwJimRaynor") + "zwJimRaynor".length,
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
                                    setSpan(
                                        URLSpan("https://crowdin.com/project/qqversionstool"),
                                        indexOf("Crowdin"),
                                        indexOf("Crowdin") + "Crowdin".length,
                                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                }
                            }

                        btnAboutWithdrawConsentUA.setOnClickListener {
                            showUADialog(true, judgeUATarget)
                            aboutDialog.dismiss()
                        }

                        btnAboutOk.setOnClickListener {
                            aboutDialog.dismiss()
                        }
                    }

                    true
                }

                R.id.btn_setting -> {
                    val dialogSettingBinding = DialogSettingBinding.inflate(layoutInflater)

                    dialogSettingBinding.apply {
                        longPressCard.isChecked = DataStoreUtil.getBooleanKV("longPressCard", true)
                        guessNot5.isChecked = DataStoreUtil.getBooleanKV("guessNot5", false)
                        switchGuessTestExtend.isChecked =
                            DataStoreUtil.getBooleanKV("guessTestExtend", false) // 扩展测试版猜版格式
                        downloadOnSystemManager.isChecked =
                            DataStoreUtil.getBooleanKV("downloadOnSystemManager", false)
                    }

                    val dialogSetting = MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.setting)
                        .setIcon(R.drawable.settings_line)
                        .setView(dialogSettingBinding.root)
                        .show()

                    dialogSettingBinding.apply {
                        btnSettingOk.setOnClickListener {
                            dialogSetting.dismiss()
                        }
                        longPressCard.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync("longPressCard", isChecked)
                        }
                        guessNot5.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync("guessNot5", isChecked)
                        }
                        dialogPersonalization.setOnClickListener {
                            val dialogPersonalization =
                                DialogPersonalizationBinding.inflate(layoutInflater).apply {
                                    root.parent?.let { parent ->
                                        if (parent is ViewGroup) {
                                            parent.removeView(root)
                                        }
                                    }

                                    versionTcloudThickness.setEnabled(
                                        DataStoreUtil.getBooleanKV(
                                            "versionTCloud", true
                                        )
                                    )

                                    versionTcloudThickness.value = when (DataStoreUtil.getStringKV(
                                        "versionTCloudThickness", "System"
                                    )) {
                                        "Light" -> 1.0f
                                        "Regular" -> 2.0f
                                        "Bold" -> 3.0f
                                        else -> 4.0f
                                    }
                                }

                            val dialogPer = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(R.string.personalization)
                                .setIcon(R.drawable.palette_line)
                                .setView(dialogPersonalization.root)
                                .show()

                            dialogPersonalization.apply {
                                switchDisplayFirst.isChecked =
                                    DataStoreUtil.getBooleanKV("displayFirst", true)
                                progressSize.isChecked =
                                    DataStoreUtil.getBooleanKV("progressSize", false)
                                versionTcloud.isChecked =
                                    DataStoreUtil.getBooleanKV("versionTCloud", true)

                                switchDisplayFirst.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKVAsync("displayFirst", isChecked)
                                    qqVersion = qqVersion.mapIndexed { index, qqVersionBean ->
                                        if (index == 0) qqVersionBean.copy(
                                            displayType = if (isChecked) 1 else 0
                                        )
                                        else qqVersionBean
                                    }
                                    timVersion = timVersion.mapIndexed { index, timVersionBean ->
                                        if (index == 0) timVersionBean.copy(
                                            displayType = if (isChecked) 1 else 0
                                        )
                                        else timVersionBean
                                    }
                                    qqVersionAdapter.submitList(qqVersion)
                                    timVersionAdapter.submitList(timVersion)
                                }

                                // 下两个设置不能异步持久化存储，否则视图更新读不到更新值
                                progressSize.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("progressSize", isChecked)
                                    qqVersionAdapter.updateItemProperty("isShowProgressSize")
                                }
                                versionTcloud.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("versionTCloud", isChecked)
                                    dialogPersonalization.versionTcloudThickness.setEnabled(
                                        isChecked
                                    )
                                    qqVersionAdapter.updateItemProperty("isTCloud")
                                    timVersionAdapter.updateItemProperty("isTCloud")
                                }
                                btnPersonalizationOk.setOnClickListener {
                                    dialogPer.dismiss()
                                }

                                versionTcloudThickness.setLabelFormatter {
                                    return@setLabelFormatter when (it) {
                                        1.0f -> "Light"
                                        2.0f -> "Regular"
                                        3.0f -> "Bold"
                                        else -> getString(R.string.thicknessFollowSystem)
                                    }
                                }

                                versionTcloudThickness.addOnChangeListener { _, value, _ ->
                                    when (value) {
                                        1.0f -> DataStoreUtil.putStringKV(
                                            "versionTCloudThickness", "Light"
                                        )

                                        2.0f -> DataStoreUtil.putStringKV(
                                            "versionTCloudThickness", "Regular"
                                        )

                                        3.0f -> DataStoreUtil.putStringKV(
                                            "versionTCloudThickness", "Bold"
                                        )

                                        else -> DataStoreUtil.putStringKV(
                                            "versionTCloudThickness", "System"
                                        )
                                    }
                                    qqVersionAdapter.updateItemProperty("isTCloud")
                                    timVersionAdapter.updateItemProperty("isTCloud")
                                }
                            }
                        }

                        switchGuessTestExtend.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync("guessTestExtend", isChecked)
                        }
                        downloadOnSystemManager.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync("downloadOnSystemManager", isChecked)
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
                                .setTitle(R.string.enumerateVersionsSuffixSetting)
                                .setIcon(R.drawable.settings_line)
                                .setView(dialogSuffixDefine.root)
                                .setCancelable(false)
                                .create()

                            dialogSuffixDefine.apply {
                                DataStoreUtil.apply {
                                    suffixDefineCheckbox64hb.isChecked =
                                        getBooleanKV("suffix64HB", true)
                                    suffixDefineCheckboxHb64.isChecked =
                                        getBooleanKV("suffixHB64", true)
                                    suffixDefineCheckbox64hb1.isChecked =
                                        getBooleanKV("suffix64HB1", true)
                                    suffixDefineCheckboxHb164.isChecked =
                                        getBooleanKV("suffixHB164", true)
                                    suffixDefineCheckbox64hb2.isChecked =
                                        getBooleanKV("suffix64HB2", true)
                                    suffixDefineCheckboxHb264.isChecked =
                                        getBooleanKV("suffixHB264", true)
                                    suffixDefineCheckbox64hb3.isChecked =
                                        getBooleanKV("suffix64HB3", true)
                                    suffixDefineCheckboxHb364.isChecked =
                                        getBooleanKV("suffixHB364", true)
                                    suffixDefineCheckbox64hd.isChecked =
                                        getBooleanKV("suffix64HD", true)
                                    suffixDefineCheckboxHd64.isChecked =
                                        getBooleanKV("suffixHD64", true)
                                    suffixDefineCheckbox64hd1.isChecked =
                                        getBooleanKV("suffix64HD1", true)
                                    suffixDefineCheckboxHd164.isChecked =
                                        getBooleanKV("suffixHD164", true)
                                    suffixDefineCheckbox64hd2.isChecked =
                                        getBooleanKV("suffix64HD2", true)
                                    suffixDefineCheckboxHd264.isChecked =
                                        getBooleanKV("suffixHD264", true)
                                    suffixDefineCheckbox64hd3.isChecked =
                                        getBooleanKV("suffix64HD3", true)
                                    suffixDefineCheckboxHd364.isChecked =
                                        getBooleanKV("suffixHD364", true)
                                    suffixDefineCheckbox64hd1hb.isChecked =
                                        getBooleanKV("suffix64HD1HB", true)
                                    suffixDefineCheckboxHd1hb64.isChecked =
                                        getBooleanKV("suffixHD1HB64", true)
                                    suffixDefineCheckboxTest.isChecked =
                                        getBooleanKV("suffixTest", true)
                                }

                                dialogSuffix.show()

                                // 异步读取字符串，防止超长字符串造成阻塞
                                settingSuffixDefine.apply {
                                    isEnabled = false
                                    btnSuffixSave.isEnabled = false
                                    lifecycleScope.launch {
                                        val suffixDefine = withContext(Dispatchers.IO) {
                                            DataStoreUtil.getStringKVAsync("suffixDefine", "")
                                                .await()
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

                                    val suffixDataStoreList = listOf(
                                        mapOf(
                                            "key" to "suffixDefine",
                                            "value" to suffixDefine,
                                            "type" to "String"
                                        ), mapOf(
                                            "key" to "suffix64HB",
                                            "value" to suffixDefineCheckbox64hb.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHB64",
                                            "value" to suffixDefineCheckboxHb64.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HB1",
                                            "value" to suffixDefineCheckbox64hb1.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHB164",
                                            "value" to suffixDefineCheckboxHb164.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HB2",
                                            "value" to suffixDefineCheckbox64hb2.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHB264",
                                            "value" to suffixDefineCheckboxHb264.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HB3",
                                            "value" to suffixDefineCheckbox64hb3.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHB364",
                                            "value" to suffixDefineCheckboxHb364.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HD",
                                            "value" to suffixDefineCheckbox64hd.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHD64",
                                            "value" to suffixDefineCheckboxHd64.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HD1",
                                            "value" to suffixDefineCheckbox64hd1.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHD164",
                                            "value" to suffixDefineCheckboxHd164.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HD2",
                                            "value" to suffixDefineCheckbox64hd2.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHD264",
                                            "value" to suffixDefineCheckboxHd264.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HD3",
                                            "value" to suffixDefineCheckbox64hd3.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHD364",
                                            "value" to suffixDefineCheckboxHd364.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixHD1HB64",
                                            "value" to suffixDefineCheckboxHd1hb64.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffix64HD1HB",
                                            "value" to suffixDefineCheckbox64hd1hb.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "suffixTest",
                                            "value" to suffixDefineCheckboxTest.isChecked,
                                            "type" to "Boolean"
                                        )
                                    )

                                    DataStoreUtil.batchPutKVAsync(suffixDataStoreList)
                                    showToast(getString(R.string.saved))
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
                    val dialogShiplyBinding = DialogShiplyBinding.inflate(layoutInflater)

                    val shiplyDialog = MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.getUpdateFromShiplyPlatform)
                        .setIcon(R.drawable.flask_line)
                        .setView(dialogShiplyBinding.root)
                        .setCancelable(false)
                        .show()

                    dialogShiplyBinding.apply {
                        DataStoreUtil.apply {
                            shiplyUin.editText?.setText(getStringKV("shiplyUin", ""))
                            shiplyVersion.editText?.setText(
                                getStringKV("shiplyVersion", "")
                            )
                            switchShiplyAdvancedConfigurations.isChecked =
                                getBooleanKV("shiplyAdvancedConfigurations", false)
                        }

                        switchShiplyAdvancedConfigurations.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync(
                                "shiplyAdvancedConfigurations",
                                isChecked
                            )
                        }

                        shiplyAdvancedConfigurationsClick.setOnClickListener {
                            ShiplyAdvancedConfigSheetFragment().apply {
                                isCancelable = false
                                show(supportFragmentManager, ShiplyAdvancedConfigSheetFragment.TAG)
                            }
                        }

                        btnShiplyCancel.setOnClickListener {
                            shiplyDialog.dismiss()
                        }

                        btnShiplyStart.setOnClickListener {
                            shiplyUin.clearFocus()
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
                                        this@MainActivity, spec
                                    )

                                btnShiplyStart.isEnabled = false
                                btnShiplyStart.style(com.google.android.material.R.style.Widget_Material3_Button_Icon)
                                btnShiplyStart.icon = progressIndicatorDrawable

                                if (shiplyUin.editText?.text.toString()
                                        .isEmpty()
                                ) throw MissingParameterException("uin 信息是用于请求 TDS 腾讯端服务 Shiply 发布平台的对象 QQ 号，缺失 uin 参数将无法获取 Shiply 平台返回数据。")
                                if (shiplyVersion.editText?.text.toString()
                                        .isEmpty()
                                ) throw MissingParameterException("请求 TDS 腾讯端服务 Shiply 发布平台需要 QQ 版本号参数，缺失版本号参数将无法获取 Shiply 平台返回数据。")

                                if (!DataStoreUtil.getBooleanKV(
                                        "shiplyAdvancedConfigurations", false
                                    )
                                ) {
                                    DataStoreUtil.apply {
                                        putStringKVAsync(
                                            "shiplyVersion", shiplyVersion.editText?.text.toString()
                                        )
                                        putStringKVAsync(
                                            "shiplyUin", shiplyUin.editText?.text.toString()
                                        )
                                    }
                                    tencentShiplyStart(
                                        btnShiplyStart,
                                        shiplyVersion.editText?.text.toString(),
                                        shiplyUin.editText?.text.toString()
                                    )
                                } else DataStoreUtil.apply {
                                    putStringKVAsync(
                                        "shiplyVersion", shiplyVersion.editText?.text.toString()
                                    )
                                    putStringKVAsync(
                                        "shiplyUin", shiplyUin.editText?.text.toString()
                                    )
                                    tencentShiplyStart(btnShiplyStart,
                                        shiplyVersion.editText?.text.toString(),
                                        shiplyUin.editText?.text.toString(),
                                        getStringKV(
                                            "shiplyAppid", ""
                                        ).ifEmpty { SHIPLY_DEFAULT_APPID },
                                        getStringKV(
                                            "shiplyOsVersion", ""
                                        ).ifEmpty { SDK_INT.toString() },
                                        getStringKV(
                                            "shiplyModel", ""
                                        ).ifEmpty { Build.MODEL.toString() },
                                        getStringKV(
                                            "shiplySdkVersion", ""
                                        ).ifEmpty { SHIPLY_DEFAULT_SDK_VERSION },
                                        getStringKV(
                                            "shiplyLanguage", ""
                                        ).ifEmpty { Locale.getDefault().language.toString() })
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

        if (intent.action == "android.intent.action.VIEW" && DataStoreUtil.getIntKV(
                "userAgreement", 0
            ) >= judgeUATarget
        ) showGuessVersionDialog()
        binding.btnGuess.setOnClickListener {
            showGuessVersionDialog()
        }

    }

    // 下面三个函数是用于响应猜版对话框 Spinner 所选项的界面变化
    private fun modeTestView(dialogGuessBinding: DialogGuessBinding) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = true
            etVersionSmall.visibility = View.VISIBLE
            guessDialogWarning.visibility = View.VISIBLE
            etVersion16code.visibility = View.GONE
            etVersionTrue.visibility = View.GONE
            tvWarning.setText(R.string.enumQQPreviewWarning)
            etVersionBig.helperText = getString(R.string.enumQQMajorVersionHelpText)
        }
    }

    private fun modeOfficialView(dialogGuessBinding: DialogGuessBinding) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            etVersionSmall.visibility = View.VISIBLE
            guessDialogWarning.visibility = View.GONE
            etVersion16code.visibility = View.GONE
            etVersionTrue.visibility = View.GONE
            etVersionBig.helperText = getString(R.string.enumQQMajorVersionHelpText)
        }
    }

    private fun modeWeChatView(dialogGuessBinding: DialogGuessBinding) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            guessDialogWarning.visibility = View.VISIBLE
            etVersionSmall.visibility = View.GONE
            etVersionTrue.visibility = View.VISIBLE
            etVersion16code.visibility = View.VISIBLE
            tvWarning.setText(R.string.enumWeixinWarning)
            etVersionBig.helperText = getString(R.string.enumWeixinMajorVersionHelpText)
        }
    }

    private fun showGuessVersionDialog() {
        val dialogGuessBinding = DialogGuessBinding.inflate(layoutInflater)
        val verBig = DataStoreUtil.getStringKV("versionBig", "")
        dialogGuessBinding.etVersionBig.editText?.setText(verBig)
        when (val memVersion = DataStoreUtil.getStringKV("versionSelect", MODE_OFFICIAL)) {
            MODE_TEST, MODE_UNOFFICIAL, MODE_OFFICIAL, MODE_WECHAT -> dialogGuessBinding.spinnerVersion.setText(
                memVersion, false
            )

            else -> {
                dialogGuessBinding.spinnerVersion.setText(MODE_OFFICIAL, false)
                DataStoreUtil.putStringKVAsync("versionSelect", MODE_OFFICIAL)
            }
        }

        when (dialogGuessBinding.spinnerVersion.text.toString()) {
            MODE_TEST, MODE_UNOFFICIAL -> modeTestView(dialogGuessBinding)
            MODE_OFFICIAL -> modeOfficialView(dialogGuessBinding)
            MODE_WECHAT -> modeWeChatView(dialogGuessBinding)
        }


        dialogGuessBinding.spinnerVersion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val judgeVerSelect = dialogGuessBinding.spinnerVersion.text.toString()
                DataStoreUtil.putStringKVAsync("versionSelect", judgeVerSelect)
                when (judgeVerSelect) {
                    MODE_TEST, MODE_UNOFFICIAL -> modeTestView(dialogGuessBinding)
                    MODE_OFFICIAL -> modeOfficialView(dialogGuessBinding)
                    MODE_WECHAT -> modeWeChatView(dialogGuessBinding)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        val dialogGuess = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.enumerateVersionsDialogTitle)
            .setIcon(R.drawable.scan_line)
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
                ) throw MissingVersionException(getString(R.string.missingMajorVersionWarning))
                val versionBig = dialogGuessBinding.etVersionBig.editText?.text.toString()
                val mode = dialogGuessBinding.spinnerVersion.text.toString()
                var versionSmall = 0
                var version16code = 0.toString()
                var versionTrue = 0
                when (mode) {
                    MODE_TEST, MODE_UNOFFICIAL -> if (dialogGuessBinding.etVersionSmall.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        getString(R.string.missingMajorVersionWarning)
                    ) else {
                        versionSmall =
                            dialogGuessBinding.etVersionSmall.editText?.text.toString().toInt()
                        if (versionSmall % 5 != 0 && !DataStoreUtil.getBooleanKV(
                                "guessNot5", false
                            )
                        ) throw InvalidMultipleException(getString(R.string.QQPreviewMinorNot5Warning))
                        if (versionSmall != 0) DataStoreUtil.putIntKVAsync(
                            "versionSmall", versionSmall
                        )
                    }

                    MODE_WECHAT -> if (dialogGuessBinding.etVersionTrue.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        getString(R.string.missingWeixinTrueVersionWarning)
                    ) else if (dialogGuessBinding.etVersion16code.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        getString(R.string.missingWeixin16CodeWarning)
                    ) else {
                        versionTrue =
                            dialogGuessBinding.etVersionTrue.editText?.text.toString().toInt()
                        version16code =
                            dialogGuessBinding.etVersion16code.editText?.text.toString()
                        if (version16code != 0.toString()) DataStoreUtil.putStringKVAsync(
                            "version16code", version16code
                        )
                        if (versionTrue != 0) DataStoreUtil.putIntKVAsync(
                            "versionTrue", versionTrue
                        )
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

        val memVersionSmall = DataStoreUtil.getIntKV("versionSmall", -1)
        if (memVersionSmall != -1) dialogGuessBinding.etVersionSmall.editText?.setText(
            memVersionSmall.toString()
        )
        val memVersion16code = DataStoreUtil.getStringKV("version16code", "-1")
        if (memVersion16code != "-1") dialogGuessBinding.etVersion16code.editText?.setText(
            memVersion16code
        )
        val memVersionTrue = DataStoreUtil.getIntKV("versionTrue", -1)
        if (memVersionTrue != -1) dialogGuessBinding.etVersionTrue.editText?.setText(memVersionTrue.toString())
    }


    @SuppressLint("SetTextI18n")
    private fun getData(menu: MenuItem? = null) {
        binding.progressLine.show()
        if (menu != null) menu.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 识别本机 Android QQ 版本并放进持久化存储
                val QQVersionInstall =
                    packageManager.getPackageInfo("com.tencent.mobileqq", 0).versionName.toString()
                val QQVersionCodeInstall =
                    if (SDK_INT >= Build.VERSION_CODES.P) packageManager.getPackageInfo(
                        "com.tencent.mobileqq", 0
                    ).longVersionCode.toString() else ""
                val QQMetaDataInstall = packageManager.getPackageInfo(
                    "com.tencent.mobileqq", PackageManager.GET_META_DATA
                )
                val QQAppSettingParamsInstall =
                    QQMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params")
                val QQAppSettingParamsPadInstall =
                    QQMetaDataInstall.applicationInfo?.metaData?.getString("AppSetting_params_pad")
                val QQRdmUUIDInstall =
                    QQMetaDataInstall.applicationInfo?.metaData?.getString("com.tencent.rdm.uuid")
                val QQTargetInstall = QQMetaDataInstall.applicationInfo?.targetSdkVersion.toString()
                val QQMinInstall = QQMetaDataInstall.applicationInfo?.minSdkVersion.toString()
                val QQCompileInstall = (if (SDK_INT >= Build.VERSION_CODES.S)
                    QQMetaDataInstall.applicationInfo?.compileSdkVersion.toString() else "")
                if (QQVersionInstall != DataStoreUtil.getStringKV(
                        "QQVersionInstall", ""
                    )
                ) DataStoreUtil.putStringKV("QQVersionInstall", QQVersionInstall)
                if (QQVersionCodeInstall != DataStoreUtil.getStringKV(
                        "QQVersionCodeInstall", ""
                    )
                ) DataStoreUtil.putStringKV("QQVersionCodeInstall", QQVersionCodeInstall)
                if (QQAppSettingParamsInstall != null && QQAppSettingParamsInstall != DataStoreUtil.getStringKV(
                        "QQAppSettingParamsInstall", ""
                    )
                ) DataStoreUtil.putStringKV("QQAppSettingParamsInstall", QQAppSettingParamsInstall)
                if (QQAppSettingParamsPadInstall != null && QQAppSettingParamsPadInstall != DataStoreUtil.getStringKV(
                        "QQAppSettingParamsPadInstall", ""
                    )
                ) DataStoreUtil.putStringKV(
                    "QQAppSettingParamsPadInstall", QQAppSettingParamsPadInstall
                )
                if (QQRdmUUIDInstall != null && QQRdmUUIDInstall != DataStoreUtil.getStringKV(
                        "QQRdmUUIDInstall", ""
                    )
                ) DataStoreUtil.putStringKV("QQRdmUUIDInstall", QQRdmUUIDInstall)
                if (QQTargetInstall.isNotEmpty() && QQTargetInstall != DataStoreUtil.getStringKV(
                        "QQTargetInstall",
                        ""
                    )
                ) DataStoreUtil.putStringKV("QQTargetInstall", QQTargetInstall)
                if (QQMinInstall.isNotEmpty() && QQMinInstall != DataStoreUtil.getStringKV(
                        "QQMinInstall",
                        ""
                    )
                ) DataStoreUtil.putStringKV("QQMinInstall", QQMinInstall)
                if (QQCompileInstall.isNotEmpty() && QQCompileInstall != DataStoreUtil.getStringKV(
                        "QQCompileInstall",
                        ""
                    )
                ) DataStoreUtil.putStringKV("QQCompileInstall", QQCompileInstall)
            } catch (_: Exception) {
                val localQQEmptyList = listOf(
                    mapOf("key" to "QQVersionInstall", "value" to "", "type" to "String"),
                    mapOf("key" to "QQVersionCodeInstall", "value" to "", "type" to "String"),
                    mapOf("key" to "QQAppSettingParamsInstall", "value" to "", "type" to "String"),
                    mapOf(
                        "key" to "QQAppSettingParamsPadInstall",
                        "value" to "",
                        "type" to "String"
                    )
                )
                DataStoreUtil.batchPutKVAsync(localQQEmptyList)
            } finally {
                try {
                    // 识别本机 Android TIM 版本并放进持久化存储
                    val TIMVersionInstall =
                        packageManager.getPackageInfo("com.tencent.tim", 0).versionName.toString()
                    val TIMVersionCodeInstall =
                        if (SDK_INT >= Build.VERSION_CODES.P) packageManager.getPackageInfo(
                            "com.tencent.tim", 0
                        ).longVersionCode.toString() else ""
                    val TIMMetaData = packageManager.getPackageInfo(
                        "com.tencent.tim", PackageManager.GET_META_DATA
                    )
                    val TIMAppSettingParamsInstall =
                        TIMMetaData.applicationInfo?.metaData?.getString("AppSetting_params")
                    val TIMRdmUUIDInstall =
                        TIMMetaData.applicationInfo?.metaData?.getString("com.tencent.rdm.uuid")
                    val TIMTargetInstall = TIMMetaData.applicationInfo?.targetSdkVersion.toString()
                    val TIMMinInstall = TIMMetaData.applicationInfo?.minSdkVersion.toString()
                    val TIMCompileInstall =
                        (if (SDK_INT >= Build.VERSION_CODES.S) TIMMetaData.applicationInfo?.compileSdkVersion.toString() else "")
                    if (TIMTargetInstall.isNotEmpty() && TIMTargetInstall != DataStoreUtil.getStringKV(
                            "TIMTargetInstall",
                            ""
                        )
                    ) DataStoreUtil.putStringKV("TIMTargetInstall", TIMTargetInstall)
                    if (TIMMinInstall.isNotEmpty() && TIMMinInstall != DataStoreUtil.getStringKV(
                            "TIMMinInstall",
                            ""
                        )
                    ) DataStoreUtil.putStringKV("TIMMinInstall", TIMMinInstall)
                    if (TIMCompileInstall.isNotEmpty() && TIMCompileInstall != DataStoreUtil.getStringKV(
                            "TIMCompileInstall",
                            ""
                        )
                    ) DataStoreUtil.putStringKV("TIMCompileInstall", TIMCompileInstall)
                    if (TIMVersionInstall != DataStoreUtil.getStringKV(
                            "TIMVersionInstall", ""
                        )
                    ) DataStoreUtil.putStringKV("TIMVersionInstall", TIMVersionInstall)
                    if (TIMVersionCodeInstall != DataStoreUtil.getStringKV(
                            "TIMVersionCodeInstall", ""
                        )
                    ) DataStoreUtil.putStringKV("TIMVersionCodeInstall", TIMVersionCodeInstall)
                    if (TIMAppSettingParamsInstall != null && TIMAppSettingParamsInstall != DataStoreUtil.getStringKV(
                            "TIMAppSettingParamsInstall", ""
                        )
                    ) DataStoreUtil.putStringKV(
                        "TIMAppSettingParamsInstall", TIMAppSettingParamsInstall
                    )
                    if (TIMRdmUUIDInstall != null && TIMRdmUUIDInstall != DataStoreUtil.getStringKV(
                            "TIMRdmUUIDInstall", ""
                        )
                    ) DataStoreUtil.putStringKV("TIMRdmUUIDInstall", TIMRdmUUIDInstall)
                } catch (_: Exception) {
                    val localTIMEmptyList = listOf(
                        mapOf("key" to "TIMVersionInstall", "value" to "", "type" to "String"),
                        mapOf("key" to "TIMVersionCodeInstall", "value" to "", "type" to "String"),
                        mapOf(
                            "key" to "TIMAppSettingParamsInstall",
                            "value" to "",
                            "type" to "String"
                        )
                    )
                    DataStoreUtil.batchPutKVAsync(localTIMEmptyList)
                } finally {
                    var progressFlag = 0
                    withContext(Dispatchers.Main) {
                        localQQAdapter.refreshData()
                        localTIMAdapter.refreshData()
                    }
                    fun endProgress() {
                        if (progressFlag == 0) progressFlag = 1
                        else {
                            binding.progressLine.hide()
                            if (menu != null) menu.isEnabled = true
                        }
                    }
                    try {
                        val okHttpClient = OkHttpClient()
                        val request =
                            Request.Builder().url("https://im.qq.com/rainbow/androidQQVersionList")
                                .build()
                        val response = okHttpClient.newCall(request).execute()
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            qqVersionBeanUtil(this@MainActivity, responseData)
                            withContext(Dispatchers.Main) {
                                qqVersionAdapter.submitList(qqVersion)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dialogError(e)
                    } finally {
                        withContext(Dispatchers.Main) { endProgress() }
                    }
                    try {
                        val okHttpClient = OkHttpClient()
                        val request =
                            Request.Builder().url("https://im.qq.com/rainbow/TIMDownload/")
                                .build()
                        val response = okHttpClient.newCall(request).execute()
                        val responseData = response.body?.string()
                        if (responseData != null) {
                            timVersionBeanUtil(this@MainActivity, responseData)
                            withContext(Dispatchers.Main) {
                                timVersionAdapter.submitList(timVersion)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dialogError(e)
                    } finally {
                        withContext(Dispatchers.Main) { endProgress() }
                    }
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
            val guessNot5 = DataStoreUtil.getBooleanKV("guessNot5", false)
            val guessTestExtend = DataStoreUtil.getBooleanKV("guessTestExtend", false)
            val downloadOnSystemManager =
                DataStoreUtil.getBooleanKV("downloadOnSystemManager", false)
            val defineSufList = DataStoreUtil.getStringKV("suffixDefine", "").split(", ")
            val suf64hb =
                if (DataStoreUtil.getBooleanKV(
                        "suffix64HB",
                        true
                    )
                ) listOf("_64_HB") else emptyList()
            val sufHb64 =
                if (DataStoreUtil.getBooleanKV(
                        "suffixHB64",
                        true
                    )
                ) listOf("_HB_64") else emptyList()
            val suf64hb1 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HB1", true
                )
            ) listOf("_64_HB1") else emptyList()
            val sufHb164 = if (DataStoreUtil.getBooleanKV(
                    "suffixHB164", true
                )
            ) listOf("_HB1_64") else emptyList()
            val suf64hb2 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HB2", true
                )
            ) listOf("_64_HB2") else emptyList()
            val sufHb264 = if (DataStoreUtil.getBooleanKV(
                    "suffixHB264", true
                )
            ) listOf("_HB2_64") else emptyList()
            val suf64hb3 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HB3", true
                )
            ) listOf("_64_HB3") else emptyList()
            val sufHb364 = if (DataStoreUtil.getBooleanKV(
                    "suffixHB364", true
                )
            ) listOf("_HB3_64") else emptyList()
            val suf64hd =
                if (DataStoreUtil.getBooleanKV(
                        "suffix64HD",
                        true
                    )
                ) listOf("_64_HD") else emptyList()
            val sufHd64 =
                if (DataStoreUtil.getBooleanKV(
                        "suffixHD64",
                        true
                    )
                ) listOf("_HD_64") else emptyList()
            val suf64hd1 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HD1", true
                )
            ) listOf("_64_HD1") else emptyList()
            val sufHd164 = if (DataStoreUtil.getBooleanKV(
                    "suffixHD164", true
                )
            ) listOf("_HD1_64") else emptyList()
            val suf64hd2 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HD2", true
                )
            ) listOf("_64_HD2") else emptyList()
            val sufHd264 = if (DataStoreUtil.getBooleanKV(
                    "suffixHD264", true
                )
            ) listOf("_HD2_64") else emptyList()
            val suf64hd3 = if (DataStoreUtil.getBooleanKV(
                    "suffix64HD3", true
                )
            ) listOf("_64_HD3") else emptyList()
            val sufHd364 = if (DataStoreUtil.getBooleanKV(
                    "suffixHD364", true
                )
            ) listOf("_HD3_64") else emptyList()
            val suf64hd1hb = if (DataStoreUtil.getBooleanKV(
                    "suffix64HD1HB", true
                )
            ) listOf("_64_HD1HB") else emptyList()
            val sufHd1hb64 = if (DataStoreUtil.getBooleanKV(
                    "suffixHD1HB64", true
                )
            ) listOf("_HD1HB_64") else emptyList()
            val sufTest =
                if (DataStoreUtil.getBooleanKV("suffixTest", true)) listOf("_test") else emptyList()

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
                sufHd1hb64,
                sufTest
            ).flatten()

            // ["_64", "_64_HB", "_64_HB1", "_64_HB2", "_64_HB3", "_64_HD", "_64_HD1", "_64_HD2", "_64_HD3", "_64_HD1HB", "_HB_64", "_HB1_64", "_HB2_64", "_HB3_64", "_HD_64", "_HD1_64", "_HD2_64", "_HD3_64", "_HD1HB_64", "_test"]

            val stList = if (defineSufList != listOf("")) stListPre + defineSufList else stListPre
            try {
                var sIndex = 0
                while (true) when (status) {
                    STATUS_ONGOING -> {
                        when (mode) {
                            MODE_TEST -> if (link == "" || !guessTestExtend) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                if (guessTestExtend) sIndex += 1
                            } else {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}.${vSmall}${stList[sIndex]}.apk"
                                sIndex += 1
                            }

                            MODE_UNOFFICIAL -> link =
                                "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%20$versionBig.${vSmall}%2064.apk"

                            MODE_OFFICIAL -> {
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

                            }

                            MODE_WECHAT -> link =
                                "https://dldir1.qq.com/weixin/android/weixin${versionBig}android${versionTrue}_0x${v16codeStr}_arm64.apk"
                            // https://dldir1.qq.com/weixin/android/weixin8049android2600_0x2800318a_arm64.apk
                        }
                        runOnUiThread {
                            updateProgressDialogMessage("${getString(R.string.enumeratingDownloadLink)}$link")
                        }
                        val okHttpClient = OkHttpClient()
                        val request = Request.Builder().url(link).head().build()
                        val response = okHttpClient.newCall(request).execute()
                        val responseContentType = response.header("Content-Type").toString()
                        if (response.isSuccessful && responseContentType.startsWith("application/")) {
                            val appSize = "%.2f".format(
                                response.header("Content-Length")!!.toDouble().div(1024 * 1024)
                            )
                            status = STATUS_PAUSE
                            runOnUiThread {
                                successButtonBinding.root.parent?.let { parent ->
                                    if (parent is ViewGroup) parent.removeView(
                                        successButtonBinding.root
                                    )
                                }

                                val successMaterialDialog = MaterialAlertDialogBuilder(this)
                                    .setTitle(R.string.acceptedEnumerateVersion)
                                    .setIcon(R.drawable.check_circle)
                                    .setView(successButtonBinding.root)
                                    .setCancelable(false)
                                    .setMessage(
                                        "${getString(R.string.downloadLink)}$link\n\n${
                                            getString(
                                                R.string.fileSize
                                            )
                                        }$appSize MB"
                                    ).show()


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
                                            Intent.EXTRA_TEXT, when (mode) {
                                                MODE_OFFICIAL -> "Android QQ $versionBig ${
                                                    getString(
                                                        R.string.stableVersion
                                                    )
                                                }（${getString(R.string.fileSize)}$appSize MB）\n\n${
                                                    getString(
                                                        R.string.downloadLink
                                                    )
                                                }$link"

                                                MODE_WECHAT -> "Android 微信 $versionBig（$versionTrue）（${
                                                    getString(
                                                        R.string.fileSize
                                                    )
                                                }$appSize MB）\n\n${getString(R.string.downloadLink)}$link"

                                                else -> "Android QQ $versionBig.$vSmall ${
                                                    getString(
                                                        R.string.previewVersion
                                                    )
                                                }（${getString(R.string.fileSize)}$appSize MB）\n\n${
                                                    getString(
                                                        R.string.downloadLink
                                                    )
                                                }$link\n\n鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                            }
                                        )
                                    }
                                    startActivity(
                                        Intent.createChooser(
                                            shareIntent, getString(R.string.shareTo)
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
                                                MODE_TEST, MODE_UNOFFICIAL -> setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_QQ_${versionBig}.${vSmall}_64.apk"
                                                )

                                                MODE_OFFICIAL -> setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_QQ_${versionBig}_64.apk"
                                                )

                                                MODE_WECHAT -> setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_微信_${versionBig}.${versionTrue}.apk"
                                                )
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

                        } else {
                            when {
                                mode == MODE_TEST && (!guessTestExtend || sIndex == (stList.size)) -> { // 测试版情况下，未打开扩展猜版或扩展猜版到最后一步时执行小版本号的递增
                                    vSmall += if (!guessNot5) 5 else 1
                                    sIndex = 0
                                }

                                mode == MODE_UNOFFICIAL -> vSmall += if (!guessNot5) 5 else 1
                                mode == MODE_WECHAT -> {
                                    val version16code = v16codeStr.toInt(16) + 1
                                    v16codeStr = version16code.toString(16)
                                }
                            }
                        }
                    }

                    STATUS_PAUSE -> sleep(500)

                    STATUS_END -> {
                        if (mode != MODE_OFFICIAL) showToast(getString(R.string.enumHasBeenStopped))
                        sIndex = 0
                        progressDialog.dismiss()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
                showToast(getString(R.string.enumHasBeenStopped))
                progressDialog.dismiss()
            }
        }

        dialogLoadingBinding.btnCancel.setOnClickListener {
            status = STATUS_END
            progressDialog.dismiss()
        }

        thread.start()
    }

    /**
     * @param btn `MaterialButton` 实例，此函数将控制传入按钮的加载态
     * @param shiplyVersion QQ 版本号
     * @param shiplyUin QQ 号
     * @param shiplyAppid QQ 版本 ID，如 `537230561`
     * @param shiplyOsVersion Android 版本（整数表示）
     * @param shiplyModel 设备型号
     * @param shiplySdkVersion Shiply SDK 版本
     * @param shiplyLanguage 语言
     **/
    private fun tencentShiplyStart(
        btn: MaterialButton,
        shiplyVersion: String,
        shiplyUin: String,
        shiplyAppid: String = SHIPLY_DEFAULT_APPID,
        shiplyOsVersion: String = SDK_INT.toString(),
        shiplyModel: String = Build.MODEL.toString(),
        shiplySdkVersion: String = SHIPLY_DEFAULT_SDK_VERSION,
        shiplyLanguage: String = Locale.getDefault().language.toString()
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            class MissingCipherTextException(message: String) : Exception(message)
            try {
                // 参考：https://github.com/callng/GQUL
                val shiplyKey = ShiplyUtil.generateAESKey()
                val shiplyData = ShiplyUtil.generateJsonString(
                    shiplyVersion,
                    shiplyUin,
                    shiplyAppid,
                    shiplyOsVersion,
                    shiplyModel,
                    shiplySdkVersion,
                    shiplyLanguage
                )
                val shiplyEncode = ShiplyUtil.aesEncrypt(shiplyData, shiplyKey)
                val shiplyRsaPublicKey =
                    ShiplyUtil.base64ToRsaPublicKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/rT6ULqXC32dgz4t/Vv4WS9pTks5Z2fPmbTHIXEVeiOEnjOpPBHOi1AUz+Ykqjk11ZyjidUwDyIaC/VtaC5Z7Bt/W+CFluDer7LiiDa6j77if5dbcvWUrJbgvhKqaEhWnMDXT1pAG2KxL/pNFAYguSLpOh9pK97G8umUMkkwWkwIDAQAB")
                if (shiplyRsaPublicKey == null) runOnUiThread { showToast("生成 RSA 公钥失败") } // 应该不会失败吧
                else {
                    val shiplyEncode2 = ShiplyUtil.rsaEncrypt(
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
                    val shiplyResult = ShiplyUtil.postJsonWithOkHttp(
                        "https://rdelivery.qq.com/v3/config/batchpull", shiplyPost
                    )
                    val shiplyText = ShiplyUtil.getCipherText(shiplyResult)
                    if (!shiplyText.isNullOrEmpty()) {
                        val shiplyDecode = ShiplyUtil.aesDecrypt(
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
                                MaterialAlertDialogBuilder(this@MainActivity)
                                    .setView(
                                        dialogShiplyBackBinding.root
                                    ).setTitle(R.string.contentReturnedByShiplyPlatform)
                                    .setIcon(R.drawable.flask_line)
                                    .show().apply {
                                        shiplyUrlRecyclerView.layoutManager =
                                            LinearLayoutManager(this@MainActivity)
                                        when {
                                            shiplyApkUrl != null -> {
                                                shiplyUrlBackTitle.visibility = View.VISIBLE
                                                shiplyUrlRecyclerView.visibility = View.VISIBLE
                                                shiplyUrlRecyclerView.adapter =
                                                    ShiplyUrlListAdapter(shiplyApkUrl)
                                            }

                                            else -> {
                                                shiplyUrlBackTitle.visibility = View.GONE
                                                shiplyUrlRecyclerView.visibility = View.GONE
                                            }
                                        }
                                        shiplyBackText.text =
                                            shiplyDecodeStringJson.toPrettyFormat()
                                    }
                            }
                        }
                    } else throw MissingCipherTextException(getString(R.string.missingCipherWarning))
                }
            } catch (e: MissingCipherTextException) {
                e.printStackTrace()
                dialogError(e, true)
            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            } finally {
                runOnUiThread {
                    btn.apply {
                        style(com.google.android.material.R.style.Widget_Material3_Button)
                        icon = null
                        isEnabled = true
                    }
                }
            }
        }
    }


    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var context: Context

        fun setContext(newContext: Context) {
            this.context = newContext
        }

        const val STATUS_ONGOING = 0
        const val STATUS_PAUSE = 1
        const val STATUS_END = 2

        val MODE_TEST: String by lazy { context.getString(R.string.previewVersion) }
        val MODE_OFFICIAL: String by lazy { context.getString(R.string.stableVersion) }
        val MODE_UNOFFICIAL: String by lazy { context.getString(R.string.spaceEnumerateVersion) }
        val MODE_WECHAT: String by lazy { context.getString(R.string.weixinEnumerateVersion) }
    }
}
