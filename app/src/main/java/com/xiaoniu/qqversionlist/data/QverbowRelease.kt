package com.xiaoniu.qqversionlist.data

import java.util.Date

data class QverbowRelease(
    val tagName: String,
    val name: String?,
    val body: String?,
    val createdAt: Date,
    val htmlUrl: String,
    val zipballUrl: String?,
    val tarballUrl: String?,
    val isDraft: Boolean,
    val isPrerelease: Boolean,
    val assets: List<QverbowReleaseAssets>?
)

