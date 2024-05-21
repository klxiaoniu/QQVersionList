/*
    QQ Version Tool for Android™
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

data class QQVersionBean(
    val versions: String,
    val versionNumber: String,
    val size: String,
    val featureTitle: String,
    val imgs: List<String>,
    val summary: List<String>,

    var jsonString: String,
    var displayType: Int = 0, // 0为收起
    var displayInstall: Boolean = false, // false 为不展示
    var isShowProgressSize: Boolean = false
)
