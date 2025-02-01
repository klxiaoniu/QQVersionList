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

package com.xiaoniu.qqversionlist.util

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Base64
import com.xiaoniu.qqversionlist.util.DataStoreUtil.getStringKV
import com.xiaoniu.qqversionlist.util.DataStoreUtil.putStringKVAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec

object KeyStoreUtil {
    private const val KEY_ALIAS_USERS_TOKEN = "Qverbow_Users_Token"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val KEY_ALGORITHM = "AES"
    private const val GCM_TAG_LENGTH = 128
    private const val ANDROID_KEY_STORE_PROVIDER = "AndroidKeyStore"

    /**
     * 使用密钥存储加密数据到键值存储中
     * 当数据为空字符串时，会删除之前存储的加密信息
     *
     * @param key 用于存储的键名前缀
     * @param data 要存储的字符串数据
     */
    fun putStringKVwithKeyStore(key: String, data: String) {
        if (data == "") {
            DataStoreUtil.apply {
                deleteKV("${key}_iv")
                deleteKV("${key}_cipher")
            }
            return
        }

        val encryptedData = encryptData(data) ?: return
        val iv = encryptedData.first
        val cipherText = encryptedData.second

        putByteArrayKV("${key}_iv", iv)
        putByteArrayKV("${key}_cipher", cipherText)
    }

    /**
     * 异步使用密钥存储加密数据到键值存储中
     * 当数据为空字符串时，会删除之前存储的加密信息
     *
     * @param key 用于存储的键名前缀
     * @param data 要存储的字符串数据
     */
    fun putStringKVwithKeyStoreAsync(key: String, data: String) {
        if (data == "") {
            DataStoreUtil.apply {
                deleteKVAsync("${key}_iv")
                deleteKVAsync("${key}_cipher")
            }
            return
        }

        val encryptedData = encryptData(data) ?: return
        val iv = encryptedData.first
        val cipherText = encryptedData.second

        putByteArrayKVAsync("${key}_iv", iv)
        putByteArrayKVAsync("${key}_cipher", cipherText)
    }

    /**
     * 从键值存储中获取使用密钥存储加密的字符串数据
     * 如果找不到对应的加密信息，则返回 null
     *
     * @param key 用于存储的键名前缀
     * @return 解密后的字符串数据，或者 null
     */
    fun getStringKVwithKeyStore(key: String): String? {
        val iv = getByteArrayKV("${key}_iv", ByteArray(0))
        val cipherText = getByteArrayKV("${key}_cipher", ByteArray(0))

        return if (iv.isEmpty() || cipherText.isEmpty()) null else decryptData(iv, cipherText)
    }

    /**
     * 异步从键值存储中获取使用密钥存储加密的字符串数据
     * 如果找不到对应的加密信息，则返回 null
     *
     * @param key 用于存储的键名前缀
     * @return 解密后的字符串数据的 Deferred 对象，或者 null
     */
    fun getStringKVwithKeyStoreAsync(key: String): Deferred<String?> {
        val iv = getByteArrayKVAsync("${key}_iv", ByteArray(0))
        val cipherText = getByteArrayKVAsync("${key}_cipher", ByteArray(0))

        return CoroutineScope(Dispatchers.IO).async {
            val iv = iv.await()
            val cipherText = cipherText.await()
            if (iv.isEmpty() || cipherText.isEmpty()) return@async null else decryptData(
                iv, cipherText
            )
        }
    }

    private fun generateKey(keyStore: KeyStore) {
        if (!keyStore.containsAlias(KEY_ALIAS_USERS_TOKEN)) {
            val keyGenerator =
                javax.crypto.KeyGenerator.getInstance(KEY_ALGORITHM, ANDROID_KEY_STORE_PROVIDER)
            val builder = KeyGenParameterSpec.Builder(
                KEY_ALIAS_USERS_TOKEN,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)

            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
    }

    private fun encryptData(data: String): Pair<ByteArray, ByteArray>? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER)
        keyStore.load(null)
        generateKey(keyStore)
        val secretKeyEntry =
            keyStore.getEntry(KEY_ALIAS_USERS_TOKEN, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        val iv = cipher.iv

        return Pair(iv, encryptedData)
    }


    private fun decryptData(iv: ByteArray, encryptedData: ByteArray): String? {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER)
        keyStore.load(null)
        val secretKeyEntry =
            keyStore.getEntry(KEY_ALIAS_USERS_TOKEN, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey

        val cipher = Cipher.getInstance(AES_MODE)
        val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        return String(cipher.doFinal(encryptedData), Charsets.UTF_8)
    }


    private fun putByteArrayKV(key: String, value: ByteArray) {
        val base64Encoded = Base64.encodeToString(value, Base64.DEFAULT)
        DataStoreUtil.putStringKV(key, base64Encoded)
    }

    private fun getByteArrayKV(key: String, defValue: ByteArray): ByteArray {
        val base64Encoded = getStringKV(key, "")
        return if (base64Encoded.isEmpty()) defValue else Base64.decode(
            base64Encoded, Base64.DEFAULT
        )
    }

    private fun putByteArrayKVAsync(key: String, value: ByteArray) {
        val base64Encoded = Base64.encodeToString(value, Base64.DEFAULT)
        putStringKVAsync(key, base64Encoded)
    }

    private fun getByteArrayKVAsync(key: String, defValue: ByteArray): Deferred<ByteArray> {
        return CoroutineScope(Dispatchers.IO).async {
            val base64Encoded = getStringKV(key, "")
            if (base64Encoded.isEmpty()) defValue else Base64.decode(base64Encoded, Base64.DEFAULT)
        }
    }

    /**
     * 检查硬件安全级别。
     * 此函数通过与 Android Keystore 系统的交互，评估当前设备上的密钥是否存储在安全的硬件环境中。
     *
     * @return Int 返回一个整数，表示硬件的安全级别：
     *          2：表示密钥存储在 StrongBox 安全元件中
     *          1：表示密钥存储在可信执行环境（TEE）中
     *          -1：表示密钥存储在未知的安全硬件中
     *          0：表示密钥存储仅通过软件实现
     *          其他值：表示未知或不支持的环境
     */
    fun checkHardwareSecurity(): Int {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE_PROVIDER)
        keyStore.load(null)
        generateKey(keyStore)
        val secretKeyEntry =
            keyStore.getEntry(KEY_ALIAS_USERS_TOKEN, null) as KeyStore.SecretKeyEntry
        val secretKey = secretKeyEntry.secretKey
        val factory = SecretKeyFactory.getInstance(secretKey.algorithm, ANDROID_KEY_STORE_PROVIDER)
        var keyInfo = factory.getKeySpec(secretKey, KeyInfo::class.java) as KeyInfo
        return if (SDK_INT >= Build.VERSION_CODES.S) keyInfo.securityLevel else (if (keyInfo.isInsideSecureHardware == true) -1 else 0)
    }
}

