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

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_QQ_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_TIM_PACKAGE_NAME
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.DexResolver
import com.xiaoniu.qqversionlist.util.FileUtil.ZipFileCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.File
import java.nio.charset.Charset
import kotlin.use

class LocalAppDetailsActivityViewModel : ViewModel() {
    companion object {
        val DEX_QQNT = arrayOf("com.tencent.qqnt")
        val DEX_BUGLY = arrayOf("com.tencent.bugly")
        val DEX_SHIPLY = arrayOf("com.tencent.rdelivery")
        val DEX_KUIKLY = arrayOf("com.tencent.kuikly", "kuikly.com.tencent")
        val DEX_HIPPY = arrayOf("com.tencent.hippy")
        val DEX_RIGHTLY = arrayOf("com.tdsrightly", "com.tencent.rightly", "com.tds.rightly")
        val DEX_UE_LIBRARY = arrayOf("com.epicgames.ue4", "com.epicgames.ue5")
        val DEX_TENCENT_BEACON = arrayOf("com.tencent.beacon")
        val DEX_JETPACK_COMPOSE = arrayOf("androidx.compose")
        val DEX_COMPOSE_MULTIPLATFORM = arrayOf("org.jetbrains.compose")
        val DEX_FLUTTER = arrayOf("io.flutter")
    }

    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isErr = MutableLiveData<Boolean>().apply { value = false }
    val isErr: LiveData<Boolean> = _isErr

    private val _localVersion = MutableLiveData<String>().apply { value = "" }
    val localVersion: LiveData<String> = _localVersion

    private val _channelText = MutableLiveData<String>().apply { value = "" }
    val channelText: LiveData<String> = _channelText

    private val _localSDKText = MutableLiveData<String>().apply { value = "" }
    val localSDKText: LiveData<String> = _localSDKText

    private val _hasQQNT = MutableLiveData<Boolean>().apply { value = false }
    val hasQQNT: LiveData<Boolean> = _hasQQNT

    private val _hasQQNTDesc = MutableLiveData<String>().apply { value = "" }
    val hasQQNTDesc: LiveData<String> = _hasQQNTDesc

    private val _hasUELibrary = MutableLiveData<Boolean>().apply { value = false }
    val hasUELibrary: LiveData<Boolean> = _hasUELibrary

    private val _hasUELibraryDesc = MutableLiveData<String>().apply { value = "" }
    val hasUELibraryDesc: LiveData<String> = _hasUELibraryDesc

    private val _hasBugly = MutableLiveData<Boolean>().apply { value = false }
    val hasBugly: LiveData<Boolean> = _hasBugly

    private val _hasBuglyDesc = MutableLiveData<String>().apply { value = "" }
    val hasBuglyDesc: LiveData<String> = _hasBuglyDesc

    private val _hasShiply = MutableLiveData<Boolean>().apply { value = false }
    val hasShiply: LiveData<Boolean> = _hasShiply

    private val _hasShiplyDesc = MutableLiveData<String>().apply { value = "" }
    val hasShiplyDesc: LiveData<String> = _hasShiplyDesc

    private val _hasKuikly = MutableLiveData<Boolean>().apply { value = false }
    val hasKuikly: LiveData<Boolean> = _hasKuikly

    private val _hasKuiklyDesc = MutableLiveData<String>().apply { value = "" }
    val hasKuiklyDesc: LiveData<String> = _hasKuiklyDesc

    private val _hasHippy = MutableLiveData<Boolean>().apply { value = false }
    val hasHippy: LiveData<Boolean> = _hasHippy

    private val _hasHippyDesc = MutableLiveData<String>().apply { value = "" }
    val hasHippyDesc: LiveData<String> = _hasHippyDesc

    private val _hasRightly = MutableLiveData<Boolean>().apply { value = false }
    val hasRightly: LiveData<Boolean> = _hasRightly

    private val _hasRightlyDesc = MutableLiveData<String>().apply { value = "" }
    val hasRightlyDesc: LiveData<String> = _hasRightlyDesc

    private val _hasTencentBeacon = MutableLiveData<Boolean>().apply { value = false }
    val hasTencentBeacon: LiveData<Boolean> = _hasTencentBeacon

    private val _hasTencentBeaconDesc = MutableLiveData<String>().apply { value = "" }
    val hasTencentBeaconDesc: LiveData<String> = _hasTencentBeaconDesc

    private val _hasJetpackCompose = MutableLiveData<Boolean>().apply { value = false }
    val hasJetpackCompose: LiveData<Boolean> = _hasJetpackCompose

    private val _hasJetpackComposeDesc = MutableLiveData<String>().apply { value = "" }
    val hasJetpackComposeDesc: LiveData<String> = _hasJetpackComposeDesc

    private val _hasComposeMultiplatform = MutableLiveData<Boolean>().apply { value = false }
    val hasComposeMultiplatform: LiveData<Boolean> = _hasComposeMultiplatform

    private val _hasComposeMultiplatformDesc = MutableLiveData<String>().apply { value = "" }
    val hasComposeMultiplatformDesc: LiveData<String> = _hasComposeMultiplatformDesc

    private val _hasFlutter = MutableLiveData<Boolean>().apply { value = false }
    val hasFlutter: LiveData<Boolean> = _hasFlutter

    private val _hasFlutterDesc = MutableLiveData<String>().apply { value = "" }
    val hasFlutterDesc: LiveData<String> = _hasFlutterDesc

    private val _isTIM = MutableLiveData<Boolean>().apply { value = false }
    val isTIM: LiveData<Boolean> = _isTIM

    private val _timBasedVer = MutableLiveData<String>().apply { value = "" }
    val timBasedVer: LiveData<String> = _timBasedVer

    // 基础信息
    private val _appName = MutableLiveData<String>().apply { value = "" }
    val appName: LiveData<String> = _appName

    private val _appIconImage = MutableLiveData<Drawable>()
    val appIconImage: LiveData<Drawable> = _appIconImage

    private val _targetSDK = MutableLiveData<Int>().apply { value = 0 }
    val targetSDK: LiveData<Int> = _targetSDK

    private val _minSDK = MutableLiveData<Int>().apply { value = 0 }
    val minSDK: LiveData<Int> = _minSDK

    private val _compileSDK = MutableLiveData<Int>().apply { value = 0 }
    val compileSDK: LiveData<Int> = _compileSDK

    private val _versionName = MutableLiveData<String>().apply { value = "" }
    val versionName: LiveData<String> = _versionName

    private val _rdmUUID = MutableLiveData<String>().apply { value = "" }
    val rdmUUID: LiveData<String> = _rdmUUID

    private val _versionCode = MutableLiveData<String>().apply { value = "" }
    val versionCode: LiveData<String> = _versionCode

    private val _appSettingParams = MutableLiveData<String>().apply { value = "" }
    val appSettingParams: LiveData<String> = _appSettingParams

    private val _appSettingParamsPad = MutableLiveData<String>().apply { value = "" }
    val appSettingParamsPad: LiveData<String> = _appSettingParamsPad

    private val _qua = MutableLiveData<String>().apply { value = "" }
    val qua: LiveData<String> = _qua

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun setErr(isErr: Boolean) {
        _isErr.value = isErr
    }

    fun setLocalVersion(version: String) {
        _localVersion.value = version
    }

    fun setChannelText(text: String) {
        _channelText.value = text
    }

    fun setLocalSDKText(text: String) {
        _localSDKText.value = text
    }

    fun setHasQQNT(hasQQNT: Boolean) {
        _hasQQNT.value = hasQQNT
    }

    fun setHasQQNTDesc(context: Context, dex: String) {
        _hasQQNTDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasUELibrary(hasUELibrary: Boolean) {
        _hasUELibrary.value = hasUELibrary
    }

    fun setHasUELibraryDesc(context: Context, dex: String) {
        _hasUELibraryDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasBugly(hasBugly: Boolean) {
        _hasBugly.value = hasBugly
    }

    fun setHasBuglyDesc(context: Context, dex: String) {
        _hasBuglyDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasShiply(hasShiply: Boolean) {
        _hasShiply.value = hasShiply
    }

    fun setHasShiplyDesc(context: Context, dex: String) {
        _hasShiplyDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasKuikly(hasKuikly: Boolean) {
        _hasKuikly.value = hasKuikly
    }

    fun setHasKuiklyDesc(context: Context, dex: String) {
        _hasKuiklyDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasHippy(hasHippy: Boolean) {
        _hasHippy.value = hasHippy
    }

    fun setHasHippyDesc(context: Context, dex: String) {
        _hasHippyDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasRightly(hasRightly: Boolean) {
        _hasRightly.value = hasRightly
    }

    fun setHasRightlyDesc(context: Context, dex: String) {
        _hasRightlyDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasTencentBeacon(hasTencentBeacon: Boolean) {
        _hasTencentBeacon.value = hasTencentBeacon
    }

    fun setHasTencentBeaconDesc(context: Context, dex: String) {
        _hasTencentBeaconDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasJetpackCompose(hasCompose: Boolean) {
        _hasJetpackCompose.value = hasCompose
    }

    fun setHasJetpackComposeDesc(context: Context, dex: String) {
        _hasJetpackComposeDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasComposeMultiplatform(hasComposeMultiplatform: Boolean) {
        _hasComposeMultiplatform.value = hasComposeMultiplatform
    }

    fun setHasComposeMultiplatformDesc(context: Context, dex: String) {
        _hasComposeMultiplatformDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setHasFlutter(hasFlutter: Boolean) {
        _hasFlutter.value = hasFlutter
    }

    fun setHasFlutterDesc(context: Context, dex: String) {
        _hasFlutterDesc.value = context.getString(R.string.thisVerContains, dex)
    }

    fun setIsTIM(isTIM: Boolean) {
        _isTIM.value = isTIM
    }

    fun setTIMBasedVer(context: Context, ver: String) {
        _timBasedVer.value = context.getString(R.string.basedOnQQVer, ver)
    }

    fun setAppName(appName: String) {
        _appName.value = appName
    }

    fun setAppIconImage(appIconImage: Drawable) {
        _appIconImage.value = appIconImage
    }

    fun setTargetSDK(targetSDK: Int) {
        _targetSDK.value = targetSDK
    }

    fun setMinSDK(minSDK: Int) {
        _minSDK.value = minSDK
    }

    fun setCompileSDK(compileSDK: Int) {
        _compileSDK.value = compileSDK
    }

    fun setVersionName(versionName: String) {
        _versionName.value = versionName
    }

    fun setRdmUUID(rdmUUID: String) {
        _rdmUUID.value = rdmUUID
    }

    fun setVersionCode(versionCode: String) {
        _versionCode.value = versionCode
    }

    fun setAppSettingParams(appSettingParams: String) {
        _appSettingParams.value = appSettingParams
    }

    fun setAppSettingParamsPad(appSettingParamsPad: String) {
        _appSettingParamsPad.value = appSettingParamsPad
    }

    fun setQua(qua: String) {
        _qua.value = qua
    }

    fun getInfo(activity: Activity, type: String, appPath: String? = null) {
        setLoading(true)
        var packageInfo: PackageInfo? = null
        var applicationInfo: ApplicationInfo? = null
        if (type == "inter") {
            val packageInfoPre = activity.packageManager.getPackageArchiveInfo(
                appPath!!, PackageManager.GET_META_DATA
            )
            val applicationInfoPre = packageInfoPre?.applicationInfo
            if (packageInfoPre != null && applicationInfoPre != null) {
                packageInfo = packageInfoPre
                applicationInfo = applicationInfoPre
            }
        } else {
            setIsTIM(type == "TIM")
            packageInfo = activity.packageManager.getPackageInfo(
                if (type == "TIM") ANDROID_TIM_PACKAGE_NAME else ANDROID_QQ_PACKAGE_NAME, 0
            )
            applicationInfo = activity.packageManager.getApplicationInfo(
                if (type == "TIM") ANDROID_TIM_PACKAGE_NAME else ANDROID_QQ_PACKAGE_NAME,
                PackageManager.GET_META_DATA
            )
        }
        if (packageInfo == null || applicationInfo == null) {
            setLoading(false)
            setAppName(activity.getString(R.string.unknownErr))
            cleanCache(activity)
            setErr(true)
            return
        }
        val packageName = getAppPackageName(applicationInfo)
        if (packageName == ANDROID_QQ_PACKAGE_NAME || packageName == ANDROID_TIM_PACKAGE_NAME) {
            if (packageName == ANDROID_TIM_PACKAGE_NAME) setIsTIM(true)
            val jobs = mutableListOf<Job>().apply {
                add(CoroutineScope(Dispatchers.IO).launch {
                    val appName = getAppName(applicationInfo, activity)
                    withContext(Dispatchers.Main) {
                        setAppName(appName)
                    }
                })
                add(CoroutineScope(Dispatchers.IO).launch {
                    val appIconImage = getAppIconImage(applicationInfo, activity)
                    if (appIconImage != null) withContext(Dispatchers.Main) {
                        setAppIconImage(appIconImage)
                    }
                })
                add(CoroutineScope(Dispatchers.IO).launch {
                    val targetSDK = getTargetSDK(applicationInfo)
                    withContext(Dispatchers.Main) {
                        setTargetSDK(targetSDK)
                    }
                })
                add(CoroutineScope(Dispatchers.IO).launch {
                    val minSDK = getMinSDK(applicationInfo)
                    withContext(Dispatchers.Main) {
                        setMinSDK(minSDK)
                    }
                })
                add(CoroutineScope(Dispatchers.IO).launch {
                    val compileSDK = getCompileSDK(applicationInfo)
                    withContext(Dispatchers.Main) {
                        if (compileSDK != null) setCompileSDK(compileSDK) else setCompileSDK(0)
                    }
                })
                checkAndSetProperty(this, ::getVersionName, ::setVersionName, packageInfo)
                checkAndSetProperty(this, ::getRdmUUID, ::setRdmUUID, applicationInfo)
                checkAndSetProperty(this, ::getVersionCode, ::setVersionCode, packageInfo)
                checkAndSetProperty(
                    this, ::getAppSettingParams, ::setAppSettingParams, applicationInfo
                )
                checkAndSetProperty(
                    this, ::getAppSettingParamsPad, ::setAppSettingParamsPad, applicationInfo
                )
                add(CoroutineScope(Dispatchers.IO).launch {
                    val qua = getQua(packageInfo)
                    withContext(Dispatchers.Main) {
                        setQua(if (qua.isNullOrEmpty()) "" else qua.replace("\n", ""))
                    }
                })
                checkAndSetLibrary(
                    this,
                    ::checkQQNT,
                    ::setHasQQNT,
                    ::setHasQQNTDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkUELibrary,
                    ::setHasUELibrary,
                    ::setHasUELibraryDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkBugly,
                    ::setHasBugly,
                    ::setHasBuglyDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkShiply,
                    ::setHasShiply,
                    ::setHasShiplyDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkKuikly,
                    ::setHasKuikly,
                    ::setHasKuiklyDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkHippy,
                    ::setHasHippy,
                    ::setHasHippyDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkRightly,
                    ::setHasRightly,
                    ::setHasRightlyDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkTencentBeacon,
                    ::setHasTencentBeacon,
                    ::setHasTencentBeaconDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkJetpackCompose,
                    ::setHasJetpackCompose,
                    ::setHasJetpackComposeDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkComposeMultiplatform,
                    ::setHasComposeMultiplatform,
                    ::setHasComposeMultiplatformDesc,
                    activity,
                    applicationInfo.sourceDir
                )
                checkAndSetLibrary(
                    this,
                    ::checkFlutter,
                    ::setHasFlutter,
                    ::setHasFlutterDesc,
                    activity,
                    applicationInfo.sourceDir
                )
            }
            CoroutineScope(Dispatchers.Main).launch {
                jobs.joinAll()
                setLoading(false)
                compileSDK.value?.let { sdkVersion ->
                    if (sdkVersion != 0) setLocalSDKText("Target ${targetSDK.value} | Min ${minSDK.value} | Compile $sdkVersion") else setLocalSDKText(
                        "Target ${targetSDK.value} | Min ${minSDK.value}"
                    )
                }
                versionCode.value?.let { versionCode ->
                    rdmUUID.value?.let { rdmUUID ->
                        setLocalVersion("${versionName.value}.${rdmUUID.split("_")[0]} ($versionCode)")
                    } ?: setLocalVersion("${versionName.value}.${rdmUUID.value!!.split("_")[0]}")
                } ?: setLocalVersion("${versionName.value}.${rdmUUID.value!!.split("_")[0]}")
                appSettingParams.value?.let { appSettingParams ->
                    val parts = appSettingParams.split("#")
                    if (parts.size > 3) setChannelText(parts[3]) else setChannelText("")
                } ?: setChannelText("")
                if (isTIM.value == true) qua.value?.let { qua ->
                    setTIMBasedVer(activity, if (qua.length > 3) qua.split("_")[3] else "")
                }
                cleanCache(activity)
            }
        } else {
            setAppName(activity.getString(R.string.packageNameIsErr))
            setLoading(false)
            setErr(true)
            cleanCache(activity)
            return
        }
    }

    private fun getAppPackageName(applicationInfo: ApplicationInfo): String {
        return applicationInfo.packageName
    }

    private fun getAppName(applicationInfo: ApplicationInfo, activity: Activity): String {
        return applicationInfo.loadLabel(activity.packageManager).toString()
    }

    private fun getAppIconImage(applicationInfo: ApplicationInfo, activity: Activity): Drawable? {
        return applicationInfo.loadIcon(activity.packageManager)
    }

    private fun getTargetSDK(applicationInfo: ApplicationInfo): Int {
        return applicationInfo.targetSdkVersion
    }

    private fun getMinSDK(applicationInfo: ApplicationInfo): Int {
        return applicationInfo.minSdkVersion
    }

    private fun getCompileSDK(applicationInfo: ApplicationInfo): Int? {
        return if (SDK_INT >= Build.VERSION_CODES.S) applicationInfo.compileSdkVersion else null
    }

    private fun getVersionName(packageInfo: PackageInfo): String? {
        return packageInfo.versionName
    }

    private fun getRdmUUID(applicationInfo: ApplicationInfo): String? {
        return applicationInfo.metaData?.getString("com.tencent.rdm.uuid")
    }

    private fun getVersionCode(packageInfo: PackageInfo): String? {
        return if (SDK_INT >= Build.VERSION_CODES.P) packageInfo.longVersionCode.toString() else null
    }

    private fun getAppSettingParams(applicationInfo: ApplicationInfo): String? {
        return applicationInfo.metaData?.getString("AppSetting_params")
    }

    private fun getAppSettingParamsPad(applicationInfo: ApplicationInfo): String? {
        return applicationInfo.metaData?.getString("AppSetting_params_pad")
    }

    private fun getQua(packageInfo: PackageInfo): String? {
        val sourceDir = packageInfo.applicationInfo?.sourceDir ?: return null
        val file = File(sourceDir)
        if (!file.exists()) return null
        return runCatching {
            ZipFileCompat(file).use { zipFile ->
                val entry = zipFile.getEntry("assets/qua.ini") ?: return@runCatching null
                zipFile.getInputStream(entry).use { inputStream ->
                    IOUtils.toString(inputStream, Charset.defaultCharset())
                }
            }
        }.getOrElse { exception -> throw Exception(exception) }
    }

    private fun checkQQNT(appPath: String): String? = checkLibrary(appPath, DEX_QQNT)
    private fun checkUELibrary(appPath: String): String? = checkLibrary(appPath, DEX_UE_LIBRARY)
    private fun checkBugly(appPath: String): String? = checkLibrary(appPath, DEX_BUGLY)
    private fun checkShiply(appPath: String): String? = checkLibrary(appPath, DEX_SHIPLY)
    private fun checkKuikly(appPath: String): String? = checkLibrary(appPath, DEX_KUIKLY)
    private fun checkHippy(appPath: String): String? = checkLibrary(appPath, DEX_HIPPY)
    private fun checkRightly(appPath: String): String? = checkLibrary(appPath, DEX_RIGHTLY)
    private fun checkTencentBeacon(appPath: String): String? =
        checkLibrary(appPath, DEX_TENCENT_BEACON)

    private fun checkJetpackCompose(appPath: String): String? =
        checkLibrary(appPath, DEX_JETPACK_COMPOSE)

    private fun checkComposeMultiplatform(appPath: String): String? =
        checkLibrary(appPath, DEX_COMPOSE_MULTIPLATFORM)

    private fun checkFlutter(appPath: String): String? = checkLibrary(appPath, DEX_FLUTTER)

    private fun checkLibrary(appPath: String, dexList: Array<String>): String? {
        dexList.forEach { dex ->
            val findResult = DexResolver.findPackage(appPath, dex)
            if (findResult.getOrDefault(false) == true) return dex
        }
        return null
    }

    private fun <T> checkAndSetProperty(
        jobs: MutableList<Job>,
        checkFunction: (T) -> String?,
        setProperty: (String) -> Unit,
        param: T
    ) {
        jobs.add(CoroutineScope(Dispatchers.IO).launch {
            val result = checkFunction(param)
            withContext(Dispatchers.Main) { setProperty(if (result.isNullOrEmpty()) "" else result) }
        })
    }

    private fun checkAndSetLibrary(
        jobs: MutableList<Job>,
        checkFunction: (String) -> String?,
        setHasLibrary: (Boolean) -> Unit,
        setHasLibraryDesc: (Activity, String) -> Unit,
        activity: Activity,
        sourceDir: String
    ) {
        jobs.add(CoroutineScope(Dispatchers.IO).launch {
            val result = checkFunction(sourceDir)
            withContext(Dispatchers.Main) {
                if (result != null) {
                    setHasLibrary(true)
                    setHasLibraryDesc(activity, result)
                } else setHasLibrary(false)
            }
        })
    }

    private fun cleanCache(activity: Activity) {
        val cacheDir = File(activity.cacheDir, "apkAnalysis")
        if (cacheDir.exists()) cacheDir.deleteRecursively()
    }
}