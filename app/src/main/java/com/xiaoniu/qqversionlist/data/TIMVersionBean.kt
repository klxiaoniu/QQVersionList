/*
    QQ Versions Tool for Android™
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
 * @param version TIM 版本号
 * @param datetime TIM 版本发布日期
 * @param fix TIM 版本特性描述
 * @param new 未知，目前获取到的 `new` 全是空
 * @param jsonString 该 TIM 版本 JSON 字符串详情
 * @param displayType 卡片展示类型，0 为收起态，1 为展开态
 * @param displayInstall 展示是否安装到本机的标签
 * @param isQQNTFramework 该版本是否基于 QQNT 技术架构
 */
@Serializable
data class TIMVersionBean(
    val version: String,
    val datetime: String,
    val fix: String,
    val new: String,

    var jsonString: String = "",
    var displayType: Int = 0, // 0为收起
    var displayInstall: Boolean = false, // false 为不展示
    var isAccessibility: Boolean = false,
    var isQQNTFramework: Boolean = false
)
