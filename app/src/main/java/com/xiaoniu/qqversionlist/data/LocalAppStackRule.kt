package com.xiaoniu.qqversionlist.data

data class LocalAppStackRule(
    val id: String,
    val dex: Array<String>,
    val desc: String ?= null
)
