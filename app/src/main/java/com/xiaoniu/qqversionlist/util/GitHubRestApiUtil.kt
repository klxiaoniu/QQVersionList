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
import org.kohsuke.github.GitHub

object GitHubRestApiUtil {
    fun getQverbowLatestRelease(): MutableMap<String, Any?> {
        val token = getGitHubToken()
        val github =
            if (checkGitHubToken()) GitHub.connectUsingOAuth(token) else GitHub.connectAnonymously()
        val repository = github.getRepository("klxiaoniu/QQVersionList")
        val latestRelease = repository.latestRelease

        return mutableMapOf<String, Any?>().apply {
            this["tagName"] = latestRelease.tagName
            this["name"] = latestRelease.name
            this["body"] = latestRelease.body
            this["createdAt"] = latestRelease.createdAt
            this["htmlUrl"] = latestRelease.htmlUrl.toString()
            this["zipballUrl"] = latestRelease.zipballUrl.toString()
            this["tarballUrl"] = latestRelease.tarballUrl.toString()
            this["isDraft"] = latestRelease.isDraft
            this["isPrerelease"] = latestRelease.isPrerelease
            this["assets"] = latestRelease.listAssets().map { asset ->
                mapOf(
                    "name" to asset.name,
                    "browserDownloadUrl" to asset.browserDownloadUrl.toString(),
                )
            }
        }
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