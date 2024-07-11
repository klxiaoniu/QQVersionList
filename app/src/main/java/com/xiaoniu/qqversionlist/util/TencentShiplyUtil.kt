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

object TencentShiplyUtil {
    private val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()

    fun generateJsonString(appVersion: String, uin: String, appid: String): String {
        val timestamp = System.currentTimeMillis() / 1000L
        val data = mapOf(
            "systemID" to "10016",
            "appID" to "4cd6974be1",
            "sign" to md5("10016$4cd6974be1$4$$$timestamp$$uin$${"rdelivery0ccc46ca-154c-4c6b-8b0b-4d8537ffcbcc"}"),
            "timestamp" to timestamp,
            "pullType" to 4,
            "target" to 1,
            "pullParams" to mapOf(
                "properties" to mapOf(
                    "platform" to 2,
                    "language" to "zh",
                    "sdkVersion" to "1.3.35-RC03",
                    "guid" to uin,
                    "appVersion" to appVersion,
                    "osVersion" to "34",
                    "is64Bit" to true,
                    "bundleId" to "com.tencent.mobileqq",
                    "uniqueId" to generateRandomUUID(),
                    "model" to "2304FPN6DC"
                ),
                "isDebugPackage" to false,
                "customProperties" to mapOf("appid" to appid)
            ),
            "taskChecksum" to "0",
            "context" to "H4sIAAAAAAAA/+Li5ni5T1WIVaBT1INRS8HS0MwyMdnCwMzQMCklxdQ81cTC1MzIIDnV0DIxydLYGAAAAP//AQAA//+OoFcLLwAAAA=="
        )

        return gson.toJson(data)
    }

    private fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    private fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }

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
            if (cipherText != null && cipherText.isJsonPrimitive) {
                return cipherText.asString
            }
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

    fun postJsonWithOkHttp(url: String, data: Any): String {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val body = gson.toJson(data)
        val request = Request.Builder()
            .url(url)
            .post(body.toRequestBody(mediaType!!))
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Encoding", "gzip")
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