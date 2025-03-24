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

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.1.20"
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.aboutlibraries)
}

private fun gitCommitHash(project: Project): String {
    return project.providers.exec {
        commandLine("git rev-parse --verify --short HEAD".split(" "))
    }.standardOutput.asText.get().trim()
}

private fun gitCommitCount(project: Project): Int {
    return project.providers.exec {
        commandLine("git rev-list HEAD --count".split(" "))
    }.standardOutput.asText.get().trim().toInt()
}

val gitCommitCount = gitCommitCount(project)
val gitCommitHash = gitCommitHash(project)

android {
    namespace = "com.xiaoniu.qqversionlist"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.xiaoniu.qqversionlist"
        minSdk = 24
        targetSdk = 36
        versionCode = gitCommitCount
        versionName = "1.5.2-$gitCommitHash"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
            }
        }
    }

    signingConfigs {
        System.getenv("KEYSTORE_PATH")?.let {
            create("release") {
                storeFile = file(it)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.findByName("debug")
            signingConfig?.enableV2Signing = true
            signingConfig?.enableV3Signing = true
            signingConfig?.enableV4Signing = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }
}

aboutLibraries {
    mapLicensesToSpdx = false
}

dependencies {
    implementation(libs.aboutlibraries.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.browser)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.window.sizeclass)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.commons.compress)
    implementation(libs.commons.io)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(platform(libs.firebase.bom))
    implementation(libs.github.api)
    implementation(libs.gson)
    implementation(libs.jsoup)
    implementation(libs.kona.crypto)
    implementation(libs.kona.provider)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.library)
    implementation(libs.material)
    implementation(libs.maven.artifact)
    implementation(libs.oapi.java.sdk)
    implementation(libs.okhttp)
    implementation(libs.pangu)
    implementation(libs.paris)
    implementation(libs.smali.dexlib2)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
