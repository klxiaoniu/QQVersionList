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

package com.xiaoniu.qqversionlist.util

import com.xiaoniu.qqversionlist.QverbowApplication.Companion.GITHUB_TOKEN
import com.xiaoniu.qqversionlist.data.QverbowRelease
import com.xiaoniu.qqversionlist.data.QverbowReleaseAssets
import org.kohsuke.github.GitHub

object GitHubRestApiUtil {
    fun getQverbowRelease(version: String? = null): QverbowRelease {
        val token = getGitHubToken()
        val github =
            if (checkGitHubToken()) GitHub.connectUsingOAuth(token) else GitHub.connectAnonymously()
        val repository = github.getRepository("klxiaoniu/QQVersionList")
        val release =
            if (version != null) repository.getReleaseByTagName(version) else repository.latestRelease

        return QverbowRelease(tagName = release.tagName,
            name = release.name,
            body = release.body,
            createdAt = release.createdAt,
            htmlUrl = release.htmlUrl.toString(),
            zipballUrl = release.zipballUrl.toString(),
            tarballUrl = release.tarballUrl.toString(),
            isDraft = release.isDraft,
            isPrerelease = release.isPrerelease,
            assets = release.listAssets().map { asset ->
                QverbowReleaseAssets(
                    name = asset.name,
                    browserDownloadUrl = asset.browserDownloadUrl.toString(),
                    contentType = asset.contentType,
                    size = asset.size
                )
            })
    }


    fun checkGitHubToken(): Boolean {
        val token = getGitHubToken()
        return if (token != null) try {
            val github = GitHub.connectUsingOAuth(token)
            !github.myself.name.isNullOrEmpty()
        } catch (_: Exception) {
            false
        } else false
    }

    private fun getGitHubToken(): String? {
        return KeyStoreUtil.getStringKVwithKeyStore(GITHUB_TOKEN)
    }
}