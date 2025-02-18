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

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.ExpLinkNextButtonBinding
import com.xiaoniu.qqversionlist.databinding.ItemExpBackUrlCardBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.FileUtil.downloadFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ExpUrlListAdapter(private val urlList: List<String>) :
    RecyclerView.Adapter<ExpUrlListAdapter.ExpUrlViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpUrlViewHolder {
        val binding =
            ItemExpBackUrlCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpUrlViewHolder(binding)
    }

    inner class ExpUrlViewHolder(binding: ItemExpBackUrlCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val expUrlText = binding.expUrlText
        var currentUrl: String? = null

        init {
            val expUrlCard = itemView.findViewById<MaterialCardView>(R.id.exp_url_card)
            expUrlCard.setOnClickListener {
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
                                val expLinkNextButtonBinding =
                                    ExpLinkNextButtonBinding.inflate(
                                        LayoutInflater.from(itemView.context)
                                    )
                                expLinkNextButtonBinding.root.parent?.let { parent ->
                                    if (parent is ViewGroup) parent.removeView(
                                        expLinkNextButtonBinding.root
                                    )
                                }
                                val expNextMaterialDialog =
                                    MaterialAlertDialogBuilder(itemView.context)
                                        .setTitle(R.string.additionalActions)
                                        .setIcon(R.drawable.flask_line)
                                        .setView(expLinkNextButtonBinding.root).apply {
                                            if (appSize != "" && appSize != "-1" && appSize != "0") setMessage(
                                                "${itemView.context.getString(R.string.downloadLink)}$url\n\n${
                                                    itemView.context.getString(
                                                        R.string.fileSize
                                                    )
                                                }$appSize MB"
                                            )
                                            else setMessage("${itemView.context.getString(R.string.downloadLink)}$url")
                                        }.show()

                                expLinkNextButtonBinding.apply {
                                    expNextBtnCopy.setOnClickListener {
                                        expNextMaterialDialog.dismiss()
                                        itemView.context.copyText(url)
                                    }

                                    expNextBtnDownload.setOnClickListener {
                                        expNextMaterialDialog.dismiss()
                                        downloadFile(itemView.context, url)
                                    }

                                    expNextBtnShare.setOnClickListener {
                                        expNextMaterialDialog.dismiss()
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, url)
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
            expUrlCard.setOnLongClickListener {
                currentUrl?.let { url ->
                    itemView.context.copyText(url)
                }
                true
            }
        }
    }

    override fun onBindViewHolder(holder: ExpUrlViewHolder, position: Int) {
        val currentUrl = urlList[position]
        holder.expUrlText.text = currentUrl
        holder.currentUrl = currentUrl
    }

    override fun getItemCount(): Int {
        return urlList.size
    }
}