package com.xiaoniu.qqversionlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    private val _isVersionListLoading = MutableLiveData<Boolean>().apply { value = false }
    val isVersionListLoading: LiveData<Boolean> get() = _isVersionListLoading

    private val _isTokenTesting = MutableLiveData<Boolean>().apply { value = false }
    val isTokenTesting: LiveData<Boolean> get() = _isTokenTesting

    private val _isUpdateBackLLMWorking = MutableLiveData<Boolean>().apply { value = false }
    val isUpdateBackLLMWorking: LiveData<Boolean> get() = _isUpdateBackLLMWorking

    private val _updateBackLLMGenText = MutableLiveData<String>().apply { value = "" }
    val updateBackLLMGenText: LiveData<String> get() = _updateBackLLMGenText

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
}