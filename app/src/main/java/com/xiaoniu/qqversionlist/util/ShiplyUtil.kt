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

package com.xiaoniu.qqversionlist.util

import android.util.Base64
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.Strictness
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object ShiplyUtil {

    /**
     * @param appVersion QQ 版本号
     * @param uin QQ 号
     * @param appid QQ 版本 ID，如 `537230561`
     * @param osVersion Android 版本（整数表示）
     * @param model 设备型号
     * @param sdkVersion Shiply SDK 版本
     * @param language 语言
     * @param targetApp 目标应用，默认为“QQ”，可选“TIM”
     * @return 生成的 JSON 字符串
     **/
    fun generateJsonString(
        appVersion: String,
        uin: String,
        appid: String,
        osVersion: String,
        model: String,
        sdkVersion: String,
        language: String,
        targetApp: String = "QQ"
    ): String {
        val timestamp = System.currentTimeMillis() / 1000L
        val appID = if (targetApp == "QQ") "4cd6974be1" else "ad6b501b0e"
        val signID =
            if (targetApp == "QQ") "0ccc46ca-154c-4c6b-8b0b-4d8537ffcbcc" else "33641818-aee7-445a-82d4-b7d0bce3a85a"
        val bundleId = if (targetApp == "QQ") "com.tencent.mobileqq" else "com.tencent.tim"
        val data = mapOf(
            "systemID" to "10016",
            "appID" to appID,
            "sign" to BigInteger(
                1,
                MessageDigest.getInstance("MD5")
                    .digest("10016$$appID$4$$$timestamp$$uin${"$"}rdelivery$signID".toByteArray())
            ).toString(16).padStart(32, '0'),
            "timestamp" to timestamp,
            "pullType" to 4,
            "target" to 1,
            "pullParams" to mapOf(
                "properties" to mapOf(
                    "platform" to 2,
                    "language" to language, // Locale.getDefault().language.toString()
                    "sdkVersion" to sdkVersion, // "1.3.36-RC01"
                    "guid" to uin,
                    "appVersion" to appVersion,
                    "osVersion" to osVersion, // Build.VERSION.SDK_INT.toString()
                    "is64Bit" to true,
                    "bundleId" to bundleId,
                    "uniqueId" to UUID.randomUUID().toString(),
                    "model" to model // Build.MODEL.toString()
                ),
                "isDebugPackage" to false,
                "customProperties" to mapOf("appid" to appid) // "537230561"
            ),
            "taskChecksum" to "0",
            "context" to "H4sIAAAAAAAA/+Li5ni5T1WIVaBT1INRS8HS0MwyMdnCwMzQMCklxdQ81cTC1MzIIDnV0DIxydLYGAAAAP//AQAA//+OoFcLLwAAAA=="
        )
        return GsonBuilder().setStrictness(Strictness.LENIENT).create().toJson(data)
    }

    /**
     * 从给定的 JSON 字符串中提取加密文本。
     *
     * 该函数旨在处理一个特定格式的 JSON 字符串，该字符串预期包含嵌套结构，
     * 最终目标是提取出“cipher_text”字段的值。如果输入字符串不符合预期格式，
     * 或者“cipher_text”字段不存在，或者它不是一个原始的 JSON 类型，则函数返回 null。
     *
     * @param jsonString 期望格式化的 JSON 字符串。
     * @return 提取的加密文本字符串，如果提取失败则返回 null。
     */
    fun getCipherText(jsonString: String): String? {
        val json = JsonParser.parseString(jsonString)
        if (!json.isJsonObject) return null

        val jsonObject = json.asJsonObject
        val rspList = jsonObject.get("rsp_list")
        if (rspList == null || !rspList.isJsonObject) return null

        val rspListObj = rspList.asJsonObject
        val firstEntry = rspListObj.entrySet().firstOrNull()
        if (firstEntry != null) {
            val response = firstEntry.value.asJsonObject
            val cipherText = response.get("cipher_text")
            if (cipherText != null && cipherText.isJsonPrimitive) return cipherText.asString
        }
        return null
    }

    fun generateAESKey(): ByteArray {
        val secureRandom = SecureRandom()
        val key = ByteArray(16)
        secureRandom.nextBytes(key)
        return key
    }

    fun aesEncrypt(data: String, key: ByteArray): ByteArray? {
        val method = "AES/CTR/NoPadding"
        val iv = ByteArray(16) { 0 }
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        try {
            val dataByteArray = data.toByteArray(StandardCharsets.UTF_8)
            val cipher = Cipher.getInstance(method)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return cipher.doFinal(dataByteArray)
        } catch (e: Exception) {
            return null
        }
    }

    fun aesDecrypt(data: ByteArray, key: ByteArray): ByteArray? {
        val method = "AES/CTR/NoPadding"
        val iv = ByteArray(16) { 0 }
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)

        try {
            val cipher = Cipher.getInstance(method)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decryptedData = cipher.doFinal(data)
            return decryptedData
        } catch (e: Exception) {
            return null
        }
    }

    fun base64ToRsaPublicKey(base64String: String): PublicKey? {
        val decodedBytes = Base64.decode(base64String, Base64.NO_WRAP)
        val spec = X509EncodedKeySpec(decodedBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec)
    }

    fun rsaEncrypt(data: ByteArray, publicKey: PublicKey): ByteArray? {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }

    /**
     * @param url 请求的 URL 地址
     * @param data 请求体中的 JSON 数据
     * @return 服务器的响应内容
     * @throws IOException 如果网络请求失败或响应体为空时抛出
     */
    fun postJsonWithOkHttp(url: String, data: Any): String {
        val client = OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }).build()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = GsonBuilder().setStrictness(Strictness.LENIENT).create().toJson(data)
        val request = Request.Builder().url(url).post(body.toRequestBody(mediaType!!))
            .addHeader("Content-Type", "application/json").addHeader("Accept-Encoding", "gzip")
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code ${response.code} for url: $url")

            response.body?.let { responseBody ->
                val input: InputStream = when (response.header("Content-Encoding")) {
                    "gzip" -> GZIPInputStream(responseBody.byteStream())
                    else -> responseBody.byteStream()
                }

                InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                    reader.readText()
                }
            } ?: throw IOException("Response body is null")
        }
    }
}