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

package com.xiaoniu.qqversionlist.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.text.method.LinkMovementMethodCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.paris.extensions.style
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec
import com.google.android.material.progressindicator.IndeterminateDrawable
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Strictness
import com.xiaoniu.qqversionlist.BuildConfig
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.SHIPLY_DEFAULT_APPID
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.SHIPLY_DEFAULT_SDK_VERSION
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.databinding.ApplicationsConfigBackButtonBinding
import com.xiaoniu.qqversionlist.databinding.DialogAboutBinding
import com.xiaoniu.qqversionlist.databinding.DialogExpBackBinding
import com.xiaoniu.qqversionlist.databinding.DialogExperimentalFeaturesBinding
import com.xiaoniu.qqversionlist.databinding.DialogFirebaseFirstInfoBinding
import com.xiaoniu.qqversionlist.databinding.DialogFormatDefineBinding
import com.xiaoniu.qqversionlist.databinding.DialogGuessBinding
import com.xiaoniu.qqversionlist.databinding.DialogHashBinding
import com.xiaoniu.qqversionlist.databinding.DialogLoadingBinding
import com.xiaoniu.qqversionlist.databinding.DialogPersonalizationBinding
import com.xiaoniu.qqversionlist.databinding.DialogSettingBinding
import com.xiaoniu.qqversionlist.databinding.DialogShiplyBinding
import com.xiaoniu.qqversionlist.databinding.DialogTencentAppStoreBinding
import com.xiaoniu.qqversionlist.databinding.ExpLinkNextButtonBinding
import com.xiaoniu.qqversionlist.databinding.SuccessButtonBinding
import com.xiaoniu.qqversionlist.databinding.UpdateQvtButtonBinding
import com.xiaoniu.qqversionlist.databinding.UserAgreementBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.Extensions.dp
import com.xiaoniu.qqversionlist.util.FileUtil.downloadFile
import com.xiaoniu.qqversionlist.util.FileUtil.getFileSize
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.getQverbowSM3
import com.xiaoniu.qqversionlist.util.InfoUtil.qverbowAboutText
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.ShiplyUtil
import com.xiaoniu.qqversionlist.util.StringUtil.getAllAPKUrl
import com.xiaoniu.qqversionlist.util.StringUtil.resolveWeixinAlphaConfig
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import com.xiaoniu.qqversionlist.util.StringUtil.trimSubstringAtEnd
import com.xiaoniu.qqversionlist.util.StringUtil.trimSubstringAtStart
import com.xiaoniu.qqversionlist.util.VersionUtil
import com.xiaoniu.qqversionlist.util.VersionUtil.resolveLocalQQ
import com.xiaoniu.qqversionlist.util.VersionUtil.resolveLocalTIM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.maven.artifact.versioning.ComparableVersion
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

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced =
            false

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) ViewCompat.setOnApplyWindowInsetsListener(
            viewRoot
        ) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.bottomAppBar.post {
                binding.bottomAppBar.updatePadding(0, 0, 0, insets.bottom)
            }
            binding.btnGuess.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom / 2
            }
            windowInsets
        }

        qqVersionAdapter = QQVersionAdapter()
        timVersionAdapter = TIMVersionAdapter()
        localQQAdapter = LocalQQAdapter()
        localTIMAdapter = LocalTIMAdapter()
        binding.rvPager.adapter = VersionListPagerAdapter(this)
        rvPagerAdapter = binding.rvPager.adapter as VersionListPagerAdapter
        qqVersionListFragment = QQVersionListFragment()
        timVersionListFragment = TIMVersionListFragment()
        if (!BuildConfig.VERSION_NAME.endsWith("Release")) binding.materialToolbar.setNavigationIcon(
            R.drawable.git_commit_line
        )

        initButtons()
    }

    /**
     * 用户协议
     * @param agreed 用户先前是否同意过用户协议
     * @param UATarget 用户协议版本
     **/
    private fun showUADialog(agreed: Boolean, UATarget: Int) {

        // 屏幕高度获取
        val screenHeight = Resources.getSystem().displayMetrics.heightPixels

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

        FastScrollerBuilder(userAgreementBinding.UAScroll).useMd2Style()
            .setPadding(0, 0, 0, 0).build()

        dialogUA.show()
    }


    @SuppressLint("SetTextI18n")
    private fun initButtons() {
        // 删除 version Shared Preferences
        DataStoreUtil.deleteKVAsync("version")

        /**
         * 这里的伴生类的 `JUDGE_UA_TARGET` 的值代表着用户协议修订版本，
         * 后续更新协议版本后也需要在下面伴生类中把 `JUDGE_UA_TARGET` + 1，以此类推
         **/
        val judgeUATarget = JUDGE_UA_TARGET
        if (DataStoreUtil.getIntKV("userAgreement", 0) < judgeUATarget) showUADialog(
            false, judgeUATarget
        ) else {
            getData()
            if (BuildConfig.VERSION_NAME.endsWith("Release") && DataStoreUtil.getBooleanKV(
                    "autoCheckUpdates",
                    false
                )
            ) checkQverbowUpdates(
                BuildConfig.VERSION_NAME.trimSubstringAtEnd("-Release"), false
            )
        }

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
                                if (BuildConfig.VERSION_NAME.endsWith("Release")) btnAboutUpdate.apply {
                                    isEnabled = true
                                    setText(R.string.checkUpdateViaGitHubAPI)
                                } else btnAboutUpdate.apply {
                                    isEnabled = false
                                    setText(R.string.ciVersionNoSupportUpdates)
                                }

                                aboutText.movementMethod =
                                    LinkMovementMethodCompat.getInstance()

                                aboutText.text = this@MainActivity.qverbowAboutText()
                            }

                        btnAboutWithdrawConsentUA.setOnClickListener {
                            showUADialog(true, judgeUATarget)
                            aboutDialog.dismiss()
                        }

                        btnAboutSharedList.setOnClickListener {
                            val url =
                                "https://raw.githubusercontent.com/klxiaoniu/QQVersionList/refs/heads/master/DataListShared.md"
                            val intent = CustomTabsIntent.Builder().build()
                            intent.launchUrl(this@MainActivity, Uri.parse(url))
                        }

                        btnAboutUpdate.setOnClickListener {
                            val spec = CircularProgressIndicatorSpec(
                                this@MainActivity, null, 0,
                                com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
                            )
                            val progressIndicatorDrawable =
                                IndeterminateDrawable.createCircularDrawable(
                                    this@MainActivity, spec
                                )

                            btnAboutUpdate.apply {
                                isEnabled = false
                                style(com.google.android.material.R.style.Widget_Material3_Button_TonalButton_Icon)
                                icon = progressIndicatorDrawable
                            }

                            checkQverbowUpdates(
                                BuildConfig.VERSION_NAME.trimSubstringAtEnd("-Release"),
                                true, btnAboutUpdate
                            )
                        }

                        btnAboutOk.setOnClickListener {
                            aboutDialog.dismiss()
                        }

                        btnAboutOpenSource.setOnClickListener {
                            startActivity(
                                Intent(
                                    this@MainActivity, OssLicensesMenuActivity::class.java
                                ).apply {
                                    OssLicensesMenuActivity.setActivityTitle(getString(R.string.openSourceLicenseTitle))
                                })
                        }

                        btnAboutHash.setOnClickListener {
                            val dialogHashBinding = DialogHashBinding.inflate(layoutInflater)
                            val qverbowSM3 = getQverbowSM3()

                            val hashDialog = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(R.string.qverbowHash)
                                .setIcon(R.drawable.shield_keyhole_line)
                                .setView(dialogHashBinding.root)
                                .show().apply {
                                    dialogHashBinding.aboutHashText.text =
                                        "SM3${getString(R.string.colon)}${qverbowSM3}"
                                    dialogHashBinding.btnAboutGithubHashVerifiy.isVisible =
                                        BuildConfig.VERSION_NAME.endsWith("Release")
                                }

                            dialogHashBinding.apply {
                                btnAboutHashOk.setOnClickListener {
                                    hashDialog.dismiss()
                                }

                                btnAboutHashCopy.setOnClickListener {
                                    copyText(qverbowSM3)
                                }

                                btnAboutGithubHashVerifiy.setOnClickListener {
                                    val spec = CircularProgressIndicatorSpec(
                                        this@MainActivity, null, 0,
                                        com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
                                    )
                                    val progressIndicatorDrawable =
                                        IndeterminateDrawable.createCircularDrawable(
                                            this@MainActivity, spec
                                        )

                                    btnAboutGithubHashVerifiy.apply {
                                        isEnabled = false
                                        style(com.google.android.material.R.style.Widget_Material3_Button_TonalButton_Icon)
                                        icon = progressIndicatorDrawable
                                    }

                                    checkQverbowHash(
                                        BuildConfig.VERSION_NAME.trimSubstringAtEnd("-Release"),
                                        getQverbowSM3(),
                                        btnAboutGithubHashVerifiy
                                    )
                                }
                            }
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
                            DataStoreUtil.getBooleanKV("guessTestExtend", false) // 扩展测试版扫版格式
                        downloadOnSystemManager.isChecked =
                            DataStoreUtil.getBooleanKV("downloadOnSystemManager", false)
                        switchAutoCheckUpdates.isChecked =
                            DataStoreUtil.getBooleanKV("autoCheckUpdates", false)
                        switchPushNotifViaFcm.isVisible =
                            Firebase.messaging.isAutoInitEnabled && GoogleApiAvailability.getInstance()
                                .isGooglePlayServicesAvailable(this@MainActivity) == ConnectionResult.SUCCESS
                        switchPushNotifViaFcm.isChecked =
                            DataStoreUtil.getBooleanKV("rainbowFCMSubscribed", false)
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
                                switchKuiklyTag.isChecked =
                                    DataStoreUtil.getBooleanKV("kuiklyTag", true)
                                switchUnrealEngineTag.isChecked =
                                    DataStoreUtil.getBooleanKV("unrealEngineTag", false)
                                switchProgressSize.isChecked =
                                    DataStoreUtil.getBooleanKV("progressSize", false)
                                switchProgressSizeText.isChecked =
                                    DataStoreUtil.getBooleanKV("progressSizeText", false)
                                switchVersionTcloud.isChecked =
                                    DataStoreUtil.getBooleanKV("versionTCloud", true)
                                switchOldLoading.isChecked =
                                    DataStoreUtil.getBooleanKV("showOldLoading", false)

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

                                switchOldLoading.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKVAsync("showOldLoading", isChecked)
                                }

                                // 下五个设置不能异步持久化存储，否则视图更新读不到更新值
                                switchKuiklyTag.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("kuiklyTag", isChecked)
                                    qqVersionAdapter.updateItemProperty("isShowKuiklyTag")
                                    timVersionAdapter.updateItemProperty("isShowKuiklyTag")
                                }
                                switchUnrealEngineTag.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("unrealEngineTag", isChecked)
                                    qqVersionAdapter.updateItemProperty("isShowUnrealEngineTag")
                                }
                                switchProgressSize.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("progressSize", isChecked)
                                    qqVersionAdapter.updateItemProperty("isShowProgressSize")
                                }
                                switchProgressSizeText.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKV("progressSizeText", isChecked)
                                    qqVersionAdapter.updateItemProperty("isShowProgressSizeText")
                                }
                                switchVersionTcloud.setOnCheckedChangeListener { _, isChecked ->
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
                        switchAutoCheckUpdates.setOnCheckedChangeListener { _, isChecked ->
                            DataStoreUtil.putBooleanKVAsync("autoCheckUpdates", isChecked)
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
                                DialogFormatDefineBinding.inflate(layoutInflater)

                            dialogSuffixDefine.root.parent?.let { parent ->
                                if (parent is ViewGroup) {
                                    parent.removeView(dialogSuffixDefine.root)
                                }
                            }

                            val dialogSuffix = MaterialAlertDialogBuilder(this@MainActivity)
                                .setTitle(R.string.enumerateVersionsFormatSetting)
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
                                    formatDefineCheckboxQq8958.isChecked =
                                        getBooleanKV("useQQ8958TestFormat", false)
                                    formatDefineCheckboxQq900814600.isChecked =
                                        getBooleanKV("useQQ900814600TestFormat", false)
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
                                        ), mapOf(
                                            "key" to "useQQ8958TestFormat",
                                            "value" to formatDefineCheckboxQq8958.isChecked,
                                            "type" to "Boolean"
                                        ), mapOf(
                                            "key" to "useQQ900814600TestFormat",
                                            "value" to formatDefineCheckboxQq900814600.isChecked,
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
                        switchPushNotifViaFcm.setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked != DataStoreUtil.getBooleanKV(
                                    "rainbowFCMSubscribed",
                                    false
                                )
                            ) {
                                if (isChecked) {
                                    if (!NotificationManagerCompat.from(this@MainActivity)
                                            .areNotificationsEnabled()
                                    ) askNotificationPermission()
                                    if (!NotificationManagerCompat.from(this@MainActivity)
                                            .areNotificationsEnabled()
                                    ) switchPushNotifViaFcm.isChecked = false
                                    else if (!checkNotificationChannelEnabled(
                                            getString(R.string.rainbow_notification_channel_id)
                                        )
                                    ) {
                                        switchPushNotifViaFcm.isChecked = false
                                        dialogError(
                                            Exception(getString(R.string.cannotEnableFirebaseCloudMessaging)),
                                            true, true
                                        )
                                    } else {
                                        switchPushNotifViaFcm.isEnabled = false
                                        Firebase.analytics.setAnalyticsCollectionEnabled(true)
                                        subscribeWithTimeout(10000L, switchPushNotifViaFcm)
                                    }
                                } else {
                                    switchPushNotifViaFcm.isEnabled = false
                                    Firebase.analytics.setAnalyticsCollectionEnabled(true)
                                    unsubscribeWithTimeout(10000L, switchPushNotifViaFcm)
                                }
                            }
                        }
                    }
                    true
                }

                R.id.btn_exp_features -> {
                    val dialogExperimentalFeaturesBinding =
                        DialogExperimentalFeaturesBinding.inflate(layoutInflater)

                    dialogExperimentalFeaturesBinding.dialogFirebase.setCellDescription(
                        if (GoogleApiAvailability.getInstance()
                                .isGooglePlayServicesAvailable(this@MainActivity) == ConnectionResult.SUCCESS && Firebase.messaging.isAutoInitEnabled
                        ) getString(R.string.initializedFirebaseServiceItem) else null
                    )

                    val dialogExperimentalFeatures = MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.experimentalFeatures)
                        .setIcon(R.drawable.flask_line)
                        .setView(dialogExperimentalFeaturesBinding.root)
                        .show()

                    dialogExperimentalFeaturesBinding.apply {
                        progressIndicator.apply {
                            hide()
                            showAnimationBehavior = LinearProgressIndicator.SHOW_NONE
                            hideAnimationBehavior = LinearProgressIndicator.HIDE_ESCAPE
                        }

                        btnExpOk.setOnClickListener {
                            dialogExperimentalFeatures.dismiss()
                        }

                        dialogGetWeixinAlphaNewest.setCellOnClickListener {
                            progressIndicator.show()
                            CoroutineScope(Dispatchers.IO).launch {
                                class CustomException(message: String) :
                                    Exception(message)
                                try {
                                    val okHttpClient = OkHttpClient()
                                    val request =
                                        Request.Builder()
                                            .url("https://dldir1.qq.com/weixin/android/weixin_android_alpha_config.json")
                                            .build()
                                    val response = okHttpClient.newCall(request).execute()
                                    if (!response.isSuccessful) throw CustomException(getString(R.string.getWeixinAlphaConfig404))
                                    val responseData = response.body?.string()
                                    if (responseData == null) throw CustomException("Response data is null.")
                                    val start = (responseData.indexOf("cb(")) + 3
                                    val end = responseData.indexOf(")")
                                    val jsonString = responseData.substring(start, end)
                                    val map = resolveWeixinAlphaConfig(jsonString)
                                    val appSize = getFileSize(map["url"].toString())
                                    runOnUiThread {
                                        val applicationsConfigBackButtonBinding =
                                            ApplicationsConfigBackButtonBinding.inflate(
                                                layoutInflater
                                            )
                                        val weixinAlphaConfigBackDialog =
                                            MaterialAlertDialogBuilder(this@MainActivity).setTitle(
                                                if (appSize != null) R.string.successInGetting else R.string.suspectedPackageWithdrawal
                                            ).setIcon(R.drawable.flask_line).setMessage(
                                                "${getString(R.string.version)}${map["versionName"].toString()}\n${
                                                    getString(
                                                        R.string.downloadLink
                                                    )
                                                }${map["url"].toString()}" + (if (appSize != null) "\n\n${
                                                    getString(
                                                        R.string.fileSize
                                                    )
                                                }$appSize MB" else ("\n\n" + getString(R.string.getWeixinAlphaConfigLink404)))
                                            ).setView(applicationsConfigBackButtonBinding.root)
                                                .show()

                                        applicationsConfigBackButtonBinding.apply {
                                            applicationsConfigBackBtnCopy.setOnClickListener {
                                                weixinAlphaConfigBackDialog.dismiss()
                                                copyText(map["url"].toString())
                                            }

                                            applicationsConfigBackBtnDownload.setOnClickListener {
                                                weixinAlphaConfigBackDialog.dismiss()
                                                downloadFile(
                                                    this@MainActivity, map["url"].toString()
                                                )
                                            }

                                            applicationsConfigBackBtnJsonDetails.setOnClickListener {
                                                showExpBackDialog(
                                                    Gson().toJson(map),
                                                    getString(R.string.jsonDetails)
                                                )
                                            }

                                            applicationsConfigBackBtnShare.setOnClickListener {
                                                weixinAlphaConfigBackDialog.dismiss()
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Android 微信测试版 ${map["versionName"].toString()}" + (if (appSize != null) "（${
                                                            getString(
                                                                R.string.fileSize
                                                            )
                                                        }$appSize MB）" else "") + "\n\n${
                                                            getString(
                                                                R.string.downloadLink
                                                            )
                                                        }${map["url"].toString()}\n\n鉴于微信测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。"
                                                    )
                                                }
                                                startActivity(
                                                    Intent.createChooser(
                                                        shareIntent, getString(R.string.shareTo)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } catch (e: CustomException) {
                                    dialogError(e, true)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    dialogError(e)
                                } finally {
                                    runOnUiThread { progressIndicator.hide() }
                                }
                            }
                        }

                        dialogGetWetypeLatest.setCellOnClickListener {
                            progressIndicator.show()
                            CoroutineScope(Dispatchers.IO).launch {
                                class CustomException(message: String) :
                                    Exception(message)
                                try {
                                    val okHttpClient =
                                        OkHttpClient.Builder().followRedirects(false).build()
                                    val request =
                                        Request.Builder()
                                            .url("https://z.weixin.qq.com/android/download?channel=latest")
                                            .build()
                                    val response = okHttpClient.newCall(request).execute()
                                    if (!response.isSuccessful && !response.isRedirect) throw CustomException(
                                        getString(R.string.getWeTypeLatestChannel404)
                                    )
                                    val url = response.header("Location")
                                    if (url == null) throw CustomException("Response data is null.")
                                    val appSize = getFileSize(url.toString())
                                    runOnUiThread {
                                        val expLinkNextButtonBinding =
                                            ExpLinkNextButtonBinding.inflate(
                                                layoutInflater
                                            )
                                        val weTypeLatestChannelBackDialog =
                                            MaterialAlertDialogBuilder(this@MainActivity).setTitle(
                                                if (appSize != null) R.string.successInGetting else R.string.suspectedPackageWithdrawal
                                            ).setIcon(R.drawable.flask_line).setMessage(
                                                "${
                                                    getString(
                                                        R.string.downloadLink
                                                    )
                                                }$url" + (if (appSize != null) "\n\n${
                                                    getString(
                                                        R.string.fileSize
                                                    )
                                                }$appSize MB" else "")
                                            ).setView(expLinkNextButtonBinding.root)
                                                .show()

                                        expLinkNextButtonBinding.apply {
                                            expNextBtnCopy.setOnClickListener {
                                                weTypeLatestChannelBackDialog.dismiss()
                                                copyText(url.toString())
                                            }

                                            expNextBtnDownload.setOnClickListener {
                                                weTypeLatestChannelBackDialog.dismiss()
                                                downloadFile(
                                                    this@MainActivity, url.toString()
                                                )
                                            }

                                            expNextBtnShare.setOnClickListener {
                                                weTypeLatestChannelBackDialog.dismiss()
                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, url)
                                                }
                                                startActivity(
                                                    Intent.createChooser(
                                                        shareIntent, getString(R.string.shareTo)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                } catch (e: CustomException) {
                                    dialogError(e, true)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    dialogError(e)
                                } finally {
                                    runOnUiThread { progressIndicator.hide() }
                                }
                            }
                        }

                        dialogTencentAppStore.setOnClickListener {
                            val dialogTencentAppStoreBinding =
                                DialogTencentAppStoreBinding.inflate(layoutInflater)

                            val tencentAppStoreDialog =
                                MaterialAlertDialogBuilder(this@MainActivity)
                                    .setTitle(R.string.getUpdateFromTencentAppStore)
                                    .setIcon(R.drawable.flask_line)
                                    .setView(dialogTencentAppStoreBinding.root)
                                    .show()

                            dialogTencentAppStoreBinding.apply {
                                tencentAppStoreBack.setOnClickListener {
                                    tencentAppStoreDialog.dismiss()
                                }

                                getQq.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.mobileqq", getQq)
                                }

                                getTim.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.tim", getTim)
                                }

                                getWeixin.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.mm", getWeixin)
                                }

                                getWecom.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.wework", getWecom)
                                }

                                getWetype.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.wetype", getWetype)
                                }

                                getQidian.setOnClickListener {
                                    tencentAppStoreStart("com.tencent.qidian", getQidian)
                                }
                            }
                        }

                        dialogShiply.setOnClickListener {
                            val dialogShiplyBinding = DialogShiplyBinding.inflate(layoutInflater)

                            val shiplyDialog = MaterialAlertDialogBuilder(this@MainActivity)
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

                                    val shiplyTargetSelect = getStringKV("shiplyTargetSelect", "")
                                    if (shiplyTargetSelect == getString(R.string.shiplyTargetAppQQ) || shiplyTargetSelect == getString(
                                            R.string.shiplyTargetAppTIM
                                        )
                                    ) shiplyTarget.setText(
                                        shiplyTargetSelect, false
                                    ) else shiplyTarget.setText(
                                        getString(R.string.shiplyTargetAppQQ), false
                                    )
                                }

                                switchShiplyAdvancedConfigurations.setOnCheckedChangeListener { _, isChecked ->
                                    DataStoreUtil.putBooleanKVAsync(
                                        "shiplyAdvancedConfigurations",
                                        isChecked
                                    )
                                }

                                shiplyAdvancedConfigurationsClick.setOnClickListener {
                                    ShiplyAdvancedConfigFragment().apply {
                                        isCancelable = false
                                        show(
                                            supportFragmentManager,
                                            ShiplyAdvancedConfigFragment.TAG
                                        )
                                    }
                                }

                                btnShiplyCancel.setOnClickListener {
                                    shiplyDialog.dismiss()
                                }

                                shiplyTarget.addTextChangedListener(object : TextWatcher {
                                    override fun afterTextChanged(p0: Editable?) {
                                        val shiplyTargetSelect = shiplyTarget.text.toString()
                                        DataStoreUtil.putStringKVAsync(
                                            "shiplyTargetSelect", shiplyTargetSelect
                                        )
                                    }

                                    override fun beforeTextChanged(
                                        p0: CharSequence?, p1: Int, p2: Int, p3: Int
                                    ) {
                                    }

                                    override fun onTextChanged(
                                        p0: CharSequence?, p1: Int, p2: Int, p3: Int
                                    ) {
                                    }
                                })

                                btnShiplyStart.setOnClickListener {
                                    shiplyUin.clearFocus()
                                    shiplyVersion.clearFocus()

                                    val imm =
                                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                    imm.hideSoftInputFromWindow(shiplyVersion.windowToken, 0)

                                    class MissingParameterException(message: String) :
                                        Exception(message)

                                    try {
                                        val spec = CircularProgressIndicatorSpec(
                                            this@MainActivity, null, 0,
                                            com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
                                        )
                                        val progressIndicatorDrawable =
                                            IndeterminateDrawable.createCircularDrawable(
                                                this@MainActivity, spec
                                            )

                                        btnShiplyStart.apply {
                                            isEnabled = false
                                            style(com.google.android.material.R.style.Widget_Material3_Button_Icon)
                                            icon = progressIndicatorDrawable
                                        }

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
                                                    "shiplyVersion",
                                                    shiplyVersion.editText?.text.toString()
                                                )
                                                putStringKVAsync(
                                                    "shiplyUin", shiplyUin.editText?.text.toString()
                                                )
                                            }
                                            tencentShiplyStart(
                                                btnShiplyStart,
                                                shiplyVersion.editText?.text.toString(),
                                                shiplyUin.editText?.text.toString(),
                                                shiplyTarget.text.toString()
                                            )
                                        } else DataStoreUtil.apply {
                                            putStringKVAsync(
                                                "shiplyVersion",
                                                shiplyVersion.editText?.text.toString()
                                            )
                                            putStringKVAsync(
                                                "shiplyUin", shiplyUin.editText?.text.toString()
                                            )
                                            tencentShiplyStart(btnShiplyStart,
                                                shiplyVersion.editText?.text.toString(),
                                                shiplyUin.editText?.text.toString(),
                                                shiplyTarget.text.toString(),
                                                getStringKV(
                                                    "shiplyAppid", ""
                                                ).ifEmpty { SHIPLY_DEFAULT_APPID },
                                                getStringKV(
                                                    "shiplyOsVersion", ""
                                                ).ifEmpty { Build.VERSION.SDK_INT.toString() },
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
                        }

                        dialogFirebase.setOnClickListener {
                            // 必须检测 Google Play 服务是否可用，因为 Firebase 服务依赖于 Google Play 服务
                            if (GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(this@MainActivity) == ConnectionResult.SUCCESS
                            ) {
                                if (!Firebase.messaging.isAutoInitEnabled) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val channelTitle =
                                            getString(R.string.rainbow_notification_channel_title)
                                        val channelDescription =
                                            getString(R.string.rainbow_notification_channel_description)
                                        val channelId =
                                            getString(R.string.rainbow_notification_channel_id)
                                        val channelImportance =
                                            NotificationManager.IMPORTANCE_DEFAULT
                                        val notificationChannel = NotificationChannel(
                                            channelId, channelTitle, channelImportance
                                        )
                                        notificationChannel.description = channelDescription
                                        val notificationManager =
                                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                                        notificationManager.createNotificationChannel(
                                            notificationChannel
                                        )
                                    }

                                    val dialogFirebaseFirstInfoBinding =
                                        DialogFirebaseFirstInfoBinding.inflate(layoutInflater)

                                    val dialogFirebaseInfo =
                                        MaterialAlertDialogBuilder(this@MainActivity)
                                            .setTitle(R.string.initFirebaseService)
                                            .setIcon(R.drawable.flask_line)
                                            .setView(dialogFirebaseFirstInfoBinding.root)
                                            .show()

                                    dialogFirebaseFirstInfoBinding.firebaseInfoCancel.setOnClickListener {
                                        dialogFirebaseInfo.dismiss()
                                    }

                                    dialogFirebaseFirstInfoBinding.firebaseInfoNext.setOnClickListener {
                                        Firebase.messaging.isAutoInitEnabled = true
                                        Firebase.analytics.setAnalyticsCollectionEnabled(true)
                                        dialogFirebaseInfo.dismiss()
                                    }

                                } else {
                                    showToast(getString(R.string.initializedFirebaseService))
                                    Firebase.analytics.setAnalyticsCollectionEnabled(true)
                                }
                            } else {
                                dialogError(
                                    Exception(getString(R.string.cannotFindGooglePlayServices)),
                                    true
                                )
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

    // 下面三个函数是用于响应扫版对话框 Spinner 所选项的界面变化
    private fun modeTestView(dialogGuessBinding: DialogGuessBinding, mode: String) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = true
            etVersionSmall.isVisible = true
            guessDialogWarning.isVisible = true
            etVersion16code.isVisible = false
            etVersionTrue.isVisible = false
            if (mode == MODE_TIM) tvWarning.setText(R.string.enumTIMWarning)
            else tvWarning.setText(R.string.enumQQPreviewWarning)
            etVersionBig.helperText = getString(R.string.enumQQMajorVersionHelpText)
        }
    }

    private fun modeOfficialView(dialogGuessBinding: DialogGuessBinding) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            etVersionSmall.isVisible = true
            guessDialogWarning.isVisible = false
            etVersion16code.isVisible = false
            etVersionTrue.isVisible = false
            etVersionBig.helperText = getString(R.string.enumQQMajorVersionHelpText)
        }
    }

    private fun modeWeChatView(dialogGuessBinding: DialogGuessBinding) {
        dialogGuessBinding.apply {
            etVersionSmall.isEnabled = false
            guessDialogWarning.isVisible = true
            etVersionSmall.isVisible = false
            etVersionTrue.isVisible = true
            etVersion16code.isVisible = true
            tvWarning.setText(R.string.enumWeixinWarning)
            etVersionBig.helperText = getString(R.string.enumWeixinMajorVersionHelpText)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showGuessVersionDialog() {
        val dialogGuessBinding = DialogGuessBinding.inflate(layoutInflater)
        val verBig = if (DataStoreUtil.getStringKV(
                "versionSelect", MODE_OFFICIAL
            ) == MODE_TIM
        ) DataStoreUtil.getStringKV(
            "TIMVersionBig", ""
        ) else DataStoreUtil.getStringKV("versionBig", "")
        dialogGuessBinding.etVersionBig.editText?.setText(verBig)
        when (val memVersion = DataStoreUtil.getStringKV("versionSelect", MODE_OFFICIAL)) {
            MODE_TEST, MODE_OFFICIAL, MODE_WECHAT, MODE_TIM -> dialogGuessBinding.spinnerVersion.setText(
                memVersion, false
            )

            MODE_UNOFFICIAL -> if (DataStoreUtil.getBooleanKV(
                    "useQQ900814600TestFormat", false
                )
            ) dialogGuessBinding.spinnerVersion.setText(memVersion, false) else {
                dialogGuessBinding.spinnerVersion.setText(MODE_OFFICIAL, false)
                DataStoreUtil.putStringKVAsync("versionSelect", MODE_OFFICIAL)
            }

            else -> {
                dialogGuessBinding.spinnerVersion.setText(MODE_OFFICIAL, false)
                DataStoreUtil.putStringKVAsync("versionSelect", MODE_OFFICIAL)
            }
        }

        when (val memVersion = dialogGuessBinding.spinnerVersion.text.toString()) {
            MODE_TEST, MODE_UNOFFICIAL, MODE_TIM -> modeTestView(
                dialogGuessBinding, memVersion
            )

            MODE_OFFICIAL -> modeOfficialView(dialogGuessBinding)
            MODE_WECHAT -> modeWeChatView(dialogGuessBinding)
        }


        dialogGuessBinding.spinnerVersion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val judgeVerSelect = dialogGuessBinding.spinnerVersion.text.toString()
                DataStoreUtil.putStringKVAsync("versionSelect", judgeVerSelect)
                when (judgeVerSelect) {
                    MODE_TEST, MODE_UNOFFICIAL, MODE_TIM -> modeTestView(
                        dialogGuessBinding, judgeVerSelect
                    )

                    MODE_OFFICIAL -> modeOfficialView(dialogGuessBinding)
                    MODE_WECHAT -> modeWeChatView(dialogGuessBinding)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        if (DataStoreUtil.getBooleanKV(
                "useQQ900814600TestFormat", false
            )
        ) (dialogGuessBinding.spinnerLayout.editText as MaterialAutoCompleteTextView).setSimpleItems(
            R.array.version_plus
        ) else (dialogGuessBinding.spinnerLayout.editText as MaterialAutoCompleteTextView).setSimpleItems(
            R.array.version_default
        )

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
                    MODE_TEST, MODE_UNOFFICIAL, MODE_TIM -> if (dialogGuessBinding.etVersionSmall.editText?.text.isNullOrEmpty()) throw MissingVersionException(
                        getString(R.string.missingMajorVersionWarning)
                    ) else {
                        versionSmall =
                            dialogGuessBinding.etVersionSmall.editText?.text.toString().toInt()
                        if (mode != MODE_TIM && versionSmall % 5 != 0 && !DataStoreUtil.getBooleanKV(
                                "guessNot5", false
                            )
                        ) throw InvalidMultipleException(getString(R.string.QQPreviewMinorNot5Warning))
                        if (versionSmall != 0) when (mode) {
                            MODE_TIM -> DataStoreUtil.putIntKVAsync(
                                "versionTIMSmall", versionSmall
                            )

                            else -> DataStoreUtil.putIntKVAsync(
                                "versionSmall", versionSmall
                            )
                        }
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
        val memVersionTIMSmall = DataStoreUtil.getIntKV("versionTIMSmall", -1)
        if (memVersionSmall != -1 && memVersionTIMSmall != -1) when {
            DataStoreUtil.getStringKV(
                "versionSelect", MODE_OFFICIAL
            ) == MODE_TIM -> dialogGuessBinding.etVersionSmall.editText?.setText(
                memVersionTIMSmall.toString()
            )

            else -> dialogGuessBinding.etVersionSmall.editText?.setText(
                memVersionSmall.toString()
            )
        } else if (memVersionTIMSmall == -1 && memVersionSmall != -1) dialogGuessBinding.etVersionSmall.editText?.setText(
            memVersionSmall.toString()
        ) else if (memVersionSmall == -1 && memVersionTIMSmall != -1) dialogGuessBinding.etVersionSmall.editText?.setText(
            memVersionTIMSmall.toString()
        )
        val memVersion16code = DataStoreUtil.getStringKV("version16code", "-1")
        if (memVersion16code != "-1") dialogGuessBinding.etVersion16code.editText?.setText(
            memVersion16code
        )
        val memVersionTrue = DataStoreUtil.getIntKV("versionTrue", -1)
        if (memVersionTrue != -1) dialogGuessBinding.etVersionTrue.editText?.setText(memVersionTrue.toString())
    }


    @SuppressLint("SetTextI18n", "PrivateResource")
    private fun getData(menu: MenuItem? = null) {
        binding.progressLine.show()
        if (menu != null) menu.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                resolveLocalQQ()
            } catch (_: Exception) {
                val localQQEmptyList = listOf(
                    mapOf("key" to "QQVersionInstall", "value" to "", "type" to "String"),
                    mapOf("key" to "QQVersionCodeInstall", "value" to "", "type" to "String"),
                    mapOf("key" to "QQAppSettingParamsInstall", "value" to "", "type" to "String"),
                    mapOf(
                        "key" to "QQAppSettingParamsPadInstall",
                        "value" to "", "type" to "String"
                    ),
                    mapOf("key" to "QQRdmUUIDInstall", "value" to "", "type" to "String"),
                    mapOf("key" to "QQQua", "value" to "", "type" to "String"),
                )
                DataStoreUtil.batchPutKVAsync(localQQEmptyList)
            } finally {
                try {
                    resolveLocalTIM()
                } catch (_: Exception) {
                    val localTIMEmptyList = listOf(
                        mapOf("key" to "TIMVersionInstall", "value" to "", "type" to "String"),
                        mapOf("key" to "TIMVersionCodeInstall", "value" to "", "type" to "String"),
                        mapOf(
                            "key" to "TIMAppSettingParamsInstall",
                            "value" to "", "type" to "String"
                        ), mapOf(
                            "key" to "TIMAppSettingParamsPadInstall",
                            "value" to "", "type" to "String"
                        ), mapOf(
                            "key" to "TIMRdmUUIDInstall",
                            "value" to "", "type" to "String"
                        ), mapOf(
                            "key" to "TIMQua",
                            "value" to "", "type" to "String"
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
                            VersionUtil.resolveQQRainbow(this@MainActivity, responseData)
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
                        // https://im.qq.com/rainbow/TIMDownload/ 已弃用
                        val okHttpClient = OkHttpClient()
                        val preRequest =
                            Request.Builder().url("https://tim.qq.com/support.html").build()
                        val preResponse = okHttpClient.newCall(preRequest).execute()
                        val htmlData = preResponse.body?.string()
                        val regex =
                            """jQuery\.ajax\(\{\s*url:\s*'([^']+)'\s*\}\)\.done\(function \(versionData\)""".toRegex()
                        val matchResult = regex.find(htmlData!!)
                        val match = matchResult?.groupValues?.getOrNull(1)
                        if (match != null && (match.startsWith("https://") || match.startsWith("http://"))) {
                            val request = Request.Builder()
                                .url(match.replace("http://", "https://"))
                                .build()
                            val response = okHttpClient.newCall(request).execute()
                            val responseData = response.body?.string()
                            if (responseData != null) {
                                VersionUtil.resolveTIMRainbow(this@MainActivity, responseData)
                                withContext(Dispatchers.Main) {
                                    timVersionAdapter.submitList(timVersion)
                                    if (!DataStoreUtil.getBooleanKV(
                                            "closeSwipeLeftForTIM", false
                                        )
                                    ) {
                                        class TipTIMSnackbarActionListener : View.OnClickListener {
                                            override fun onClick(v: View?) {
                                                DataStoreUtil.putBooleanKV(
                                                    "closeSwipeLeftForTIM", true
                                                )
                                            }
                                        }

                                        val isDarkTheme: Boolean =
                                            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                                                Configuration.UI_MODE_NIGHT_YES -> true
                                                else -> false
                                            }

                                        Snackbar.make(
                                            binding.root, R.string.swipeLeftForTIMVersions,
                                            Snackbar.LENGTH_INDEFINITE
                                        ).setAction(R.string.ok, TipTIMSnackbarActionListener())
                                            .setAnchorView(binding.btnGuess)
                                            .apply {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) if (isDarkTheme) setBackgroundTint(
                                                    getColor(com.google.android.material.R.color.m3_sys_color_dynamic_dark_secondary)
                                                ) else setBackgroundTint(getColor(com.google.android.material.R.color.m3_sys_color_dynamic_light_secondary))
                                            }.show()
                                    }
                                }
                            }
                        } else throw Exception("Can not get TIM version data.")
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
            .apply {
                dialogLoadingBinding.apply {
                    val oldLoadingIsVisible = DataStoreUtil.getBooleanKV("showOldLoading", false)
                    progressIndicator.isVisible = oldLoadingIsVisible
                    loadingIndicator.isVisible = !oldLoadingIsVisible
                }
            }.create()

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
            val useQQ8958TestFormat = DataStoreUtil.getBooleanKV("useQQ8958TestFormat", false)
            val suf64hb =
                if (DataStoreUtil.getBooleanKV(
                        "suffix64HB", true
                    )
                ) listOf("_64_HB") else emptyList()
            val sufHb64 =
                if (DataStoreUtil.getBooleanKV(
                        "suffixHB64", true
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
                        "suffix64HD", true
                    )
                ) listOf("_64_HD") else emptyList()
            val sufHd64 =
                if (DataStoreUtil.getBooleanKV(
                        "suffixHD64", true
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
                suf64hb, suf64hb1, suf64hb2, suf64hb3, suf64hd, suf64hd1, suf64hd2, suf64hd3,
                suf64hd1hb, sufHb64, sufHb164, sufHb264, sufHb364, sufHd64, sufHd164, sufHd264,
                sufHd364, sufHd1hb64, sufTest
            ).flatten()

            // ["_64", "_64_HB", "_64_HB1", "_64_HB2", "_64_HB3", "_64_HD", "_64_HD1", "_64_HD2", "_64_HD3", "_64_HD1HB", "_HB_64", "_HB1_64", "_HB2_64", "_HB3_64", "_HD_64", "_HD1_64", "_HD2_64", "_HD3_64", "_HD1HB_64", "_test"]

            val stList = if (defineSufList != listOf("")) stListPre + defineSufList else stListPre
            val wxSoListPre = listOf("", "_1")
            val wxSoList =
                if (defineSufList != listOf("")) wxSoListPre + defineSufList else wxSoListPre
            try {
                var sIndex = 0
                while (true) when (status) {
                    STATUS_ONGOING -> {
                        when (mode) {
                            MODE_TEST -> if (link == "" || !guessTestExtend) {
                                link =
                                    if (useQQ8958TestFormat) "https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_$versionBig.${vSmall}${stList[sIndex]}.apk" else "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                if (guessTestExtend) sIndex += 1
                            } else {
                                link =
                                    if (useQQ8958TestFormat) "https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_$versionBig.${vSmall}${stList[sIndex]}.apk" else "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                sIndex += 1
                            }

                            MODE_TIM -> if (link == "" || !guessTestExtend) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/TIM_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                if (guessTestExtend) sIndex += 1
                            } else {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/TIM_$versionBig.${vSmall}${stList[sIndex]}.apk"
                                sIndex += 1
                            }

                            MODE_UNOFFICIAL -> link =
                                "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%20$versionBig.${vSmall}%2064.apk"

                            MODE_OFFICIAL -> {
                                val soListPre = listOf(
                                    "_64", "_64_HB", "_64_HB1", "_64_HB2", "_64_HB3", "_HB_64",
                                    "_HB1_64", "_HB2_64", "_HB3_64", "_64_BBPJ", "_BBPJ_64"
                                )
                                val soList =
                                    if (defineSufList != listOf("")) soListPre + defineSufList else soListPre
                                if (sIndex == (soList.size)) {
                                    status = STATUS_END
                                    showToast("未扫描到包")
                                    continue
                                } else {
                                    link =
                                        if (useQQ8958TestFormat) "https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_${versionBig}${soList[sIndex]}.apk" else "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}${soList[sIndex]}.apk"
                                    sIndex += 1
                                }

                            }

                            MODE_WECHAT -> {
                                // https://dldir1.qq.com/weixin/android/weixin8049android2600_0x2800318a_arm64.apk
                                // https://dldir1.qq.com/weixin/android/weixin8054android2740_0x28003630_arm64_1.apk
                                link =
                                    "https://dldir1.qq.com/weixin/android/weixin${versionBig}android${versionTrue}_0x${v16codeStr}_arm64${wxSoList[sIndex]}.apk"
                                sIndex += 1
                            }
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
                                    // 测试版情况下，未打开扩展扫版或扩展扫版到最后一步时执行小版本号的递增
                                    when {
                                        mode == MODE_TEST && (!guessTestExtend || sIndex >= (stList.size)) -> {
                                            vSmall += if (!guessNot5) 5 else 1
                                            sIndex = 0
                                        }

                                        mode == MODE_TIM && (!guessTestExtend || sIndex >= (stList.size)) -> {
                                            vSmall += 1
                                            sIndex = 0
                                        }

                                        mode == MODE_UNOFFICIAL -> vSmall += if (!guessNot5) 5 else 1
                                        mode == MODE_WECHAT -> {
                                            if (sIndex >= wxSoList.size && guessTestExtend) {
                                                val version16code = v16codeStr.toInt(16) + 1
                                                v16codeStr = version16code.toString(16)
                                                sIndex = 0
                                            } else if (sIndex >= wxSoListPre.size && !guessTestExtend) {
                                                val version16code = v16codeStr.toInt(16) + 1
                                                v16codeStr = version16code.toString(16)
                                                sIndex = 0
                                            }
                                        }
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

                                                MODE_TIM -> "Android TIM $versionBig.$vSmall（${
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

                                                MODE_TIM -> setDestinationInExternalPublicDir(
                                                    Environment.DIRECTORY_DOWNLOADS,
                                                    "Android_TIM_${versionBig}.${vSmall}_64.apk"
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
                                mode == MODE_TEST && (!guessTestExtend || sIndex == (stList.size)) -> { // 测试版情况下，未打开扩展扫版或扩展扫版到最后一步时执行小版本号的递增
                                    vSmall += if (!guessNot5) 5 else 1
                                    sIndex = 0
                                }

                                mode == MODE_TIM && (!guessTestExtend || sIndex == (stList.size)) -> {
                                    vSmall += 1
                                    sIndex = 0
                                }

                                mode == MODE_UNOFFICIAL -> vSmall += if (!guessNot5) 5 else 1
                                mode == MODE_WECHAT -> {
                                    if (sIndex >= wxSoList.size && guessTestExtend) {
                                        val version16code = v16codeStr.toInt(16) + 1
                                        v16codeStr = version16code.toString(16)
                                        sIndex = 0
                                    } else if (sIndex >= wxSoListPre.size && !guessTestExtend) {
                                        val version16code = v16codeStr.toInt(16) + 1
                                        v16codeStr = version16code.toString(16)
                                        sIndex = 0
                                    }
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

    private fun showExpBackDialog(sourceDataJson: String, dialogTitle: String) {
        val dialogExpBackBinding =
            DialogExpBackBinding.inflate(layoutInflater)

        dialogExpBackBinding.root.parent?.let { parent ->
            if (parent is ViewGroup) parent.removeView(dialogExpBackBinding.root)
        }

        val shiplyApkUrl = sourceDataJson.toPrettyFormat().getAllAPKUrl()

        dialogExpBackBinding.apply {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setView(
                    dialogExpBackBinding.root
                ).setTitle(dialogTitle)
                .setIcon(R.drawable.flask_line)
                .show().apply {
                    expUrlRecyclerView.layoutManager =
                        LinearLayoutManager(this@MainActivity)
                    when {
                        shiplyApkUrl != null -> {
                            expUrlBackTitle.isVisible = true
                            expUrlRecyclerView.isVisible = true
                            expUrlRecyclerView.adapter =
                                ExpUrlListAdapter(shiplyApkUrl)
                        }

                        else -> {
                            expUrlBackTitle.isVisible = false
                            expUrlRecyclerView.isVisible = false
                        }
                    }
                    expBackText.text =
                        sourceDataJson.toPrettyFormat()
                    FastScrollerBuilder(dialogExpBackBinding.expBackTextScroll).useMd2Style()
                        .setPadding(0, 0, 0, 32.dp).build()
                }
        }
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
        targetApp: String = "QQ",
        shiplyAppid: String = SHIPLY_DEFAULT_APPID,
        shiplyOsVersion: String = Build.VERSION.SDK_INT.toString(),
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
                    shiplyLanguage,
                    targetApp
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
                            showExpBackDialog(
                                shiplyDecodeStringJson,
                                getString(R.string.contentReturnedByShiplyPlatform)
                            )
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

    private fun tencentAppStoreStart(
        getType: String,
        btn: MaterialButton
    ) {
        val spec = CircularProgressIndicatorSpec(
            this@MainActivity, null, 0,
            com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall
        )
        val progressIndicatorDrawable =
            IndeterminateDrawable.createCircularDrawable(
                this@MainActivity, spec
            )
        btn.apply {
            isEnabled = false
            style(com.google.android.material.R.style.Widget_Material3_Button_TonalButton_Icon)
            icon = progressIndicatorDrawable
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient.Builder().build()
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val getTypePost = mapOf("packagename" to getType)
                val body =
                    GsonBuilder().setStrictness(Strictness.LENIENT).create().toJson(getTypePost)
                val request = Request.Builder().url("https://upage.html5.qq.com/wechat-apkinfo")
                    .post(body.toRequestBody(mediaType!!))
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseData = response.body?.string()
                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val tencentAppStoreResult = gson.fromJson(responseData, JsonObject::class.java)
                    val tencentAppStoreResultJson = gson.toJson(tencentAppStoreResult)
                    val appAllData = tencentAppStoreResult.getAsJsonObject("app_detail_records")
                        .getAsJsonObject(getType).getAsJsonObject("apk_all_data")
                    val appName = appAllData.getAsJsonPrimitive("name").asString
                    val appVersionName = appAllData.getAsJsonPrimitive("version_name").asString
                    val appUrl = appAllData.getAsJsonPrimitive("url").asString
                    val appSize =
                        "%.2f".format(appAllData.getAsJsonPrimitive("size_byte").asDouble.div(1024 * 1024))

                    runOnUiThread {
                        val applicationsConfigBackButtonBinding =
                            ApplicationsConfigBackButtonBinding.inflate(
                                layoutInflater
                            )
                        val tencentAppStoreConfigBackDialog =
                            MaterialAlertDialogBuilder(this@MainActivity).setTitle(R.string.contentReturnedByTencentAppStore)
                                .setIcon(R.drawable.flask_line).setMessage(
                                    "$appName $appVersionName\n${getString(R.string.downloadLink)}$appUrl" + "\n\n${
                                        getString(
                                            R.string.fileSize
                                        )
                                    }$appSize MB"
                                ).setView(applicationsConfigBackButtonBinding.root).show()

                        applicationsConfigBackButtonBinding.apply {
                            applicationsConfigBackBtnCopy.setOnClickListener {
                                tencentAppStoreConfigBackDialog.dismiss()
                                copyText(appUrl)
                            }

                            applicationsConfigBackBtnDownload.setOnClickListener {
                                tencentAppStoreConfigBackDialog.dismiss()
                                downloadFile(
                                    this@MainActivity, appUrl
                                )
                            }

                            applicationsConfigBackBtnJsonDetails.setOnClickListener {
                                showExpBackDialog(
                                    tencentAppStoreResultJson,
                                    getString(R.string.jsonDetails)
                                )
                            }

                            applicationsConfigBackBtnShare.setOnClickListener {
                                tencentAppStoreConfigBackDialog.dismiss()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Android $appName $appVersionName" + "（${getString(R.string.fileSize)}$appSize MB）" + "\n\n${
                                            getString(
                                                R.string.downloadLink
                                            )
                                        }$appUrl\n\n来自腾讯应用宝"
                                    )
                                }
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent, getString(R.string.shareTo)
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    dialogError(e)
                }
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

    private fun checkQverbowUpdates(
        selfVersion: String,
        isManual: Boolean,
        btn: MaterialButton? = null
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request =
                    Request.Builder()
                        .url("https://api.github.com/repos/klxiaoniu/QQVersionList/releases/latest")
                        .build()
                val response = okHttpClient.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    val gson = Gson()
                    val jsonData = gson.fromJson(responseData, JsonObject::class.java)
                    val latestQverbowVersion =
                        jsonData.get("tag_name").asString.trimSubstringAtStart("v")
                    if (ComparableVersion(latestQverbowVersion) > ComparableVersion(selfVersion)) {
                        val latestQverbowAssets = jsonData.get("assets").asJsonArray
                        var latestQverbowDownloadUrl: String? = null
                        var latestQverbowFileName: String? = null
                        var latestQverbowFileSize: String? = null
                        for (asset in latestQverbowAssets) {
                            val assetObject = asset.asJsonObject
                            val contentType = assetObject.get("content_type").asString
                            val browserDownloadUrl =
                                assetObject.get("browser_download_url").asString
                            if (contentType == "application/vnd.android.package-archive") {
                                latestQverbowDownloadUrl = browserDownloadUrl
                                latestQverbowFileName = assetObject.get("name").asString
                                latestQverbowFileSize = "%.2f".format(
                                    assetObject.get("size").asLong.toDouble().div(1024 * 1024)
                                )
                                break
                            }
                        }
                        if (latestQverbowDownloadUrl != null) withContext(Dispatchers.Main) {
                            val updateQvtButtonBinding =
                                UpdateQvtButtonBinding.inflate(layoutInflater)

                            val updateQvtMaterialDialog =
                                MaterialAlertDialogBuilder(this@MainActivity)
                                    .setTitle(R.string.updateQVTAvailable)
                                    .setIcon(R.drawable.check_circle)
                                    .setView(updateQvtButtonBinding.root)
                                    .setMessage(
                                        "${getString(R.string.version)}$latestQverbowVersion\n${
                                            getString(
                                                R.string.downloadLink
                                            )
                                        }$latestQverbowDownloadUrl\n${
                                            getString(
                                                R.string.fileSize
                                            )
                                        }$latestQverbowFileSize MB"
                                    )
                                    .show()

                            updateQvtButtonBinding.updateQvtCopy.setOnClickListener {
                                copyText(latestQverbowDownloadUrl)
                                updateQvtMaterialDialog.dismiss()
                            }

                            updateQvtButtonBinding.updateQvtDownload.setOnClickListener {
                                updateQvtMaterialDialog.dismiss()
                                downloadFile(
                                    this@MainActivity,
                                    latestQverbowDownloadUrl,
                                    latestQverbowFileName
                                )
                            }
                        } else showToast(getString(R.string.noAssetsDetected))
                    } else showToast(getString(R.string.noUpdatesDetected))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isManual) dialogError(
                        RuntimeException(getString(R.string.cannotGetGitHub), e), true
                    ) else showToast(getString(R.string.cannotGetGitHub))
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btn?.apply {
                        style(com.google.android.material.R.style.Widget_Material3_Button_TonalButton)
                        icon = null
                        isEnabled = true
                    }
                }
            }
        }
    }

    private fun checkQverbowHash(
        selfVersion: String,
        sm3: String,
        btn: MaterialButton? = null
    ) {
        class HashIsFalseException(message: String) : Exception(message)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request =
                    Request.Builder()
                        .url("https://api.github.com/repos/klxiaoniu/QQVersionList/releases/tags/v$selfVersion")
                        .build()
                val response = okHttpClient.newCall(request).execute()
                val responseData = response.body?.string()
                if (responseData != null) {
                    val gson = Gson()
                    val jsonData = gson.fromJson(responseData, JsonObject::class.java)
                    val githubBody =
                        jsonData.get("body").asString
                    withContext(Dispatchers.Main) {
                        if (githubBody.contains(sm3))
                            showToast(R.string.hashIsTrue) else throw HashIsFalseException(
                            getString(R.string.hashIsFalse)
                        )
                    }
                }
            } catch (e: HashIsFalseException) {
                withContext(Dispatchers.Main) {
                    dialogError(
                        RuntimeException(getString(R.string.hashIsFalse), e), true, false, true
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialogError(
                        RuntimeException(getString(R.string.cannotGetGitHub), e), true
                    )
                }
            } finally {
                withContext(Dispatchers.Main) {
                    btn?.apply {
                        style(com.google.android.material.R.style.Widget_Material3_Button_TonalButton)
                        icon = null
                        isEnabled = true
                    }
                }
            }
        }
    }

    // 检查特定通知渠道是否被用户关闭
    private fun checkNotificationChannelEnabled(channelId: String): Boolean {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            return channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            // 对于 API 级别 < 26 的设备，默认返回 true
            return true
        }
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            dialogError(
                Exception(getString(R.string.cannotEnableFirebaseCloudMessaging)), true, true
            )
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                dialogError(
                    Exception(getString(R.string.cannotEnableFirebaseCloudMessaging)), true, true
                )
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (checkNotificationChannelEnabled(getString(R.string.rainbow_notification_channel_id))) {
                // FCM SDK (and your app) can post notifications.
            } else {
                dialogError(
                    Exception(getString(R.string.cannotEnableFirebaseCloudMessaging)), true, true
                )
            }
        }
    }

    // Firebase 云消息传递订阅和退订 API 有问题，会在无法连接 Google 服务器时无限重试，还无法通过生命周期等线程进行管理和关闭甚至杀死相关进程
    // 下面两个方法实现不对，给 Firebase 提了 Issue，接下来等 Firebase 更改相关 API 或者进一步回复再改
    // 下面的是临时策略，请勿为了缩减 MainActivity 而将其单独分离出去，等 Google 解决后应该可以只用一条语句替代下面的整个函数
    private fun subscribeWithTimeout(
        timeoutMillis: Long, switchPushNotifViaFcm: MaterialSwitch
    ) {
        var status = false
        val job = lifecycleScope.launch {
            Firebase.messaging.subscribeToTopic("rainbowUpdates").addOnCanceledListener {
                status = true
                showToast(getString(R.string.subscribeFailed))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = false
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", false
                )
            }.addOnSuccessListener {
                status = true
                showToast(getString(R.string.subscribeSuccess))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = true
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", true
                )
            }.addOnFailureListener {
                status = true
                showToast(getString(R.string.subscribeFailed))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = false
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", false
                )
            }
        }
        lifecycleScope.launch {
            for (i in 1..100) if (status) break else delay(timeoutMillis / 100)
            if (!status) {
                job.cancel()
                showToast(getString(R.string.subscribeTimeout))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = false
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", false
                )
            }
        }
    }

    private fun unsubscribeWithTimeout(
        timeoutMillis: Long, switchPushNotifViaFcm: MaterialSwitch
    ) {
        var status = false
        val job = lifecycleScope.launch {
            Firebase.messaging.unsubscribeFromTopic("rainbowUpdates").addOnCanceledListener {
                status = true
                showToast(getString(R.string.unsubscribeFailed))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = true
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", true
                )
            }.addOnSuccessListener {
                status = true
                showToast(getString(R.string.unsubscribeSuccess))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = false
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", false
                )
            }.addOnFailureListener {
                status = true
                showToast(getString(R.string.unsubscribeFailed))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = true
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", true
                )
            }
        }
        lifecycleScope.launch {
            for (i in 1..100) if (status) break else delay(timeoutMillis / 100)
            if (!status) {
                job.cancel()
                showToast(getString(R.string.unsubscribeTimeout))
                switchPushNotifViaFcm.isEnabled = true
                switchPushNotifViaFcm.isChecked = true
                DataStoreUtil.putBooleanKV(
                    "rainbowFCMSubscribed", true
                )
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
        const val JUDGE_UA_TARGET = 5 // 2024.10.28 第五版

        val MODE_TEST: String by lazy { context.getString(R.string.previewVersion) }
        val MODE_OFFICIAL: String by lazy { context.getString(R.string.stableVersion) }
        val MODE_UNOFFICIAL: String by lazy { context.getString(R.string.spaceEnumerateVersion) }
        val MODE_WECHAT: String by lazy { context.getString(R.string.weixinEnumerateVersion) }
        val MODE_TIM: String by lazy { context.getString(R.string.timVersion) }
    }
}
