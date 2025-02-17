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

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.pm.ProviderInfo
import android.content.pm.ServiceInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_QQ_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_TIM_PACKAGE_NAME
import com.xiaoniu.qqversionlist.QverbowApplication.Companion.ANDROID_WECHAT_PACKAGE_NAME
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.LocalAppStackResult
import com.xiaoniu.qqversionlist.data.LocalAppStackRule
import com.xiaoniu.qqversionlist.util.DexResolver
import com.xiaoniu.qqversionlist.util.FileUtil.ZipFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileInputStream
import java.nio.charset.Charset
import java.security.MessageDigest
import kotlin.use

class LocalAppDetailsActivityViewModel : ViewModel() {
    companion object {
        const val RULE_TYPE_PRIVATE_TENCENT = "Tencent Private" // 腾讯私有库
        const val RULE_TYPE_PRIVATE_3RD_PARTY = "3rd Party Private" // 第三方私有库
        const val RULE_TYPE_OTEAM_TENCENT = "Tencent Oteam" // 腾讯开源协同
        const val RULE_TYPE_OPEN_SOURCE_3RD_PARTY = "3rd Party Open Source" // 第三方开源库

        const val RULE_ID_QQNT = "QQNT"
        const val RULE_ID_BUGLY = "Bugly"
        const val RULE_ID_SHIPLY = "Shiply"
        const val RULE_ID_KUIKLY = "Kuikly"
        const val RULE_ID_HIPPY = "Hippy"
        const val RULE_ID_RIGHTLY = "Rightly"
        const val RULE_ID_UE_LIBRARY = "UELibrary"
        const val RULE_ID_TENCENT_BEACON = "腾讯灯塔"
        const val RULE_ID_JETPACK_COMPOSE = "Jetpack Compose"
        const val RULE_ID_COMPOSE_MULTIPLATFORM = "Compose Multiplatform"
        const val RULE_ID_FLUTTER = "Flutter"
        const val RULE_ID_MMKV = "MMKV"
        const val RULE_ID_WCDB = "WCDB"
        const val RULE_ID_MARS = "Mars"
        const val RULE_ID_MATRIX = "Matrix"
        const val RULE_ID_TINKER = "Tinker"
        const val RULE_ID_REACT_NATIVE = "React Native"
        const val RULE_ID_TENCENT_BROWSING_SERVICE = "腾讯浏览服务"

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
        val DEX_MMKV = arrayOf("com.tencent.mmkv")
        val DEX_WCDB = arrayOf("com.tencent.wcdb")
        val DEX_MARS = arrayOf("com.tencent.mars")
        val DEX_MATRIX = arrayOf("com.tencent.matrix")
        val DEX_TINKER = arrayOf("com.tencent.tinker")
        val DEX_REACT_NATIVE = arrayOf("com.facebook.react")
        val DEX_TENCENT_BROWSING_SERVICE = arrayOf("com.tencent.tbs")

        const val URL_BUGLY = "https://bugly.tds.qq.com/v2/index/tds-main"
        const val URL_UE_LIBRARY =
            "https://dev.epicgames.com/documentation/unreal-engine/building-unreal-engine-as-a-library"
        const val URL_HIPPY = "https://openhippy.com/"
        const val URL_SHIPLY = "https://shiply.tds.qq.com/"
        const val URL_RIGHTLY = "https://rightly.tds.qq.com/"
        const val URL_TENCENT_BEACON = "https://beacon.qq.com/"
        const val URL_JETPACK_COMPOSE = "https://developer.android.com/compose"
        const val URL_COMPOSE_MULTIPLATFORM = "https://www.jetbrains.com/compose-multiplatform/"
        const val URL_FLUTTER = "https://flutter.dev/"
        const val URL_MMKV = "https://github.com/Tencent/MMKV"
        const val URL_WCDB = "https://github.com/Tencent/wcdb"
        const val URL_MARS = "https://github.com/Tencent/Mars"
        const val URL_MATRIX = "https://github.com/Tencent/matrix"
        const val URL_TINKER = "https://github.com/Tencent/tinker"
        const val URL_REACT_NATIVE = "https://reactnative.dev/"
        const val URL_TENCENT_BROWSING_SERVICE = "https://x5.tencent.com/"

        val DEX_PRE_RULES = listOf<LocalAppStackRule>(
            LocalAppStackRule(RULE_ID_QQNT, DEX_QQNT, RULE_TYPE_PRIVATE_TENCENT),
            LocalAppStackRule(RULE_ID_BUGLY, DEX_BUGLY, RULE_TYPE_PRIVATE_TENCENT, URL_BUGLY),
            LocalAppStackRule(RULE_ID_SHIPLY, DEX_SHIPLY, RULE_TYPE_PRIVATE_TENCENT, URL_SHIPLY),
            LocalAppStackRule(RULE_ID_KUIKLY, DEX_KUIKLY, RULE_TYPE_PRIVATE_TENCENT),
            LocalAppStackRule(RULE_ID_HIPPY, DEX_HIPPY, RULE_TYPE_PRIVATE_TENCENT, URL_HIPPY),
            LocalAppStackRule(RULE_ID_RIGHTLY, DEX_RIGHTLY, RULE_TYPE_PRIVATE_TENCENT, URL_RIGHTLY),
            LocalAppStackRule(
                RULE_ID_UE_LIBRARY, DEX_UE_LIBRARY, RULE_TYPE_PRIVATE_3RD_PARTY, URL_UE_LIBRARY
            ),
            LocalAppStackRule(
                RULE_ID_TENCENT_BEACON,
                DEX_TENCENT_BEACON,
                RULE_TYPE_PRIVATE_TENCENT,
                URL_TENCENT_BEACON
            ),
            LocalAppStackRule(
                RULE_ID_JETPACK_COMPOSE,
                DEX_JETPACK_COMPOSE,
                RULE_TYPE_OPEN_SOURCE_3RD_PARTY,
                URL_JETPACK_COMPOSE
            ),
            LocalAppStackRule(
                RULE_ID_COMPOSE_MULTIPLATFORM,
                DEX_COMPOSE_MULTIPLATFORM,
                RULE_TYPE_OPEN_SOURCE_3RD_PARTY,
                URL_COMPOSE_MULTIPLATFORM
            ),
            LocalAppStackRule(
                RULE_ID_FLUTTER, DEX_FLUTTER, RULE_TYPE_OPEN_SOURCE_3RD_PARTY, URL_FLUTTER
            ),
            LocalAppStackRule(RULE_ID_MMKV, DEX_MMKV, RULE_TYPE_OTEAM_TENCENT, URL_MMKV),
            LocalAppStackRule(RULE_ID_WCDB, DEX_WCDB, RULE_TYPE_OTEAM_TENCENT, URL_WCDB),
            LocalAppStackRule(RULE_ID_MARS, DEX_MARS, RULE_TYPE_OTEAM_TENCENT, URL_MARS),
            LocalAppStackRule(RULE_ID_MATRIX, DEX_MATRIX, RULE_TYPE_OTEAM_TENCENT, URL_MATRIX),
            LocalAppStackRule(RULE_ID_TINKER, DEX_TINKER, RULE_TYPE_OTEAM_TENCENT, URL_TINKER),
            LocalAppStackRule(
                RULE_ID_REACT_NATIVE,
                DEX_REACT_NATIVE,
                RULE_TYPE_OPEN_SOURCE_3RD_PARTY,
                URL_REACT_NATIVE
            ),
            LocalAppStackRule(
                RULE_ID_TENCENT_BROWSING_SERVICE,
                DEX_TENCENT_BROWSING_SERVICE,
                RULE_TYPE_PRIVATE_TENCENT,
                URL_TENCENT_BROWSING_SERVICE
            )
        )

        val RULES_ID_ORDER = listOf(
            RULE_ID_QQNT,
            RULE_ID_COMPOSE_MULTIPLATFORM,
            RULE_ID_JETPACK_COMPOSE,
            RULE_ID_FLUTTER,
            RULE_ID_REACT_NATIVE,
            RULE_ID_UE_LIBRARY,
            RULE_ID_BUGLY,
            RULE_ID_SHIPLY,
            RULE_ID_KUIKLY,
            RULE_ID_HIPPY,
            RULE_ID_RIGHTLY
        )
    }

    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isErr = MutableLiveData<Boolean>().apply { value = false }
    val isErr: LiveData<Boolean> = _isErr

    private val _isAIShowing = MutableLiveData<Boolean>().apply { value = false }
    val isAIShowing: LiveData<Boolean> = _isAIShowing

    private val _localSM3 = MutableLiveData<String>().apply { value = "" }
    val localSM3: LiveData<String> = _localSM3

    private val _interSM3 = MutableLiveData<String>().apply { value = "" }
    val interSM3: LiveData<String> = _interSM3

    private val _localVersionNameWithInter = MutableLiveData<String>().apply { value = "" }
    val localVersionNameWithInter: LiveData<String> = _localVersionNameWithInter

    private val _localVersion = MutableLiveData<String>().apply { value = "" }
    val localVersion: LiveData<String> = _localVersion

    private val _channelText = MutableLiveData<String>().apply { value = "" }
    val channelText: LiveData<String> = _channelText

    private val _localSDKText = MutableLiveData<String>().apply { value = "" }
    val localSDKText: LiveData<String> = _localSDKText

    private val _isTIM = MutableLiveData<Boolean>().apply { value = false }
    val isTIM: LiveData<Boolean> = _isTIM

    private val _timBasedVer = MutableLiveData<String>().apply { value = "" }
    val timBasedVer: LiveData<String> = _timBasedVer

    private val _isWeixin = MutableLiveData<Boolean>().apply { value = false }
    val isWeixin: LiveData<Boolean> = _isWeixin

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

    // 栈信息检查结果
    private val _localAppStackResults =
        MutableLiveData<MutableList<LocalAppStackResult>>().apply { value = mutableListOf() }
    val localAppStackResults: LiveData<MutableList<LocalAppStackResult>> = _localAppStackResults

    private val _activityDiff = MutableLiveData<String>().apply { value = "" }
    val activityDiff: LiveData<String> = _activityDiff

    private val _serviceDiff = MutableLiveData<String>().apply { value = "" }
    val serviceDiff: LiveData<String> = _serviceDiff

    private val _providerDiff = MutableLiveData<String>().apply { value = "" }
    val providerDiff: LiveData<String> = _providerDiff

    private val _receiverDiff = MutableLiveData<String>().apply { value = "" }
    val receiverDiff: LiveData<String> = _receiverDiff

    private val _permissionDiff = MutableLiveData<String>().apply { value = "" }
    val permissionDiff: LiveData<String> = _permissionDiff

    private val _isChangesBackLLMWorking = MutableLiveData<Boolean>().apply { value = false }
    val isChangesBackLLMWorking: LiveData<Boolean> = _isChangesBackLLMWorking

    private val _changesBackLLMGenText = MutableLiveData<String>().apply { value = "" }
    val changesBackLLMGenText: LiveData<String> = _changesBackLLMGenText

    fun setLoading(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    fun setErr(isErr: Boolean) {
        _isErr.value = isErr
    }

    fun setAIShowing(isAIShowing: Boolean) {
        _isAIShowing.value = isAIShowing
    }

    fun setLocalSM3(sm3: String) {
        _localSM3.value = sm3
    }

    fun setInterSM3(sm3: String) {
        _interSM3.value = sm3
    }

    fun setLocalVersionNameWithInter(versionName: String) {
        _localVersionNameWithInter.value = versionName
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

    fun setIsTIM(isTIM: Boolean) {
        _isTIM.value = isTIM
    }

    fun setTIMBasedVer(context: Context, ver: String) {
        _timBasedVer.value = context.getString(R.string.basedOnQQVer, ver)
    }

    fun setIsWeixin(isWeixin: Boolean) {
        _isWeixin.value = isWeixin
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

    fun setLocalAppStackResults(result: MutableList<LocalAppStackResult>) {
        _localAppStackResults.value = result
    }

    fun setActivityDiff(diff: String) {
        _activityDiff.value = diff
    }

    fun setServiceDiff(diff: String) {
        _serviceDiff.value = diff
    }

    fun setProviderDiff(diff: String) {
        _providerDiff.value = diff
    }

    fun setReceiverDiff(diff: String) {
        _receiverDiff.value = diff
    }

    fun setPermissionDiff(diff: String) {
        _permissionDiff.value = diff
    }

    fun setChangesBackLLMWorking(isWorking: Boolean) {
        _isChangesBackLLMWorking.value = isWorking
    }

    fun setChangesBackLLMGenText(text: String) {
        _changesBackLLMGenText.value = text
    }

    fun getInfo(activity: Activity, type: String, appPath: String? = null) {
        setLoading(true)
        var packageInfo: PackageInfo? = null
        var applicationInfo: ApplicationInfo? = null
        if (type == "inter") {
            val packageInfoPre = activity.packageManager.getPackageArchiveInfo(
                appPath!!,
                PackageManager.GET_META_DATA or PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS or PackageManager.GET_PERMISSIONS
            )
            val applicationInfoPre = packageInfoPre?.applicationInfo
            if (packageInfoPre != null && applicationInfoPre != null) {
                packageInfo = packageInfoPre
                applicationInfo = applicationInfoPre
            }
        } else {
            setIsTIM(type == "TIM")
            setIsWeixin(type == "Weixin")
            packageInfo = activity.packageManager.getPackageInfo(
                when (type) {
                    "TIM" -> ANDROID_TIM_PACKAGE_NAME
                    "Weixin" -> ANDROID_WECHAT_PACKAGE_NAME
                    else -> ANDROID_QQ_PACKAGE_NAME
                }, 0
            )
            applicationInfo = activity.packageManager.getApplicationInfo(
                when (type) {
                    "TIM" -> ANDROID_TIM_PACKAGE_NAME
                    "Weixin" -> ANDROID_WECHAT_PACKAGE_NAME
                    else -> ANDROID_QQ_PACKAGE_NAME
                }, PackageManager.GET_META_DATA
            )
        }
        if (packageInfo == null || applicationInfo == null) {
            setErr(true)
            setAppName(activity.getString(R.string.unknownErr))
            cleanCache(activity)
            setLoading(false)
            return
        }
        val packageName = getAppPackageName(applicationInfo)
        if (packageName == ANDROID_QQ_PACKAGE_NAME || packageName == ANDROID_TIM_PACKAGE_NAME || packageName == ANDROID_WECHAT_PACKAGE_NAME) {
            when (packageName) {
                ANDROID_TIM_PACKAGE_NAME -> setIsTIM(true)
                ANDROID_WECHAT_PACKAGE_NAME -> setIsWeixin(true)
            }

            val allJobs = mutableListOf<Job>().apply {
                add(viewModelScope.launch(Dispatchers.IO) {
                    val baseJobs = mutableListOf<Job>().apply {
                        add(viewModelScope.launch(Dispatchers.IO) {
                            val appName = getAppName(applicationInfo, activity)
                            withContext(Dispatchers.Main) {
                                setAppName(appName)
                            }
                        })
                        add(viewModelScope.launch(Dispatchers.IO) {
                            val appIconImage = getAppIconImage(applicationInfo, activity)
                            if (appIconImage != null) withContext(Dispatchers.Main) {
                                setAppIconImage(appIconImage)
                            }
                        })
                        add(viewModelScope.launch(Dispatchers.IO) {
                            val targetSDK = getTargetSDK(applicationInfo)
                            withContext(Dispatchers.Main) {
                                setTargetSDK(targetSDK)
                            }
                        })
                        add(viewModelScope.launch(Dispatchers.IO) {
                            val minSDK = getMinSDK(applicationInfo)
                            withContext(Dispatchers.Main) {
                                setMinSDK(minSDK)
                            }
                        })
                        add(viewModelScope.launch(Dispatchers.IO) {
                            val compileSDK = getCompileSDK(applicationInfo)
                            withContext(Dispatchers.Main) {
                                if (compileSDK != null) setCompileSDK(compileSDK) else setCompileSDK(
                                    0
                                )
                            }
                        })
                        checkAndSetProperty(this, ::getVersionName, ::setVersionName, packageInfo)
                        checkAndSetProperty(this, ::getVersionCode, ::setVersionCode, packageInfo)
                        if (isWeixin.value == false) {
                            checkAndSetProperty(this, ::getRdmUUID, ::setRdmUUID, applicationInfo)
                            checkAndSetProperty(
                                this, ::getAppSettingParams, ::setAppSettingParams, applicationInfo
                            )
                            checkAndSetProperty(
                                this,
                                ::getAppSettingParamsPad,
                                ::setAppSettingParamsPad,
                                applicationInfo
                            )
                            add(viewModelScope.launch(Dispatchers.IO) {
                                val qua = getQua(packageInfo)
                                withContext(Dispatchers.Main) {
                                    setQua(if (qua.isNullOrEmpty()) "" else qua.replace("\n", ""))
                                }
                            })
                        }
                    }
                    baseJobs.joinAll()
                    val checkIsSamePackageJobs = mutableListOf<Job>().apply {
                        add(viewModelScope.launch(Dispatchers.IO) {
                            if (type == "inter") {
                                val interSM3 = getSM3(applicationInfo)
                                withContext(Dispatchers.Main) {
                                    setInterSM3(interSM3)
                                }
                            }
                        })
                        add(viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val applicationInfo = activity.packageManager.getApplicationInfo(
                                    packageName, PackageManager.GET_META_DATA
                                )
                                val packageInfo = activity.packageManager.getPackageInfo(
                                    packageName, 0
                                )
                                val localSM3 = getSM3(applicationInfo)
                                val localVersionName = getVersionName(packageInfo)
                                withContext(Dispatchers.Main) {
                                    setLocalSM3(localSM3)
                                    if (localVersionName != null) setLocalVersionNameWithInter(
                                        localVersionName
                                    )
                                }
                            } catch (_: Exception) {
                                withContext(Dispatchers.Main) { setLocalSM3("") }
                            }
                        })
                    }
                    checkIsSamePackageJobs.joinAll()
                    val isSamePackage = localSM3.value == interSM3.value
                    val diffPackagesJob = mutableListOf<Job>().apply {
                        add(viewModelScope.launch(Dispatchers.IO) {
                            if (type == "inter" && !isSamePackage && localSM3.value != "" && SDK_INT >= Build.VERSION_CODES.P) {
                                val localPackageInfo = activity.packageManager.getPackageInfo(
                                    packageName,
                                    PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or PackageManager.GET_PROVIDERS or PackageManager.GET_PERMISSIONS
                                )
                                val interPackageInfo = packageInfo
                                val localVersionCode = getVersionCode(localPackageInfo)
                                val interVersionCode = getVersionCode(interPackageInfo)
                                if (localVersionCode != null && interVersionCode != null) withContext(
                                    Dispatchers.Main
                                ) {
                                    compareActivities(
                                        localPackageInfo.activities, interPackageInfo.activities
                                    )
                                    compareServices(
                                        localPackageInfo.services, interPackageInfo.services
                                    )
                                    compareReceivers(
                                        localPackageInfo.receivers, interPackageInfo.receivers
                                    )
                                    compareProviders(
                                        localPackageInfo.providers, interPackageInfo.providers
                                    )
                                    comparePermissions(
                                        localPackageInfo.permissions, interPackageInfo.permissions
                                    )
                                }
                            }
                        })
                    }
                    diffPackagesJob.joinAll()
                    withContext(Dispatchers.Main) {
                        compileSDK.value?.let { sdkVersion ->
                            if (sdkVersion != 0) setLocalSDKText("Target ${targetSDK.value} | Min ${minSDK.value} | Compile $sdkVersion") else setLocalSDKText(
                                "Target ${targetSDK.value} | Min ${minSDK.value}"
                            )
                        }
                        if (isWeixin.value == true) {
                            setLocalVersion("${versionName.value} (${versionCode.value})")
                        } else {
                            versionCode.value?.let { versionCode ->
                                rdmUUID.value?.let { rdmUUID ->
                                    setLocalVersion("${versionName.value}.${rdmUUID.split("_")[0]} ($versionCode)")
                                } ?: setLocalVersion(
                                    "${versionName.value}.${rdmUUID.value!!.split("_")[0]}"
                                )
                            }
                                ?: setLocalVersion("${versionName.value}.${rdmUUID.value!!.split("_")[0]}")
                            appSettingParams.value?.let { appSettingParams ->
                                val parts = appSettingParams.split("#")
                                if (parts.size > 3) setChannelText(parts[3]) else setChannelText("")
                            } ?: setChannelText("")
                            if (isTIM.value == true) qua.value?.let { qua ->
                                setTIMBasedVer(
                                    activity, if (qua.length > 3) qua.split("_")[3] else ""
                                )
                            }
                        }
                        if (type == "inter" && !isSamePackage && localSM3.value != "" && SDK_INT >= Build.VERSION_CODES.P) setAIShowing(
                            true
                        )
                    }
                })
                add(viewModelScope.launch(Dispatchers.IO) {
                    val semaphore = Semaphore(3)
                    val dexJobs = mutableListOf<Job>()
                    DEX_PRE_RULES.forEach { rule ->
                        dexJobs.add(viewModelScope.launch(Dispatchers.IO) {
                            semaphore.withPermit {
                                val findDex = checkLibrary(applicationInfo.sourceDir, rule.dex)
                                if (findDex != null) {
                                    withContext(Dispatchers.Main) {
                                        val oldList = localAppStackResults.value
                                        val newList =
                                            (if (oldList.isNullOrEmpty()) mutableListOf() else oldList).apply {
                                                if (!this.any { it.id == rule.id }) {
                                                    this.add(LocalAppStackResult(rule.id, findDex))
                                                }
                                            }
                                        setLocalAppStackResults(newList.toMutableList())
                                    }
                                }
                            }
                        })
                    }
                    dexJobs.joinAll()
                })
            }
            viewModelScope.launch {
                allJobs.joinAll()
                setLoading(false)
                cleanCache(activity)
            }
        } else {
            setErr(true)
            setAppName(activity.getString(R.string.packageNameIsErr))
            setLoading(false)
            cleanCache(activity)
            return
        }
    }

    private fun getSM3(applicationInfo: ApplicationInfo): String {
        val appSourceDir = applicationInfo.sourceDir
        val messageDigest = MessageDigest.getInstance("SM3")
        val fileInputStream = FileInputStream(appSourceDir)
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) messageDigest.update(
            buffer, 0, bytesRead
        )
        return messageDigest.digest().joinToString("") { "%02X".format(it) }
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

    private fun <T> checkAndSetProperty(
        jobs: MutableList<Job>,
        checkFunction: (T) -> String?,
        setProperty: (String) -> Unit,
        param: T
    ) {
        jobs.add(viewModelScope.launch(Dispatchers.IO) {
            val result = checkFunction(param)
            withContext(Dispatchers.Main) { setProperty(if (result.isNullOrEmpty()) "" else result) }
        })
    }

    private fun checkLibrary(appPath: String, dexList: Array<String>): String? {
        dexList.forEach { dex ->
            val findResult = DexResolver.findPackage(appPath, dex)
            if (findResult.getOrDefault(false) == true) return dex
        }
        return null
    }

    private fun compareActivities(
        components1: Array<ActivityInfo>?, components2: Array<ActivityInfo>?
    ) {
        val set1 = components1?.map { it.name }?.toSet() ?: emptySet()
        val set2 = components2?.map { it.name }?.toSet() ?: emptySet()

        val added = set2 - set1
        val removed = set1 - set2

        val result = StringBuilder()

        if (added.isNotEmpty()) result.append("Added activities: ").append(added.joinToString(", "))
            .append("\n")
        if (removed.isNotEmpty()) result.append("Removed activities: ")
            .append(removed.joinToString(", ")).append("\n")

        setActivityDiff(result.toString())
    }

    private fun compareServices(
        components1: Array<ServiceInfo>?, components2: Array<ServiceInfo>?
    ) {
        val set1 = components1?.map { it.name }?.toSet() ?: emptySet()
        val set2 = components2?.map { it.name }?.toSet() ?: emptySet()

        val added = set2 - set1
        val removed = set1 - set2

        val result = StringBuilder()

        if (added.isNotEmpty()) result.append("Added services: ").append(added.joinToString(", "))
            .append("\n")
        if (removed.isNotEmpty()) result.append("Removed services: ")
            .append(removed.joinToString(", ")).append("\n")

        setServiceDiff(result.toString())
    }

    private fun compareReceivers(
        components1: Array<ActivityInfo>?, components2: Array<ActivityInfo>?
    ) {
        val set1 = components1?.map { it.name }?.toSet() ?: emptySet()
        val set2 = components2?.map { it.name }?.toSet() ?: emptySet()

        val added = set2 - set1
        val removed = set1 - set2

        val result = StringBuilder()

        if (added.isNotEmpty()) result.append("Added receivers: ").append(added.joinToString(", "))
            .append("\n")
        if (removed.isNotEmpty()) result.append("Removed receivers: ")
            .append(removed.joinToString(", ")).append("\n")

        setReceiverDiff(result.toString())
    }

    private fun compareProviders(
        components1: Array<ProviderInfo>?, components2: Array<ProviderInfo>?
    ) {
        val set1 = components1?.map { it.name }?.toSet() ?: emptySet()
        val set2 = components2?.map { it.name }?.toSet() ?: emptySet()

        val added = set2 - set1
        val removed = set1 - set2

        val result = StringBuilder()

        if (added.isNotEmpty()) result.append("Added providers: ").append(added.joinToString(", "))
            .append("\n")
        if (removed.isNotEmpty()) result.append("Removed providers: ")
            .append(removed.joinToString(", ")).append("\n")

        setProviderDiff(result.toString())
    }

    private fun comparePermissions(
        permissions1: Array<PermissionInfo>?, permissions2: Array<PermissionInfo>?
    ) {
        val set1 = permissions1?.map { it.name }?.toSet() ?: emptySet()
        val set2 = permissions2?.map { it.name }?.toSet() ?: emptySet()

        val added = set2 - set1
        val removed = set1 - set2

        val result = StringBuilder()

        if (added.isNotEmpty()) result.append("Added permissions: ")
            .append(added.joinToString(", ")).append("\n")
        if (removed.isNotEmpty()) result.append("Removed permissions: ")
            .append(removed.joinToString(", ")).append("\n")

        setPermissionDiff(result.toString())
    }

    fun cleanCache(activity: Activity) {
        val cacheDir = File(activity.cacheDir, "apkAnalysis")
        if (cacheDir.exists()) cacheDir.deleteRecursively()
    }
}