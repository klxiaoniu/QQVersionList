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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.xiaoniu.qqversionlist.QVTApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DataStoreUtil {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "data",
        produceMigrations = { context ->
            listOf(
                SharedPreferencesMigration(context, "data")
            )
        })

    private val dataStore: DataStore<Preferences> by lazy {
        QVTApplication.instance.dataStore
    }

    fun getIntKV(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[intPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putIntKV(key: String, value: Int) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(key)] = value
            }
        }
    }

    fun getStringKV(key: String, defValue: String): String {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[stringPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putStringKV(key: String, value: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    fun getBooleanKV(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putBooleanKV(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }

    fun deleteKV(key: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
                preferences.remove(intPreferencesKey(key))
                preferences.remove(booleanPreferencesKey(key))
                preferences.remove(floatPreferencesKey(key))
                preferences.remove(longPreferencesKey(key))
                preferences.remove(doublePreferencesKey(key))
            }
        }
    }

    /**
     * 批量写入 DataStore 持久化存储数据
     * @param dataStoreList List<Map<String, Any>>
     *
     *  key: String
     *
     *  value: Any
     *
     *  type: String
     */
    fun batchPutKV(dataStoreList: List<Map<String, Any>>) {
        runBlocking {
            dataStore.edit { preferences ->
                dataStoreList.forEach { dataMap ->
                    val key = dataMap["key"] as? String ?: return@forEach
                    val value = dataMap["value"]
                    when (val type = dataMap["type"] as? String) {
                        "Int", "int" -> preferences[intPreferencesKey(key)] =
                            value as? Int ?: return@forEach

                        "Long", "long" -> preferences[longPreferencesKey(key)] =
                            value as? Long ?: return@forEach

                        "Float", "float" -> preferences[floatPreferencesKey(key)] =
                            value as? Float ?: return@forEach

                        "Double", "double" -> preferences[doublePreferencesKey(key)] =
                            value as? Double ?: return@forEach

                        "String", "string" -> preferences[stringPreferencesKey(key)] =
                            value as? String ?: return@forEach

                        "Boolean", "boolean" -> preferences[booleanPreferencesKey(key)] =
                            value as? Boolean ?: return@forEach

                        else -> throw IllegalArgumentException("DataStore 不支持的类型: $type")
                    }
                }
            }
        }
    }

    fun getIntKVAsync(key: String, defValue: Int): Deferred<Int> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[intPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putIntKVAsync(key: String, value: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(key)] = value
            }
        }
    }

    fun getStringKVAsync(key: String, defValue: String): Deferred<String> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[stringPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putStringKVAsync(key: String, value: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    fun getBooleanKVAsync(key: String, defValue: Boolean): Deferred<Boolean> {
        return CoroutineScope(Dispatchers.IO).async {
            dataStore.data.firstOrNull()?.let { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defValue
            } ?: defValue
        }
    }

    fun putBooleanKVAsync(key: String, value: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }

    fun deleteKVAsync(key: String) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
                preferences.remove(intPreferencesKey(key))
                preferences.remove(booleanPreferencesKey(key))
                preferences.remove(floatPreferencesKey(key))
                preferences.remove(longPreferencesKey(key))
                preferences.remove(doublePreferencesKey(key))
            }
        }
    }

    /**
     * 异步批量写入 DataStore 持久化存储数据
     * @param dataStoreList List<Map<String, Any>>
     *
     *  key: String
     *
     *  value: Any
     *
     *  type: String
     */
    fun batchPutKVAsync(dataStoreList: List<Map<String, Any>>) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { preferences ->
                dataStoreList.forEach { dataMap ->
                    val key = dataMap["key"] as? String ?: return@forEach
                    val value = dataMap["value"]
                    when (val type = dataMap["type"] as? String) {
                        "Int", "int" -> preferences[intPreferencesKey(key)] =
                            value as? Int ?: return@forEach

                        "Long", "long" -> preferences[longPreferencesKey(key)] =
                            value as? Long ?: return@forEach

                        "Float", "float" -> preferences[floatPreferencesKey(key)] =
                            value as? Float ?: return@forEach

                        "Double", "double" -> preferences[doublePreferencesKey(key)] =
                            value as? Double ?: return@forEach

                        "String", "string" -> preferences[stringPreferencesKey(key)] =
                            value as? String ?: return@forEach

                        "Boolean", "boolean" -> preferences[booleanPreferencesKey(key)] =
                            value as? Boolean ?: return@forEach

                        else -> throw IllegalArgumentException("DataStore 不支持的类型: $type")
                    }
                }
            }
        }
    }
}

