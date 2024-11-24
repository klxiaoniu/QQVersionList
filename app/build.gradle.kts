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
    id("org.jetbrains.kotlin.android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    kotlin("plugin.serialization") version "2.0.21"
    id("com.google.android.gms.oss-licenses-plugin")
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
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xiaoniu.qqversionlist"
        minSdk = 24
        targetSdk = 35
        versionCode = gitCommitCount
        versionName = "1.4.4-$gitCommitHash"
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
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.activity.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.paris)
    implementation(libs.maven.artifact)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.library)
    implementation(libs.androidx.browser)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.commons.compress)
    implementation(libs.kona.crypto)
    implementation(libs.kona.provider)
}
