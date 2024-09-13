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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.DataStoreUtil

class LocalTIMAdapter : RecyclerView.Adapter<LocalTIMAdapter.LocalTIMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalTIMViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.local_tim, parent, false)
        return LocalTIMViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalTIMViewHolder, position: Int) {
        val TIMVersionInstall2 = DataStoreUtil.getStringKV("TIMVersionInstall", "")
        val TIMVersionCodeInstall2 = DataStoreUtil.getStringKV("TIMVersionCodeInstall", "")
        val TIMAppSettingParamsInstall = DataStoreUtil.getStringKV("TIMAppSettingParamsInstall", "")
        val TIMRdmUUIDInstall = if (DataStoreUtil.getStringKV(
                "TIMRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getStringKV("TIMRdmUUIDInstall", "").split("_")[0]}" else ""
        val TIMChannelInstall = if (TIMAppSettingParamsInstall != "") DataStoreUtil.getStringKV(
            "TIMAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        holder.apply {
            if (TIMVersionInstall2 != "") {
                itemTimInstallText.text =
                    if (TIMChannelInstall != "") itemView.context.getString(R.string.localTIMVersion) + DataStoreUtil.getStringKV(
                        "TIMVersionInstall", ""
                    ) + TIMRdmUUIDInstall + (if (TIMVersionCodeInstall2 != "") " (${TIMVersionCodeInstall2})" else "") + " - $TIMChannelInstall" else itemView.context.getString(
                        R.string.localTIMVersion
                    ) + DataStoreUtil.getStringKV("TIMVersionInstall", "")
                itemTimInstallCard.visibility = View.VISIBLE
                itemTimInstallCard.setOnLongClickListener {
                    if (DataStoreUtil.getBooleanKV("longPressCard", true)) {
                        val tv = TextView(itemView.context).apply {
                            text = HtmlCompat.fromHtml(
                                (if (DataStoreUtil.getStringKV(
                                        "TIMTargetInstall", ""
                                    ) != ""
                                ) "<b>Target SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMTargetInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMMinInstall", ""
                                    ) != ""
                                ) "<br><b>Min SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMMinInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMCompileInstall", ""
                                    ) != ""
                                ) "<br><b>Compile SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMCompileInstall", ""
                                    )
                                }" else "") + "<br><b>Version Name</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMVersionInstall", ""
                                    )
                                }" + (if (DataStoreUtil.getStringKV(
                                        "TIMRdmUUIDInstall", ""
                                    ) != ""
                                ) "<br><b>Rdm UUID</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMRdmUUIDInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMVersionCodeInstall", ""
                                    ) != ""
                                ) "<br><b>Version Code</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMVersionCodeInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMAppSettingParamsInstall", ""
                                    ) != ""
                                ) "<br><b>AppSetting_params</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMAppSettingParamsInstall", ""
                                    )
                                }" else ""), HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            setTextIsSelectable(true)
                            setPadding(96, 48, 96, 96)
                        }
                        MaterialAlertDialogBuilder(itemView.context)
                            .setView(tv)
                            .setTitle(R.string.localTIMVersionDetails)
                            .setIcon(R.drawable.phone_find_line)
                            .show()
                    } else Toast.makeText(
                        itemView.context,
                        itemView.context.getString(R.string.longPressToViewSourceDetailsIsDisabled),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            } else itemTimInstallCard.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = 1

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    inner class LocalTIMViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemTimInstallCard: View = itemView.findViewById(R.id.item_tim_install_card)
        val itemTimInstallText: TextView = itemView.findViewById(R.id.item_tim_install_text)
    }
}

