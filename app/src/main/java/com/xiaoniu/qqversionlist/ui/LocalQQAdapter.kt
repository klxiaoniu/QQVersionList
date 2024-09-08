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

class LocalQQAdapter : RecyclerView.Adapter<LocalQQAdapter.LocalQQViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalQQViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.local_qq_list, parent, false)
        return LocalQQViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalQQViewHolder, position: Int) {
        val QQVersionInstall2 = DataStoreUtil.getString("QQVersionInstall", "")
        val TIMVersionInstall2 = DataStoreUtil.getString("TIMVersionInstall", "")
        val QQVersionCodeInstall2 = DataStoreUtil.getString("QQVersionCodeInstall", "")
        val TIMVersionCodeInstall2 = DataStoreUtil.getString("TIMVersionCodeInstall", "")
        val QQAppSettingParamsInstall = DataStoreUtil.getString("QQAppSettingParamsInstall", "")
        val TIMAppSettingParamsInstall = DataStoreUtil.getString("TIMAppSettingParamsInstall", "")
        val QQRdmUUIDInstall = if (DataStoreUtil.getString(
                "QQRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getString("QQRdmUUIDInstall", "").split("_")[0]}" else ""
        val TIMRdmUUIDInstall = if (DataStoreUtil.getString(
                "TIMRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getString("TIMRdmUUIDInstall", "").split("_")[0]}" else ""
        val QQChannelInstall = if (QQAppSettingParamsInstall != "") DataStoreUtil.getString(
            "QQAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        val TIMChannelInstall = if (TIMAppSettingParamsInstall != "") DataStoreUtil.getString(
            "TIMAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        holder.apply {
            if (QQVersionInstall2 != "") {
                itemQqInstallText.text =
                    if (QQChannelInstall != "") itemView.context.getString(R.string.localQQVersion) + DataStoreUtil.getString(
                        "QQVersionInstall", ""
                    ) + QQRdmUUIDInstall + (if (QQVersionCodeInstall2 != "") " (${QQVersionCodeInstall2})" else "") + " - $QQChannelInstall" else itemView.context.getString(
                        R.string.localQQVersion
                    ) + DataStoreUtil.getString("QQVersionInstall", "")
                itemQqInstallCard.visibility = View.VISIBLE

                // 无障碍标记
                /*if (DefaultArtifactVersion(QQVersionInstall2) >= DefaultArtifactVersion(
                        EARLIEST_ACCESSIBILITY_VERSION
                    )
                ) itemQqInstallText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.accessibility_new_24px, 0, 0, 0
                ) else itemQqInstallText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.phone_find_line, 0, 0, 0
                )
                val oldItemQqInstallCardDescribe =
                    itemQqInstallText.text.toString()
                if (DefaultArtifactVersion(QQVersionInstall2) >= DefaultArtifactVersion(
                        EARLIEST_ACCESSIBILITY_VERSION
                    )
                ) itemQqInstallCard.contentDescription =
                    "$oldItemQqInstallCardDescribe。" + String(
                        Base64.decode(
                            getString(R.string.accessibilityTag),
                            Base64.NO_WRAP
                        ), Charsets.UTF_8
                    )*/
                itemQqInstallCard.setOnLongClickListener {
                    if (DataStoreUtil.getBoolean("longPressCard", true)) {
                        val tv = TextView(itemView.context).apply {
                            text = "Version Name: ${
                                DataStoreUtil.getString(
                                    "QQVersionInstall", ""
                                )
                            }" + (if (DataStoreUtil.getString(
                                    "QQRdmUUIDInstall", ""
                                ) != ""
                            ) "\n\nRdm UUID: ${
                                DataStoreUtil.getString(
                                    "QQRdmUUIDInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "QQVersionCodeInstall", ""
                                ) != ""
                            ) "\n\nVersion Code: ${
                                DataStoreUtil.getString(
                                    "QQVersionCodeInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "QQAppSettingParamsInstall", ""
                                ) != ""
                            ) "\n\nAppSetting_params: ${
                                DataStoreUtil.getString(
                                    "QQAppSettingParamsInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "QQAppSettingParamsPadInstall", ""
                                ) != ""
                            ) "\n\nAppSetting_params_pad: ${
                                DataStoreUtil.getString(
                                    "QQAppSettingParamsPadInstall", ""
                                )
                            }" else "")
                            setTextIsSelectable(true)
                            setPadding(96, 48, 96, 96)
                        }
                        MaterialAlertDialogBuilder(itemView.context)
                            .setView(tv)
                            .setTitle(R.string.localQQVersionDetails)
                            .setIcon(R.drawable.phone_find_line)
                            .show()
                    } else Toast.makeText(
                        itemView.context,
                        itemView.context.getString(R.string.longPressToViewSourceDetailsIsDisabledPleaseGoToSettingsToTurnItOn),
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
            } else itemQqInstallCard.visibility = View.GONE
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
                            text = "Version Name: ${
                                DataStoreUtil.getString(
                                    "TIMVersionInstall", ""
                                )
                            }" + (if (DataStoreUtil.getString(
                                    "TIMRdmUUIDInstall", ""
                                ) != ""
                            ) "\n\nRdm UUID: ${
                                DataStoreUtil.getString(
                                    "TIMRdmUUIDInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMVersionCodeInstall", ""
                                ) != ""
                            ) "\n\nVersion Code: ${
                                DataStoreUtil.getString(
                                    "TIMVersionCodeInstall", ""
                                )
                            }" else "") + (if (DataStoreUtil.getString(
                                    "TIMAppSettingParamsInstall", ""
                                ) != ""
                            ) "\n\nAppSetting_params: ${
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
    inner class LocalQQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemQqInstallText = itemView.findViewById<TextView>(R.id.item_qq_install_text)
        val itemQqInstallCard = itemView.findViewById<View>(R.id.item_qq_install_card)
        val itemTimInstallCard = itemView.findViewById<View>(R.id.item_tim_install_card)
        val itemTimInstallText = itemView.findViewById<TextView>(R.id.item_tim_install_text)
    }
}

