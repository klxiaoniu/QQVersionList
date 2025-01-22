package com.xiaoniu.qqversionlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VersionListViewModel : ViewModel() {
    private val _isVersionListLoading = MutableLiveData<Boolean>().apply { value = false }
    val isVersionListLoading: LiveData<Boolean> get() = _isVersionListLoading

    fun setVersionListLoading(isLoading: Boolean) {
        _isVersionListLoading.value = isLoading
    }
}