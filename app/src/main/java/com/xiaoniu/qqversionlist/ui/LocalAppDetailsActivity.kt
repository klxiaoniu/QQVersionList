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

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import coil3.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.ActivityLocalAppDetailsBinding
import com.xiaoniu.qqversionlist.databinding.DialogLocalQqTimInfoBinding
import com.xiaoniu.qqversionlist.ui.MainActivity.Companion.JUDGE_UA_TARGET
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.FileSystems

class LocalAppDetailsActivity : AppCompatActivity() {
    lateinit var viewModel: LocalAppDetailsActivityViewModel
    lateinit var binding: ActivityLocalAppDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false

        setContentView(R.layout.activity_local_app_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (DataStoreUtil.getIntKV("userAgreement", 0) < JUDGE_UA_TARGET) {
            showToast(R.string.haveNotAgreeUA)
            finish()
        }
        binding = ActivityLocalAppDetailsBinding.inflate(layoutInflater)
        val viewRoot = binding.root
        setContentView(viewRoot)
        viewModel = ViewModelProvider(this)[LocalAppDetailsActivityViewModel::class.java]
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                finish()
            }
            progressLine.apply {
                showAnimationBehavior = LinearProgressIndicator.SHOW_NONE
                hideAnimationBehavior = LinearProgressIndicator.HIDE_ESCAPE
            }
            viewModel.apply {
                appIconImage.observe(this@LocalAppDetailsActivity) { appIconImage ->
                    if (appIconImage != null) {
                        localIcon.isVisible = true
                        localIcon.load(appIconImage)
                    } else localIcon.isVisible = false
                }
                isLoading.observe(this@LocalAppDetailsActivity) { isLoading ->
                    if (isLoading) {
                        progressLine.show()
                        detailInfo.setOnClickListener(null)
                    } else {
                        progressLine.hide()
                        detailInfo.setOnClickListener {
                            val localInfoAllText = (if (targetSDK.value != 0) "Target SDK${
                                getString(R.string.colon)
                            }${targetSDK.value}" else "") + (if (minSDK.value != 0) "\nMin SDK${
                                getString(R.string.colon)
                            }${minSDK.value}" else "") + (if (compileSDK.value != 0) "\nCompile SDK${
                                getString(R.string.colon)
                            }${
                                compileSDK.value
                            }" else "") + "\nVersion Name${getString(R.string.colon)}${
                                versionName.value
                            }" + (if (rdmUUID.value != "") "\nRdm UUID${
                                getString(R.string.colon)
                            }${
                                rdmUUID.value
                            }" else "") + (if (versionCode.value != "") "\nVersion Code${
                                getString(R.string.colon)
                            }${
                                versionCode.value
                            }" else "") + (if (appSettingParams.value != "") "\nAppSetting_params${
                                getString(R.string.colon)
                            }${
                                appSettingParams.value
                            }" else "") + (if (appSettingParamsPad.value != "") "\nAppSetting_params_pad${
                                getString(R.string.colon)
                            }${
                                appSettingParamsPad.value
                            }" else "") + (if (qua.value != "") "\nQUA${
                                getString(R.string.colon)
                            }${qua.value}" else "")

                            val dialogLocalQqTimInfoBinding = DialogLocalQqTimInfoBinding.inflate(
                                LayoutInflater.from(this@LocalAppDetailsActivity)
                            )

                            MaterialAlertDialogBuilder(this@LocalAppDetailsActivity).setView(
                                dialogLocalQqTimInfoBinding.root
                            ).setTitle(R.string.localDetailsMsg).setIcon(R.drawable.phone_find_line)
                                .apply {
                                    dialogLocalQqTimInfoBinding.apply {
                                        val dialogLocalSdkDesc = localSDKText.value

                                        dialogLocalSdk.apply {
                                            setCellDescription(dialogLocalSdkDesc)
                                            this.setOnClickListener {
                                                context.copyText(
                                                    "Android SDK${
                                                        getString(R.string.colon)
                                                    }$dialogLocalSdkDesc"
                                                )
                                            }
                                        }
                                        dialogLocalVersionName.apply {
                                            setCellDescription(versionName.value)
                                            this.setOnClickListener {
                                                context.copyText(
                                                    "Version Name${
                                                        getString(R.string.colon)
                                                    }${versionName.value}"
                                                )
                                            }
                                        }
                                        dialogLocalRdmUuid.apply {
                                            if (rdmUUID.value != "") {
                                                setCellDescription(rdmUUID.value)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Rdm UUID${
                                                            getString(R.string.colon)
                                                        }${rdmUUID.value}"
                                                    )
                                                }
                                            } else dialogLocalRdmUuid.isVisible = false
                                        }
                                        dialogLocalVersionCode.apply {
                                            if (versionCode.value != "") {
                                                setCellDescription(versionCode.value)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Version Code${
                                                            getString(R.string.colon)
                                                        }${versionCode.value}"
                                                    )
                                                }
                                            } else dialogLocalVersionCode.isVisible = false
                                        }
                                        dialogLocalAppsettingParams.apply {
                                            if (appSettingParams.value != "") {
                                                setCellDescription(appSettingParams.value)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params${
                                                            getString(R.string.colon)
                                                        }${appSettingParams.value}"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParams.isVisible = false
                                        }
                                        dialogLocalAppsettingParamsPad.apply {
                                            if (appSettingParamsPad.value != "") {
                                                setCellDescription(appSettingParamsPad.value)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params_pad${
                                                            getString(R.string.colon)
                                                        }${appSettingParamsPad.value}"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParamsPad.isVisible = false
                                        }
                                        dialogLocalQua.apply {
                                            if (qua.value != "") {
                                                setCellDescription(qua.value)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "QUA${
                                                            getString(
                                                                R.string.colon
                                                            )
                                                        }${qua.value}"
                                                    )
                                                }
                                            } else dialogLocalQua.isVisible = false
                                        }
                                        dialogLocalCopyAll.setOnClickListener {
                                            context.copyText(localInfoAllText)
                                        }
                                    }
                                }.show()
                        }
                    }
                }
                channelText.observe(this@LocalAppDetailsActivity) { text ->
                    if (text == "") binding.localChannelCard.isVisible = false else {
                        localChannelCard.isVisible = true
                        localChannelText.text = text
                    }
                }
                isErr.observe(this@LocalAppDetailsActivity) { isErr ->
                    detailInfo.isVisible = !isErr
                    stackInfo.isVisible = !isErr
                }
                observeString(appName, binding.localName)
                observeString(localVersion, binding.localVersion)
                observeString(localSDKText, binding.localSdk)
                observeStringWithVisible(isTIM, timBasedVer, binding.localTimBase)
                observeBoolean(hasQQNT, cellQqnt)
                observeString(hasQQNTDesc, qqntDesc)
                observeBoolean(hasUELibrary, cellUeLibrary)
                observeString(hasUELibraryDesc, ueLibraryDesc)
                observeBoolean(hasBugly, cellBugly)
                observeString(hasBuglyDesc, buglyDesc)
                observeBoolean(hasShiply, cellShiply)
                observeString(hasShiplyDesc, shiplyDesc)
                observeBoolean(hasKuikly, cellKuikly)
                observeString(hasKuiklyDesc, kuiklyDesc)
                observeBoolean(hasHippy, cellHippy)
                observeString(hasHippyDesc, hippyDesc)
                observeBoolean(hasRightly, cellRightly)
                observeString(hasRightlyDesc, rightlyDesc)
                observeBoolean(hasTencentBeacon, cellTencentBeacon)
                observeString(hasTencentBeaconDesc, tencentBeaconDesc)
                observeBoolean(hasJetpackCompose, cellJetpackCompose)
                observeString(hasJetpackComposeDesc, jetpackComposeDesc)
                observeBoolean(hasComposeMultiplatform, cellComposeMultiplatform)
                observeString(hasComposeMultiplatformDesc, composeMultiplatformDesc)
                observeBoolean(hasFlutter, cellFlutter)
                observeString(hasFlutterDesc, flutterDesc)
            }

            setupClickListener(
                cellQqnt,
                R.string.localDetailsQQNT,
                R.string.localDetailsQQNTDesc,
                null,
                R.drawable.qqnt_logo_unofficial_fix
            )
            setupClickListener(
                cellBugly,
                R.string.localDetailsBugly,
                R.string.localDetailsBuglyDesc,
                "https://bugly.tds.qq.com/v2/index/tds-main",
                R.drawable.bugly_official
            )
            setupClickListener(
                cellUeLibrary,
                R.string.localDetailsUELibrary,
                R.string.localDetailsUELibraryDesc,
                "https://dev.epicgames.com/documentation/unreal-engine/building-unreal-engine-as-a-library",
                R.drawable.ue_icon_2023_black
            )
            setupClickListener(
                cellHippy,
                R.string.localDetailsHippy,
                R.string.localDetailsHippyDesc,
                "https://openhippy.com/",
                R.drawable.hippy_official
            )
            setupClickListener(
                cellKuikly,
                R.string.localDetailsKuikly,
                R.string.localDetailsKuiklyDesc,
                null,
                R.drawable.kuikly_official
            )
            setupClickListener(
                cellShiply,
                R.string.localDetailsShiply,
                R.string.localDetailsShiplyDesc,
                "https://shiply.tds.qq.com/",
                R.drawable.shiply_official
            )
            setupClickListener(
                cellRightly,
                R.string.localDetailsRightly,
                R.string.localDetailsRightlyDesc,
                "https://rightly.tds.qq.com/",
                R.drawable.rightly_official
            )
            setupClickListener(
                cellTencentBeacon,
                R.string.localDetailsTencentBeacon,
                R.string.localDetailsTencentBeaconDesc,
                "https://beacon.qq.com/",
                R.drawable.beacon_official
            )
            setupClickListener(
                cellComposeMultiplatform,
                R.string.localDetailsComposeMultiplatform,
                R.string.localDetailsComposeMultiplatformDesc,
                "https://www.jetbrains.com/compose-multiplatform/",
                R.drawable.compose
            )
            setupClickListener(
                cellJetpackCompose,
                R.string.localDetailsJetpackCompose,
                R.string.localDetailsJetpackComposeDesc,
                "https://developer.android.com/compose",
                R.drawable.compose
            )
            setupClickListener(
                cellFlutter,
                R.string.localDetailsFlutter,
                R.string.localDetailsFlutterDesc,
                "https://flutter.dev/",
                R.drawable.icon_flutter_dk_blue
            )
        }

        try {
            val intent = intent
            when {
                intent.hasExtra("localAppType") -> {
                    val localAppType = intent.getStringExtra("localAppType")
                    if (localAppType == "QQ" || localAppType == "TIM") viewModel.getInfo(
                        this, localAppType
                    )
                    if (localAppType == "QQ") binding.topAppBar.title =
                        getString(R.string.localQQVersionDetails)
                    else if (localAppType == "TIM") binding.topAppBar.title =
                        getString(R.string.localTIMVersionDetails)
                }

                intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_VIEW -> {
                    binding.topAppBar.title = getString(R.string.apkAnalyze)
                    val action = intent.action
                    val uri = when (action) {
                        Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(
                            intent, Intent.EXTRA_STREAM, Uri::class.java
                        )

                        Intent.ACTION_VIEW -> intent.data
                        else -> null
                    }
                    if (uri != null) {
                        val path = uri.path
                        if (path != null) {
                            val normalizedPath =
                                if (SDK_INT >= Build.VERSION_CODES.O) FileSystems.getDefault()
                                    .getPath(path).normalize()
                                    .toString() else File(path).canonicalPath
                            if (!normalizedPath.startsWith("/data")) {
                                val cacheDir = File(cacheDir, "apkAnalysis")
                                if (cacheDir.exists()) cacheDir.deleteRecursively()
                                cacheDir.mkdirs()
                                val requiredSpace = 500 * 1024 * 1024L // 500MB
                                val freeSpace = cacheDir.freeSpace
                                val fileSize = getFileSizeFromUri(uri)
                                if (freeSpace >= requiredSpace + if (fileSize != -1L) fileSize else 0) {
                                    val destinationFile = File(cacheDir, "temp_apk.apk")
                                    contentResolver.openInputStream(uri)?.use { inputStream ->
                                        FileUtils.copyInputStreamToFile(
                                            inputStream, destinationFile
                                        )
                                    }
                                    viewModel.getInfo(this, "inter", destinationFile.path)
                                } else viewModel.setAppName(getString(R.string.notEnoughSpaceOnTheDevice))
                            } else viewModel.setAppName(getString(R.string.unknownErr))
                        } else viewModel.setAppName(getString(R.string.unknownErr))
                    } else viewModel.setAppName(getString(R.string.unknownErr))
                }

                else -> {
                    viewModel.setAppName(getString(R.string.unknownErr))
                    binding.topAppBar.apply {
                        title = getString(R.string.app_name)
                        subtitle = null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dialogError(e)
        }
    }

    private fun observeBoolean(liveData: LiveData<Boolean>, view: View) {
        liveData.observe(this@LocalAppDetailsActivity) { isVisible ->
            view.isVisible = isVisible
        }
    }

    private fun observeString(liveData: LiveData<String>, textView: TextView) {
        liveData.observe(this@LocalAppDetailsActivity) { text ->
            textView.text = text
        }
    }

    private fun observeStringWithVisible(
        liveDataBoolean: LiveData<Boolean>, liveDataString: LiveData<String>, textView: TextView
    ) {
        liveDataBoolean.observe(this@LocalAppDetailsActivity) { boolean ->
            liveDataString.observe(this@LocalAppDetailsActivity) { text ->
                textView.isVisible = text != "" && boolean
                textView.text = text
            }
        }
    }

    private fun setupClickListener(
        view: View, titleRes: Int, messageRes: Int, url: String?, iconRes: Int? = null
    ) {
        view.setOnClickListener {
            MaterialAlertDialogBuilder(this@LocalAppDetailsActivity).setTitle(titleRes)
                .setMessage(messageRes).setPositiveButton(R.string.done) { _, _ -> }.apply {
                    if (iconRes != null) setIcon(iconRes)
                    if (url != null) setNeutralButton(R.string.details) { _, _ ->
                        val uri = Uri.parse(url)
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(this@LocalAppDetailsActivity, uri)
                    }
                }.show()
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                cursor.moveToFirst()
                if (sizeIndex != -1) cursor.getLong(sizeIndex) else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }
}