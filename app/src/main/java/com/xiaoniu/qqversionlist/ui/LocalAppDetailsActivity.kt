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

package com.xiaoniu.qqversionlist.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import coil3.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ZHIPU_TOKEN
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.LocalAppStackResult
import com.xiaoniu.qqversionlist.databinding.ActivityLocalAppDetailsBinding
import com.xiaoniu.qqversionlist.databinding.DialogChangesLlmInferenceBinding
import com.xiaoniu.qqversionlist.databinding.DialogLocalQqTimInfoBinding
import com.xiaoniu.qqversionlist.ui.LocalAppDetailsActivityViewModel.Companion.DEX_PRE_RULES
import com.xiaoniu.qqversionlist.ui.LocalAppDetailsActivityViewModel.Companion.RULES_ID_ORDER
import com.xiaoniu.qqversionlist.ui.LocalAppDetailsActivityViewModel.Companion.RULE_TYPE_OPEN_SOURCE_3RD_PARTY
import com.xiaoniu.qqversionlist.ui.LocalAppDetailsActivityViewModel.Companion.RULE_TYPE_OTEAM_TENCENT
import com.xiaoniu.qqversionlist.ui.LocalAppDetailsActivityViewModel.Companion.RULE_TYPE_PRIVATE_TENCENT
import com.xiaoniu.qqversionlist.ui.MainActivity.Companion.JUDGE_UA_TARGET
import com.xiaoniu.qqversionlist.ui.theme.QQVersionListTheme
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.openUrlWithChromeCustomTab
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.KeyStoreUtil
import com.xiaoniu.qqversionlist.util.StringUtil.pangu
import com.xiaoniu.qqversionlist.util.ZhipuSDKUtil.getZhipuWrite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File
import java.net.SocketTimeoutException
import java.nio.file.FileSystems
import java.util.Locale
import kotlin.collections.sortedWith

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
        val localInterChangesLlmGen = DataStoreUtil.getBooleanKV("localInterChangesLlmGen", false)
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
                isAIShowing.observe(this@LocalAppDetailsActivity) { isAIShowing ->
                    if (isAIShowing && localInterChangesLlmGen) {
                        floatingActionButtonLlm.isVisible = true
                        floatingActionButtonLlm.show()
                        floatingActionButtonLlm.setOnClickListener {
                            val dialogChangesLlmInferenceBinding =
                                DialogChangesLlmInferenceBinding.inflate(
                                    layoutInflater
                                )

                            val changeText =
                                activityDiff.value + "\n\n" + serviceDiff.value + "\n\n" + receiverDiff.value + "\n\n" + providerDiff.value + "\n\n" + permissionDiff.value
                            val versionChange =
                                "${appName.value} ${localVersionNameWithInter.value} → ${versionName.value}"

                            CoroutineScope(
                                Dispatchers.IO
                            ).launch {
                                try {
                                    val token = KeyStoreUtil.getStringKVwithKeyStore(ZHIPU_TOKEN)
                                    val tokenIsNullOrEmpty = token.isNullOrEmpty()
                                    if (!tokenIsNullOrEmpty) {
                                        runOnUiThread { viewModel.setChangesBackLLMWorking(true) }
                                        val llmResponse = getZhipuWrite(
                                            getString(
                                                R.string.llmInferenceLocalChangesPrompt,
                                                appName.value,
                                                localVersionNameWithInter.value,
                                                versionName.value,
                                                Locale.getDefault().toString()
                                            ),
                                            versionChange + "\n\n" + changeText,
                                            token
                                        )
                                        val gson = GsonBuilder().setPrettyPrinting().create()
                                        val responseObject = gson.fromJson(
                                            llmResponse, JsonObject::class.java
                                        )

                                        runOnUiThread {
                                            if (responseObject.getAsJsonPrimitive("code").asInt == 200) {
                                                val zhipuContent =
                                                    responseObject.getAsJsonObject("data").asJsonObject.getAsJsonArray(
                                                        "choices"
                                                    ).asJsonArray.first().asJsonObject.getAsJsonObject(
                                                        "message"
                                                    ).asJsonObject.getAsJsonPrimitive(
                                                        "content"
                                                    ).asString
                                                viewModel.setChangesBackLLMGenText(zhipuContent.pangu())
                                            } else {
                                                val zhipuContent =
                                                    responseObject.getAsJsonPrimitive("msg").asString + getString(
                                                        R.string.colon
                                                    ) + responseObject.getAsJsonObject("error").asJsonObject.getAsJsonPrimitive(
                                                        "message"
                                                    ).asString
                                                viewModel.setChangesBackLLMGenText(zhipuContent)
                                            }
                                            viewModel.setChangesBackLLMWorking(false)
                                        }
                                    } else runOnUiThread {
                                        viewModel.setChangesBackLLMWorking(false)
                                        viewModel.setChangesBackLLMGenText(getString(R.string.zhipuTokenIsNull))
                                    }
                                } catch (_: SocketTimeoutException) {
                                    runOnUiThread {
                                        viewModel.setChangesBackLLMWorking(false)
                                        viewModel.setChangesBackLLMGenText(getString(R.string.timeout))
                                    }
                                } catch (e: Exception) {
                                    runOnUiThread {
                                        e.printStackTrace()
                                        dialogError(e)
                                    }
                                }
                            }

                            val dialogChangesLlmInference =
                                MaterialAlertDialogBuilder(this@LocalAppDetailsActivity).setTitle(R.string.llmInferenceLocalChanges)
                                    .setView(dialogChangesLlmInferenceBinding.root)
                                    .setIcon(R.drawable.ai_generate_2).show()

                            dialogChangesLlmInferenceBinding.versionChangeInfo.text = versionChange

                            viewModel.isChangesBackLLMWorking.observe(this@LocalAppDetailsActivity,
                                Observer { isWorking ->
                                    if (DataStoreUtil.getBooleanKV("updateLogLlmGen", false)) {
                                        dialogChangesLlmInferenceBinding.llmGenCard.isVisible =
                                            !isWorking && viewModel.changesBackLLMGenText.value.toString()
                                                .isNotEmpty()
                                        dialogChangesLlmInferenceBinding.progressIndicator.isVisible =
                                            isWorking
                                        dialogChangesLlmInferenceBinding.btnCopy.isVisible =
                                            !isWorking && viewModel.changesBackLLMGenText.value.toString()
                                                .isNotEmpty()
                                        dialogChangesLlmInferenceBinding.btnCopy.setOnClickListener {
                                            copyText(
                                                viewModel.changesBackLLMGenText.value.toString() + "\n\n" + getString(
                                                    R.string.genByAITips
                                                )
                                            )
                                        }
                                    } else {
                                        dialogChangesLlmInferenceBinding.llmGenCard.isVisible =
                                            false
                                        dialogChangesLlmInferenceBinding.progressIndicator.isVisible =
                                            false
                                        dialogChangesLlmInferenceBinding.btnCopy.isVisible = false
                                        dialogChangesLlmInferenceBinding.btnCopy.setOnClickListener(
                                            null
                                        )
                                    }
                                })
                            viewModel.changesBackLLMGenText.observe(this@LocalAppDetailsActivity,
                                Observer { text ->
                                    dialogChangesLlmInferenceBinding.llmGenText.text = text
                                })

                            dialogChangesLlmInferenceBinding.btnOk.setOnClickListener {
                                dialogChangesLlmInference.dismiss()
                            }
                        }
                    } else {
                        floatingActionButtonLlm.hide()
                        floatingActionButtonLlm.isVisible = false
                        floatingActionButtonLlm.setOnClickListener(null)
                    }
                }
                localAppStackResults.observe(this@LocalAppDetailsActivity) { result ->
                    stackInfoList.setContent {
                        QQVersionListTheme { LocalAppDetailsStackWindow(result) }
                    }
                }
                appIconImage.observe(this@LocalAppDetailsActivity) { appIconImage ->
                    if (appIconImage != null) {
                        localIcon.isVisible = true
                        localIcon.load(appIconImage)
                    } else localIcon.isVisible = false
                }
                isLoading.observe(this@LocalAppDetailsActivity) { isLoading ->
                    if (isLoading) {
                        progressLine.show()
                        detailInfo.isVisible = false
                        detailInfo.setOnClickListener(null)
                    } else {
                        progressLine.hide()
                        if (isWeixin.value == false && isErr.value == false) {
                            detailInfo.isVisible = true
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

                                val dialogLocalQqTimInfoBinding =
                                    DialogLocalQqTimInfoBinding.inflate(
                                        LayoutInflater.from(this@LocalAppDetailsActivity)
                                    )

                                MaterialAlertDialogBuilder(this@LocalAppDetailsActivity).setView(
                                    dialogLocalQqTimInfoBinding.root
                                ).setTitle(R.string.localDetailsMsg)
                                    .setIcon(R.drawable.phone_find_line).apply {
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
                                                } else dialogLocalAppsettingParamsPad.isVisible =
                                                    false
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
                        } else {
                            detailInfo.isVisible = false
                            detailInfo.setOnClickListener(null)
                        }
                    }
                }
                channelText.observe(this@LocalAppDetailsActivity) { text ->
                    if (text == "" || isWeixin.value == true) binding.localChannelCard.isVisible =
                        false else {
                        localChannelCard.isVisible = true
                        localChannelText.text = text
                    }
                }
                isErr.observe(this@LocalAppDetailsActivity) { isErr ->
                    stackInfo.isVisible = !isErr
                }
                observeString(appName, binding.localName)
                observeString(localVersion, binding.localVersion)
                observeString(localSDKText, binding.localSdk)
                observeStringWithVisible(isTIM, timBasedVer, binding.localTimBase)
            }
        }

        try {
            val intent = intent
            when {
                intent.hasExtra("localAppType") -> {
                    val localAppType = intent.getStringExtra("localAppType")
                    if (localAppType == "QQ" || localAppType == "TIM" || localAppType == "Weixin") viewModel.getInfo(
                        this, localAppType
                    )
                    if (localAppType == "QQ") binding.topAppBar.title =
                        getString(R.string.localQQVersionDetails)
                    else if (localAppType == "TIM") binding.topAppBar.title =
                        getString(R.string.localTIMVersionDetails)
                    else if (localAppType == "Weixin") binding.topAppBar.title =
                        getString(R.string.localWeixinVersionDetails)
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.cleanCache(this)
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

    private fun showStackDescDialog(
        titleRes: Int, messageRes: Int, url: String?, iconRes: Int? = null
    ) {
        MaterialAlertDialogBuilder(this@LocalAppDetailsActivity).setTitle(titleRes)
            .setMessage(messageRes).setPositiveButton(R.string.done) { _, _ -> }.apply {
                if (iconRes != null) setIcon(iconRes)
                if (url != null) setNeutralButton(R.string.details) { _, _ ->
                    openUrlWithChromeCustomTab(url)
                }
            }.show()
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun LocalAppDetailsStackWindow(
        result: MutableList<LocalAppStackResult>
    ) {
        Column {
            (if (result.isEmpty()) mutableListOf() else result).sortedWith(compareBy<LocalAppStackResult> {
                if (RULES_ID_ORDER.indexOf(it.id) == -1) Int.MAX_VALUE
                else RULES_ID_ORDER.indexOf(it.id)
            }.thenComparing(compareBy<LocalAppStackResult> { it.id.lowercase() })
            ).forEach { item -> StackItem(item = item) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun StackItem(item: LocalAppStackResult) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ), onClick = {
            when (item.id) {
                LocalAppDetailsActivityViewModel.RULE_ID_QQNT -> showStackDescDialog(
                    R.string.localDetailsQQNT,
                    R.string.localDetailsQQNTDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.qqnt_logo_unofficial_fix
                )

                LocalAppDetailsActivityViewModel.RULE_ID_BUGLY -> showStackDescDialog(
                    R.string.localDetailsBugly,
                    R.string.localDetailsBuglyDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.bugly_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_UE_LIBRARY -> showStackDescDialog(
                    R.string.localDetailsUELibrary,
                    R.string.localDetailsUELibraryDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.ue_icon_2023_black
                )

                LocalAppDetailsActivityViewModel.RULE_ID_HIPPY -> showStackDescDialog(
                    R.string.localDetailsHippy,
                    R.string.localDetailsHippyDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.hippy_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_KUIKLY -> showStackDescDialog(
                    R.string.localDetailsKuikly,
                    R.string.localDetailsKuiklyDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.kuikly_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_SHIPLY -> showStackDescDialog(
                    R.string.localDetailsShiply,
                    R.string.localDetailsShiplyDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.shiply_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_RIGHTLY -> showStackDescDialog(
                    R.string.localDetailsRightly,
                    R.string.localDetailsRightlyDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.rightly_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BEACON -> showStackDescDialog(
                    R.string.localDetailsTencentBeacon,
                    R.string.localDetailsTencentBeaconDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.beacon_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_COMPOSE_MULTIPLATFORM -> showStackDescDialog(
                    R.string.localDetailsComposeMultiplatform,
                    R.string.localDetailsComposeMultiplatformDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.compose
                )

                LocalAppDetailsActivityViewModel.RULE_ID_JETPACK_COMPOSE -> showStackDescDialog(
                    R.string.localDetailsJetpackCompose,
                    R.string.localDetailsJetpackComposeDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.compose
                )

                LocalAppDetailsActivityViewModel.RULE_ID_FLUTTER -> showStackDescDialog(
                    R.string.localDetailsFlutter,
                    R.string.localDetailsFlutterDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.flutter_line
                )

                LocalAppDetailsActivityViewModel.RULE_ID_MMKV -> showStackDescDialog(
                    R.string.localDetailsMMKV,
                    R.string.localDetailsMMKVDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.oteam_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_WCDB -> showStackDescDialog(
                    R.string.localDetailsWCDB,
                    R.string.localDetailsWCDBDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.oteam_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_MARS -> showStackDescDialog(
                    R.string.localDetailsMars,
                    R.string.localDetailsMarsDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.oteam_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_MATRIX -> showStackDescDialog(
                    R.string.localDetailsMatrix,
                    R.string.localDetailsMatrixDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.oteam_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_TINKER -> showStackDescDialog(
                    R.string.localDetailsTinker,
                    R.string.localDetailsTinkerDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.oteam_official
                )

                LocalAppDetailsActivityViewModel.RULE_ID_REACT_NATIVE -> showStackDescDialog(
                    R.string.localDetailsReactNative,
                    R.string.localDetailsReactNativeDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.reactjs_line
                )

                LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BROWSING_SERVICE -> showStackDescDialog(
                    R.string.localDetailsTBS,
                    R.string.localDetailsTBSDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.tencent_logo
                )

                LocalAppDetailsActivityViewModel.RULE_ID_SKYLINE_RENDERING_ENGINE -> showStackDescDialog(
                    R.string.localDetailsSkylineRenderingEngine,
                    R.string.localDetailsSkylineRenderingEngineDesc,
                    DEX_PRE_RULES.find { it.id == item.id }?.url,
                    R.drawable.mini_program_line
                )

                else -> null
            }
        }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = getIconResId(item.id)
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(start = 4.dp, end = 4.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp, end = 6.dp)
                ) {
                    Text(
                        text = getItemTitle(item.id),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = stringResource(id = R.string.thisVerContains, item.dex),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.arrow_right_s_line),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    private fun getIconResId(id: String): Int {
        return when (id) {
            LocalAppDetailsActivityViewModel.RULE_ID_QQNT -> R.drawable.qqnt_logo_unofficial_fix
            LocalAppDetailsActivityViewModel.RULE_ID_BUGLY -> R.drawable.bugly_official
            LocalAppDetailsActivityViewModel.RULE_ID_SHIPLY -> R.drawable.shiply_official
            LocalAppDetailsActivityViewModel.RULE_ID_KUIKLY -> R.drawable.kuikly_official
            LocalAppDetailsActivityViewModel.RULE_ID_HIPPY -> R.drawable.hippy_official
            LocalAppDetailsActivityViewModel.RULE_ID_RIGHTLY -> R.drawable.rightly_official
            LocalAppDetailsActivityViewModel.RULE_ID_UE_LIBRARY -> R.drawable.ue_icon_2023_black
            LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BEACON -> R.drawable.beacon_official
            LocalAppDetailsActivityViewModel.RULE_ID_JETPACK_COMPOSE -> R.drawable.compose
            LocalAppDetailsActivityViewModel.RULE_ID_COMPOSE_MULTIPLATFORM -> R.drawable.compose
            LocalAppDetailsActivityViewModel.RULE_ID_FLUTTER -> R.drawable.flutter_line
            LocalAppDetailsActivityViewModel.RULE_ID_REACT_NATIVE -> R.drawable.reactjs_line
            LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BROWSING_SERVICE -> R.drawable.tencent_logo
            LocalAppDetailsActivityViewModel.RULE_ID_SKYLINE_RENDERING_ENGINE -> R.drawable.mini_program_line
            else -> when (DEX_PRE_RULES.find { it.id == id }?.type) {
                RULE_TYPE_PRIVATE_TENCENT -> R.drawable.tencent_logo
                RULE_TYPE_OPEN_SOURCE_3RD_PARTY -> R.drawable.open_source_line
                RULE_TYPE_OTEAM_TENCENT -> R.drawable.oteam_official
                else -> R.drawable.tools_line
            }
        }
    }

    @Composable
    private fun getItemTitle(id: String): String {
        return when (id) {
            LocalAppDetailsActivityViewModel.RULE_ID_QQNT -> stringResource(R.string.localDetailsQQNT)
            LocalAppDetailsActivityViewModel.RULE_ID_BUGLY -> stringResource(R.string.localDetailsBugly)
            LocalAppDetailsActivityViewModel.RULE_ID_SHIPLY -> stringResource(R.string.localDetailsShiply)
            LocalAppDetailsActivityViewModel.RULE_ID_KUIKLY -> stringResource(R.string.localDetailsKuikly)
            LocalAppDetailsActivityViewModel.RULE_ID_HIPPY -> stringResource(R.string.localDetailsHippy)
            LocalAppDetailsActivityViewModel.RULE_ID_RIGHTLY -> stringResource(R.string.localDetailsRightly)
            LocalAppDetailsActivityViewModel.RULE_ID_UE_LIBRARY -> stringResource(R.string.localDetailsUELibrary)
            LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BEACON -> stringResource(R.string.localDetailsTencentBeacon)
            LocalAppDetailsActivityViewModel.RULE_ID_JETPACK_COMPOSE -> stringResource(R.string.localDetailsJetpackCompose)
            LocalAppDetailsActivityViewModel.RULE_ID_COMPOSE_MULTIPLATFORM -> stringResource(R.string.localDetailsComposeMultiplatform)
            LocalAppDetailsActivityViewModel.RULE_ID_FLUTTER -> stringResource(R.string.localDetailsFlutter)
            LocalAppDetailsActivityViewModel.RULE_ID_REACT_NATIVE -> stringResource(R.string.localDetailsReactNative)
            LocalAppDetailsActivityViewModel.RULE_ID_TENCENT_BROWSING_SERVICE -> stringResource(R.string.localDetailsTBS)
            LocalAppDetailsActivityViewModel.RULE_ID_SKYLINE_RENDERING_ENGINE -> stringResource(R.string.localDetailsSkylineRenderingEngine)
            else -> id
        }
    }
}
