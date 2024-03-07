package com.xiaoniu.qqversionlist.data

data class QQVersionBean(
    val versions: String,
    val versionNumber: String,
    val size: String,
    val featureTitle: String,
    val imgs: List<String>,
    val summary: List<String>,

    var jsonString: String,
    var displayType: Int = 0 // 0为收起
)