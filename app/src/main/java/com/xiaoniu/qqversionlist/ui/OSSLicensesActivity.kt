package com.xiaoniu.qqversionlist.ui

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.aboutlibraries.Libs
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.ui.ui.theme.QQVersionListTheme
import org.apache.commons.io.IOUtils
import java.net.URL
import java.nio.charset.StandardCharsets

class OSSLicensesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 不加这段代码的话 Google 可能会在系统栏加遮罩
        if (SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false

        setContent { QQVersionListTheme { OSSLicenses() } }
    }

    private fun readRawResource(context: Context, resourceId: Int): String {
        context.resources.openRawResource(resourceId).use { inputStream ->
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun OSSLicenses() {
        val lib = OSSLicensesObject.getCurrentLibrary()
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
                if (lib != null) Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(11.dp, 7.dp, 11.dp, 11.dp)
                ) {
                    val url = if (lib.website != null) lib.website.toString() else ""
                    val repoUrl = if (url.isNotEmpty()) URL(url) else null
                    val isGitHub = if (repoUrl != null) repoUrl.host == "github.com" else false
                    val isGitLab = if (repoUrl != null) repoUrl.host == "gitlab.com" else false
                    if (url.isNotEmpty()) Card(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(5.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        border = BorderStroke(0.dp, MaterialTheme.colorScheme.secondaryContainer),
                        onClick = {
                            if (url.isNotEmpty()) {
                                val intent = CustomTabsIntent.Builder().build()
                                intent.launchUrl(this@OSSLicensesActivity, Uri.parse(url))
                            }
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
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 13.55.sp
                                )
                            )
                        }
                    }
                    val libs = Libs.Builder()
                        .withJson(readRawResource(this@OSSLicensesActivity, R.raw.aboutlibraries))
                        .build()
                    if (!lib.licenses.isEmpty()) libs.licenses.forEach { allLicense ->
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
                                } else if (!licenseUrl.isNullOrEmpty()) Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(5.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    border = BorderStroke(
                                        0.dp, MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    onClick = {
                                        val intent = CustomTabsIntent.Builder().build()
                                        intent.launchUrl(
                                            this@OSSLicensesActivity,
                                            Uri.parse(licenseUrl.toString())
                                        )
                                    }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.info_card_line),
                                            contentDescription = stringResource(id = R.string.openSourceLicenseTitle),
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        Text(
                                            text = licenseUrl.toString(), style = TextStyle(
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                fontSize = 13.55.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))
                }
            }
        }
    }
}