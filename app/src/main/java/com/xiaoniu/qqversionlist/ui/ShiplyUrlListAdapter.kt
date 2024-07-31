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

package com.xiaoniu.qqversionlist.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity.DOWNLOAD_SERVICE
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.ShiplyLinkNextButtonBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ShiplyUrlListAdapter(private val urlList: List<String>) :
    RecyclerView.Adapter<ShiplyUrlListAdapter.ShiplyUrlViewHolder>() {

    inner class ShiplyUrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shiplyUrlText = itemView.findViewById<TextView>(R.id.shiply_url_text)
        var currentUrl: String? = null

        init {
            val shiplyUrlCard = itemView.findViewById<MaterialCardView>(R.id.shiply_url_card)
            shiplyUrlCard.setOnClickListener {
                currentUrl?.let { url ->
                    CoroutineScope(Dispatchers.IO).launch {
                        var appSize = ""
                        try {
                            val okHttpClient = OkHttpClient()
                            val request = Request.Builder().url(url).head().build()
                            val response = okHttpClient.newCall(request).execute()
                            appSize = "%.2f".format(
                                response.header("Content-Length")?.toDoubleOrNull()
                                    ?.div(1024 * 1024)
                            )
                        } catch (_: Exception) {
                        } finally {
                            withContext(Dispatchers.Main) {
                                val shiplyLinkNextButtonBinding =
                                    ShiplyLinkNextButtonBinding.inflate(
                                        LayoutInflater.from(itemView.context)
                                    )
                                shiplyLinkNextButtonBinding.root.parent?.let { parent ->
                                    if (parent is ViewGroup) parent.removeView(
                                        shiplyLinkNextButtonBinding.root
                                    )
                                }
                                val shiplyNextMaterialDialog =
                                    MaterialAlertDialogBuilder(itemView.context)
                                        .setTitle(R.string.additionalActions)
                                        .setIcon(R.drawable.flask_line)
                                        .setView(shiplyLinkNextButtonBinding.root).apply {
                                            if (appSize != "" && appSize != "-1" && appSize != "0") setMessage(
                                                "${itemView.context.getString(R.string.downloadLink)}$url\n\n${itemView.context.getString(R.string.fileSize)}$appSize MB"
                                            )
                                            else setMessage("${itemView.context.getString(R.string.downloadLink)}$url")
                                        }
                                        .show()

                                shiplyLinkNextButtonBinding.apply {
                                    shiplyNextBtnCopy.setOnClickListener {
                                        shiplyNextMaterialDialog.dismiss()
                                        itemView.context.copyText(url)
                                    }

                                    shiplyNextBtnDownload.setOnClickListener {
                                        shiplyNextMaterialDialog.dismiss()
                                        if (DataStoreUtil.getBoolean(
                                                "downloadOnSystemManager",
                                                false
                                            )
                                        ) {
                                            val requestDownload =
                                                DownloadManager.Request(Uri.parse(url))
                                            requestDownload.setDestinationInExternalPublicDir(
                                                Environment.DIRECTORY_DOWNLOADS,
                                                url.substringAfterLast('/')
                                            )
                                            val downloadManager =
                                                itemView.context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                                            downloadManager.enqueue(requestDownload)
                                        } else {
                                            // 这里不用 Chrome Custom Tab 的原因是 Chrome 不知道咋回事有概率卡在“等待下载”状态
                                            val browserIntent =
                                                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                            browserIntent.apply {
                                                addCategory(Intent.CATEGORY_BROWSABLE)
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            itemView.context.startActivity(browserIntent)
                                        }
                                    }

                                    shiplyNextBtnShare.setOnClickListener {
                                        shiplyNextMaterialDialog.dismiss()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                if (url.contains("downv6.qq.com")) {
                                                    if (appSize != "" && appSize != "-1" && appSize != "0") "Android QQ（${itemView.context.getString(R.string.fileSize)}$appSize MB）\n\n${itemView.context.getString(R.string.downloadLink)}$url\n\n此下载地址指向的 QQ 安装包可能属于测试版本。测试版本可能存在不可预知的稳定性问题，请明确并确保自身具备足够的风险识别和承受能力。"
                                                    else "Android QQ ${itemView.context.getString(R.string.downloadLink)}$url\n\n此下载地址指向的 QQ 安装包可能属于测试版本。测试版本可能存在不可预知的稳定性问题，请明确并确保自身具备足够的风险识别和承受能力。"
                                                } else {
                                                    if (appSize != "" && appSize != "-1" && appSize != "0") "Android QQ（大小：$appSize MB）\n\n下载地址：$url\n\n此下载地址由 TDS 腾讯端服务 Shiply 发布平台提供，指向的 QQ 安装包可能属于测试版本。测试版本可能存在不可预知的稳定性问题，请明确并确保自身具备足够的风险识别和承受能力。"
                                                    else "Android QQ ${itemView.context.getString(R.string.downloadLink)}$url\n\n此下载地址由 TDS 腾讯端服务 Shiply 发布平台提供，指向的 QQ 安装包可能属于测试版本。测试版本可能存在不可预知的稳定性问题，请明确并确保自身具备足够的风险识别和承受能力。"
                                                }
                                            )
                                        }
                                        itemView.context.startActivity(
                                            Intent.createChooser(
                                                shareIntent,
                                                itemView.context.getString(R.string.shareTo)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            shiplyUrlCard.setOnLongClickListener {
                currentUrl?.let { url ->
                    itemView.context.copyText(url)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiplyUrlViewHolder {
        val urlCardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shiply_back_url_card, parent, false)
        return ShiplyUrlViewHolder(urlCardView)
    }

    override fun onBindViewHolder(holder: ShiplyUrlViewHolder, position: Int) {
        val currentUrl = urlList[position]
        holder.shiplyUrlText.text = currentUrl
        holder.currentUrl = currentUrl
    }

    override fun getItemCount(): Int {
        return urlList.size
    }
}