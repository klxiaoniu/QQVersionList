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

package com.xiaoniu.qqversionlist.ui

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.entity.Library
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.ui.ui.theme.QQVersionListTheme
import com.xiaoniu.qqversionlist.util.InfoUtil.openUrlWithChromeCustomTab
import com.xiaoniu.qqversionlist.util.OSSLicensesObject
import java.net.URL

class OSSLicensesActivity : ComponentActivity() {
    private var uniqueIdExtra: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false

        uniqueIdExtra = intent.getStringExtra("library")

        setContent { QQVersionListTheme { OSSLicenses() } }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun OSSLicenses() {
        val libs = OSSLicensesObject.libs
        val lib: Library? =
            if (uniqueIdExtra != null) libs?.libraries?.find { it.uniqueId == uniqueIdExtra } else null

        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(
                        if (lib != null) lib.name.toString() else stringResource(id = R.string.openSourceLicenseTitle)
                    )
                }, navigationIcon = {
                    IconButton(onClick = { finish() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_left_line),
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }, scrollBehavior = scrollBehavior
                )
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .consumeWindowInsets(innerPadding)
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (lib != null) LibraryDetails(lib, innerPadding)
            }
        }
    }

    @Composable
    private fun LibraryDetails(lib: Library, innerPadding: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(11.dp, 7.dp, 11.dp, 11.dp)
        ) {
            LibraryWebsite(lib)
            LibraryLicenses(lib)
            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
        }
    }

    @Composable
    private fun LibraryWebsite(lib: Library) {
        val url = if (lib.website != null) lib.website.toString() else ""
        val repoUrl = if (url.isNotEmpty()) URL(url) else null
        val isGitHub = if (repoUrl != null) repoUrl.host == "github.com" else false
        val isGitLab = if (repoUrl != null) repoUrl.host == "gitlab.com" else false
        if (url.isNotEmpty()) Card(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer),
            onClick = {
                if (url.isNotEmpty()) openUrlWithChromeCustomTab(url)
            }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = when {
                        isGitHub -> painterResource(id = R.drawable.github_line)
                        isGitLab -> painterResource(id = R.drawable.gitlab_line)
                        else -> painterResource(id = R.drawable.git_repository_line)
                    },
                    contentDescription = stringResource(id = R.string.aboutOpenSourceRepo),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = url, style = TextStyle(
                        color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.55.sp
                    )
                )
            }
        }

    }

    @Composable
    private fun LibraryLicenses(lib: Library) {
        val libs = OSSLicensesObject.libs
        if (!lib.licenses.isEmpty()) libs?.licenses?.forEach { allLicense ->
            lib.licenses.forEach { libLicense ->
                if (allLicense.hash == libLicense.hash) {
                    val licenseContent = allLicense.licenseContent
                    val licenseUrl = allLicense.url
                    if (!licenseContent.isNullOrEmpty()) SelectionContainer {
                        Text(
                            modifier = Modifier.padding(5.dp, 8.dp),
                            text = licenseContent.toString(),
                            style = typography.bodyMedium
                        )
                    } else if (!licenseUrl.isNullOrEmpty()) ClickableCard(url = licenseUrl.toString(),
                        icon = painterResource(id = R.drawable.info_card_line),
                        contentDescription = stringResource(id = R.string.openSourceLicenseTitle),
                        onClick = { openUrlWithChromeCustomTab(licenseUrl.toString()) })
                }
            }
        }
    }

    @Composable
    private fun ClickableCard(
        url: String, icon: Painter, contentDescription: String, onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer),
            onClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = url, style = TextStyle(
                        color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 13.55.sp
                    )
                )
            }
        }
    }
}