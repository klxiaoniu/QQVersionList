package com.xiaoniu.qqversionlist.util

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.Strictness
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

object TencentShiplyUtil {
    private val gson = GsonBuilder().setStrictness(Strictness.LENIENT).create()
    private const val AES_METHOD = "AES/CTR/NoPadding"
    private const val RSA_METHOD = "RSA/ECB/PKCS1Padding"

    fun generateJsonString(appVersion: String, uin: String): String {
        val timestamp = System.currentTimeMillis() / 1000L
        val systemID = "10016"
        val appID = "4cd6974be1"
        val sign =
            md5("10016$4cd6974be1$4$$timestamp$$uin${"rdelivery0ccc46ca-154c-4c6b-8b0b-4d8537ffcbcc"}")
        val pullType = 4
        val target = 1
        val properties = mapOf(
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
        )
        val isDebugPackage = false
        val customProperties = mapOf("appid" to "537230561")
        val taskChecksum = "0"
        val context =
            "H4sIAAAAAAAA/+Li5ni5T1WIVaBT1INRS8HS0MwyMdnCwMzQMCklxdQ81cTC1MzIIDnV0DIxydLYGAAAAP//AQAA//+OoFcLLwAAAA=="

        val data = mapOf(
            "systemID" to systemID,
            "appID" to appID,
            "sign" to sign,
            "timestamp" to timestamp,
            "pullType" to pullType,
            "target" to target,
            "pullParams" to mapOf(
                "properties" to properties,
                "isDebugPackage" to isDebugPackage,
                "customProperties" to customProperties
            ),
            "taskChecksum" to taskChecksum,
            "context" to context
        )

        return data.toString()
    }

    fun md5(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

    fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun getCipherText(jsonString: String): String? {
        return try {
            val jsonObject = Gson().fromJson(jsonString, JsonObject::class.java)
            val rspList = jsonObject.getAsJsonArray("rsp_list")
            for (i in 0 until rspList.size()) {
                val item = rspList.get(i).asJsonObject
                if (item.has("cipher_text")) {
                    return item.get("cipher_text").asString
                }
            }
            null
        } catch (e: JsonParseException) {
            e.printStackTrace()
            null
        }
    }

    fun generateAESKey(): SecretKey? {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(128)
        return keyGen.generateKey()
    }

    fun aesEncrypt(data: String, key: SecretKey): String? {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    fun aesDecrypt(encryptedData: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decryptedData = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        return String(decryptedData)
    }

    fun base64ToRsaPublicKey(base64String: String): PublicKey? {
        val encodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(encodedBytes)
        return try {
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun rsaEncrypt(data: String, publicKey: PublicKey): String? {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedData = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    fun postJsonWithOkHttp(url: String, data: Any): Response {
        val client = OkHttpClient()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, gson.toJson(data))
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept-Encoding", "gzip")
            .build()

        return client.newCall(request).execute().apply {
            if (!isSuccessful) throw IOException("Unexpected code $code for url: $url")
        }
    }

}