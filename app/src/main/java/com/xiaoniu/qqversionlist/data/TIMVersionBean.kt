package com.xiaoniu.qqversionlist.data

import kotlinx.serialization.Serializable

@Serializable
data class TIMVersionBean(
    val version: String,
    val datetime: String,
    val fix: List<String>,
    val new: String,

    var jsonString: String = "",
    var displayType: Int = 0, // 0为收起
    var displayInstall: Boolean = false, // false 为不展示
    var isAccessibility: Boolean = false,
    var isQQNTFramework: Boolean = false
)
