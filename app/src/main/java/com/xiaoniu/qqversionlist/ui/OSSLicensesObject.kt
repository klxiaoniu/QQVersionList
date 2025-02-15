package com.xiaoniu.qqversionlist.ui

import com.mikepenz.aboutlibraries.entity.Library

object OSSLicensesObject {
    private var currentLibrary: Library? = null

    fun setCurrentLibrary(library: Library) {
        currentLibrary = library
    }

    fun getCurrentLibrary(): Library? {
        return currentLibrary
    }
}