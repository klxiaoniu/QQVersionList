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

data class LocalAppStackRule(
    val id: String,
    val dex: Array<String>,
    val type: String,
    val url: String? = null,
    val desc: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalAppStackRule

        if (id != other.id) return false
        if (!dex.contentEquals(other.dex)) return false
        if (type != other.type) return false
        if (url != other.url) return false
        if (desc != other.desc) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + dex.contentHashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (desc?.hashCode() ?: 0)
        return result
    }
}
