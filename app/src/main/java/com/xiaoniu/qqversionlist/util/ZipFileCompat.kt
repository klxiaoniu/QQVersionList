package com.xiaoniu.qqversionlist.util

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile

class ZipFileCompat(file: File) : AutoCloseable {
    private var javaZipFile: ZipFile? = null
    private var apacheZipFile: ApacheZipFile? = null

    init {
        runCatching {
            javaZipFile = ZipFile(file)
        }.onFailure {
            apacheZipFile = ApacheZipFile.Builder().setFile(file).get()
        }
    }

    fun getEntry(name: String): ZipEntry? {
        return javaZipFile?.getEntry(name) ?: apacheZipFile?.getEntry(name)
    }

    fun getInputStream(entry: ZipEntry): InputStream {
        return javaZipFile?.getInputStream(entry) ?: run {
            if (entry is ZipArchiveEntry) {
                apacheZipFile?.getInputStream(entry) ?: throw Exception("未找到 InputStream。")
            } else {
                val archiveEntry = apacheZipFile?.getEntry(entry.name)
                archiveEntry?.let { apacheZipFile?.getInputStream(it) }
                    ?: throw Exception("未找到 InputStream。")
            }
        }
    }

    override fun close() {
        javaZipFile?.close()
        apacheZipFile?.close()
    }
}