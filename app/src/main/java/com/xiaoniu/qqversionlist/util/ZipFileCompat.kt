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

package com.xiaoniu.qqversionlist.util

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile

class ZipFileCompat(file: File) : AutoCloseable {
    private var javaZipFile: ZipFile? = null
    private var apacheCommonsZipFile: ApacheZipFile? = null

    init {
        runCatching {
            javaZipFile = ZipFile(file)
        }.onFailure {
            apacheCommonsZipFile = ApacheZipFile.Builder().setFile(file).get()
        }
    }

    fun getEntry(name: String): ZipEntry? {
        return javaZipFile?.getEntry(name) ?: apacheCommonsZipFile?.getEntry(name)
    }

    fun getInputStream(entry: ZipEntry): InputStream {
        return javaZipFile?.getInputStream(entry) ?: run {
            if (entry is ZipArchiveEntry) {
                apacheCommonsZipFile?.getInputStream(entry)
                    ?: throw Exception("未找到 InputStream。")
            } else {
                val archiveEntry = apacheCommonsZipFile?.getEntry(entry.name)
                archiveEntry?.let { apacheCommonsZipFile?.getInputStream(it) }
                    ?: throw Exception("未找到 InputStream。")
            }
        }
    }

    override fun close() {
        javaZipFile?.close()
        apacheCommonsZipFile?.close()
    }
}