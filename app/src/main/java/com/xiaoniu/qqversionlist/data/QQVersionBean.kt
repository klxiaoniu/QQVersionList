// SPDX-License-Identifier: AGPL-3.0-or-later

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

import kotlinx.serialization.Serializable

/**
 * @param versions QQ 版本代码
 * @param versionNumber QQ 版本号
 * @param size QQ 版本额定大小
 * @param featureTitle QQ 版本特性标题
 * @param imgs QQ 应用宣传图片列表
 * @param summary QQ 版本特性描述列表
 * @param jsonString 该 QQ 版本 JSON 字符串详情
 * @param displayType 卡片展示类型，0 为收起态，1 为展开态
 * @param displayInstall 展示是否安装到本机的标签
 * @param isQQNTFramework 该版本是否基于 QQNT 技术架构
 * @param isUnrealEngine 该版本是否存在 Unreal Engine Lib
 * @param isKuiklyInside 该版本是否存在 TDS 腾讯端服务 Kukily 开发框架
 */
@Serializable
data class QQVersionBean(
    val versions: String,
    val versionNumber: String,
    val size: String,
    val featureTitle: String,
    val imgs: List<String>,
    val summary: List<String>,

    var jsonString: String = "",
    var displayType: Int = 0, // 0为收起
    var displayInstall: Boolean = false, // false 为不展示
    var isAccessibility: Boolean = false,
    var isQQNTFramework: Boolean = false,
    var isUnrealEngine: Boolean = false,
    var isKuiklyInside: Boolean = false
)
