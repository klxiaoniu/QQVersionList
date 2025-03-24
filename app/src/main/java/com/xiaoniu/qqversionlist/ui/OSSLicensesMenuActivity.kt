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

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Developer
import com.mikepenz.aboutlibraries.entity.Library
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.ui.theme.QQVersionListTheme
import com.xiaoniu.qqversionlist.util.InfoUtil.openUrlWithChromeCustomTab
import com.xiaoniu.qqversionlist.util.OSSLicensesObject
import org.apache.commons.io.IOUtils
import java.nio.charset.StandardCharsets

class OSSLicensesMenuActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false

        setContent { QQVersionListTheme { OSSLicensesMenu() } }
    }

    private fun readRawResource(context: Context, resourceId: Int): String {
        context.resources.openRawResource(resourceId).use { inputStream ->
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    @Composable
    private fun OSSLicensesMenu(lib: Libs? = null) {
        var libs = lib
        if (libs == null) libs =
            Libs.Builder().withJson(readRawResource(this, R.raw.aboutlibraries)).build() else lib
        OSSLicensesObject.libs = libs
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(id = R.string.openSourceLicenseTitle))
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .consumeWindowInsets(innerPadding), contentPadding = innerPadding
            ) {
                libs.libraries.forEach { library ->
                    item {
                        LibraryItem(library = library)
                        HorizontalDivider(thickness = 1.dp)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun LibraryItem(library: Library) {
        val version = library.artifactVersion
        Box(
            modifier = Modifier.clickable(onClick = {
                if (library.licenses.isNotEmpty() || library.website != null) {
                    val intent = Intent(
                        this@OSSLicensesMenuActivity, OSSLicensesActivity::class.java
                    )
                    intent.putExtra("library", library.uniqueId)
                    startActivity(intent)
                }
            })
        ) {
            Box(modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 16.dp)) {
                Column {
                    RenderName(library.name)
                    RenderUniqueID(library.uniqueId)
                    RenderDevelopers(library.developers)
                    RenderDescription(library.description)
                    RenderVersionAndLicenses(library, version)
                }
            }
        }
    }

    @Composable
    private fun RenderName(name: String) {
        return Text(
            modifier = Modifier.padding(0.dp, 2.dp, 0.dp, 0.dp),
            text = name, style = typography.titleMedium
        )
    }

    @Composable
    private fun RenderUniqueID(uniqueID: String) {
        return Text(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 2.dp),
            text = uniqueID, style = typography.labelSmall
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun RenderDevelopers(developers: List<Developer>) {
        if (!developers.isEmpty()) {
            FlowRow(
                modifier = Modifier
                    .padding(0.dp, 2.dp, 0.dp, 2.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                val colorScheme = MaterialTheme.colorScheme
                developers.withIndex().forEach { (index, developer) ->
                    if (!developer.organisationUrl.isNullOrEmpty()) Text(
                        text = developer.name.toString(),
                        style = typography.bodyMedium.copy(
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { openUrlWithChromeCustomTab(developer.organisationUrl.toString()) }),
                        color = colorScheme.primary
                    ) else Text(
                        text = developer.name.toString(),
                        style = typography.bodyMedium,
                    )
                    if (index < developers.size - 1) Text(
                        text = ", ", style = typography.bodyMedium
                    )
                }
            }
        }
    }

    @Composable
    private fun RenderDescription(description: String?) {
        if (!description.isNullOrBlank()) Text(
            text = description,
            style = typography.bodySmall,
            modifier = Modifier.padding(0.dp, 2.dp, 0.dp, 2.dp)
        )
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun RenderVersionAndLicenses(library: Library, version: String?) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = if (version.toString().first()
                            .isDigit()
                    ) "v${version.toString()}" else version.toString(),
                    modifier = Modifier.padding(8.dp, 3.dp, 8.dp, 3.dp),
                    style = typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            if (library.licenses.isNotEmpty()) library.licenses.forEach {
                Card(
                    colors = CardDefaults.cardColors(
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ), shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(8.dp, 3.dp, 8.dp, 3.dp),
                        text = it.name,
                        style = typography.labelSmall
                    )
                }
            }
        }
    }
}