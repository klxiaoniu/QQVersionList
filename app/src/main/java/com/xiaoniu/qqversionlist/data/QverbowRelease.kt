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

