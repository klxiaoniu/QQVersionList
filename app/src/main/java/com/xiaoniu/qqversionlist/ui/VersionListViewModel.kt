package com.xiaoniu.qqversionlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VersionListViewModel : ViewModel() {
    private val _isVersionListLoading = MutableLiveData<Boolean>().apply { value = false }
    val isVersionListLoading: LiveData<Boolean> get() = _isVersionListLoading

    private val _isTokenTesting = MutableLiveData<Boolean>().apply { value = false }
    val isTokenTesting: LiveData<Boolean> get() = _isTokenTesting

    fun setVersionListLoading(isLoading: Boolean) {
        _isVersionListLoading.value = isLoading
    }

    fun setTokenTesting(isTesting: Boolean) {
        _isTokenTesting.value = isTesting
    }
}