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
        val TIMVersionInstall2 = DataStoreUtil.getString("TIMVersionInstall", "")
        val TIMVersionCodeInstall2 = DataStoreUtil.getString("TIMVersionCodeInstall", "")
        val QQAppSettingParamsInstall = DataStoreUtil.getString("QQAppSettingParamsInstall", "")
        val TIMAppSettingParamsInstall = DataStoreUtil.getString("TIMAppSettingParamsInstall", "")
        val TIMRdmUUIDInstall = if (DataStoreUtil.getString(
                "TIMRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getString("TIMRdmUUIDInstall", "").split("_")[0]}" else ""
        val TIMChannelInstall = if (TIMAppSettingParamsInstall != "") DataStoreUtil.getString(
            "TIMAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        holder.apply {
            if (TIMVersionInstall2 != "") {
                itemTimInstallText.text =
                    if (TIMChannelInstall != "") itemView.context.getString(R.string.localTIMVersion) + DataStoreUtil.getString(
                        "TIMVersionInstall", ""
                    ) + TIMRdmUUIDInstall + (if (TIMVersionCodeInstall2 != "") " (${TIMVersionCodeInstall2})" else "") + " - $TIMChannelInstall" else itemView.context.getString(
                        R.string.localTIMVersion
                    ) + DataStoreUtil.getString("TIMVersionInstall", "")
                itemTimInstallCard.visibility = View.VISIBLE
                itemTimInstallCard.setOnLongClickListener {
                    if (DataStoreUtil.getBoolean("longPressCard", true)) {
                        val tv = TextView(itemView.context).apply {
                            text = (if (DataStoreUtil.getString(
                                    "TIMTargetInstall", ""
                                ) != ""
                            ) "Target SDK: ${
                                DataStoreUtil.getString(
                                    "TIMTargetInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMMinInstall", ""
                                ) != ""
                            ) "\nMin SDK: ${
                                DataStoreUtil.getString(
                                    "TIMMinInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMCompileInstall", ""
                                ) != ""
                            ) "\nCompile SDK: ${
                                DataStoreUtil.getString(
                                    "TIMCompileInstall", ""
                                )
                            }" else "") + "\nVersion Name: ${
                                DataStoreUtil.getString(
                                    "TIMVersionInstall", ""
                                )
                            }" + (if (DataStoreUtil.getString(
                                    "TIMRdmUUIDInstall", ""
                                ) != ""
                            ) "\nRdm UUID: ${
                                DataStoreUtil.getString(
                                    "TIMRdmUUIDInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMVersionCodeInstall", ""
                                ) != ""
                            ) "\nVersion Code: ${
                                DataStoreUtil.getString(
                                    "TIMVersionCodeInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMAppSettingParamsInstall", ""
                                ) != ""
                            ) "\nAppSetting_params: ${
                                DataStoreUtil.getString(
                                    "TIMAppSettingParamsInstall", ""
                                )
                            }" else "")
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
                        itemView.context.getString(R.string.longPressToViewSourceDetailsIsDisabledPleaseGoToSettingsToTurnItOn),
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

