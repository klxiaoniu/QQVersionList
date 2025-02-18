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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.data.WeixinVersionBean

class MainActivityViewModel : ViewModel() {
    private val _isVersionListLoading = MutableLiveData<Boolean>().apply { value = false }
    val isVersionListLoading: LiveData<Boolean> get() = _isVersionListLoading

    private val _isTokenTesting = MutableLiveData<Boolean>().apply { value = false }
    val isTokenTesting: LiveData<Boolean> get() = _isTokenTesting

    private val _isUpdateBackLLMWorking = MutableLiveData<Boolean>().apply { value = false }
    val isUpdateBackLLMWorking: LiveData<Boolean> get() = _isUpdateBackLLMWorking

    private val _updateBackLLMGenText = MutableLiveData<String>().apply { value = "" }
    val updateBackLLMGenText: LiveData<String> get() = _updateBackLLMGenText

    private val _qqVersion = MutableLiveData<List<QQVersionBean>>().apply { value = emptyList() }
    val qqVersion: LiveData<List<QQVersionBean>> get() = _qqVersion

    private val _timVersion = MutableLiveData<List<TIMVersionBean>>().apply { value = emptyList() }
    val timVersion: LiveData<List<TIMVersionBean>> get() = _timVersion

    private val _weixinVersion = MutableLiveData<List<WeixinVersionBean>>().apply { value = emptyList() }
    val weixinVersion: LiveData<List<WeixinVersionBean>> get() = _weixinVersion

    fun setVersionListLoading(isLoading: Boolean) {
        _isVersionListLoading.value = isLoading
    }

    fun setTokenTesting(isTesting: Boolean) {
        _isTokenTesting.value = isTesting
    }

    fun setUpdateBackLLMWorking(isWorking: Boolean) {
        _isUpdateBackLLMWorking.value = isWorking
    }

    fun setUpdateBackLLMGenText(text: String) {
        _updateBackLLMGenText.value = text
    }

    fun setQQVersion(qqVersion: List<QQVersionBean>) {
        _qqVersion.value = qqVersion
    }

    fun setTIMVersion(timVersion: List<TIMVersionBean>) {
        _timVersion.value = timVersion
    }

    fun setWeixinVersion(weixinVersion: List<WeixinVersionBean>) {
        _weixinVersion.value = weixinVersion
    }
}