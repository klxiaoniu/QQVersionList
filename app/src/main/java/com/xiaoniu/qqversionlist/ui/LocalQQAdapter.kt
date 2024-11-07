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
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.LocalQqBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class LocalQQAdapter : RecyclerView.Adapter<LocalQQAdapter.LocalQQViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalQQViewHolder {
        val binding = LocalQqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalQQViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalQQViewHolder, position: Int) {
        val QQVersionInstall2 = DataStoreUtil.getStringKV("QQVersionInstall", "")
        val QQVersionCodeInstall2 = DataStoreUtil.getStringKV("QQVersionCodeInstall", "")
        val QQAppSettingParamsInstall = DataStoreUtil.getStringKV("QQAppSettingParamsInstall", "")
        val QQRdmUUIDInstall = if (DataStoreUtil.getStringKV(
                "QQRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getStringKV("QQRdmUUIDInstall", "").split("_")[0]}" else ""
        val QQChannelInstall = if (QQAppSettingParamsInstall != "") DataStoreUtil.getStringKV(
            "QQAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        holder.apply {
            if (QQVersionInstall2 != "") {
                itemQqInstallText.text =
                    itemView.context.getString(R.string.localQQVersion) + DataStoreUtil.getStringKV(
                        "QQVersionInstall", ""
                    ) + QQRdmUUIDInstall + (if (QQVersionCodeInstall2 != "") " (${QQVersionCodeInstall2})" else "") + (if (QQChannelInstall != "") " - $QQChannelInstall" else "")
                itemQqInstallCard.isVisible = true

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
                    if (DataStoreUtil.getBooleanKV("longPressCard", true)) {
                        val tv = TextView(itemView.context).apply {
                            text = HtmlCompat.fromHtml(
                                (if (DataStoreUtil.getStringKV(
                                        "QQTargetInstall",
                                        ""
                                    ) != ""
                                ) "<b>Target SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQTargetInstall",
                                        ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQMinInstall",
                                        ""
                                    ) != ""
                                ) "<br><b>Min SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQMinInstall",
                                        ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQCompileInstall",
                                        ""
                                    ) != ""
                                ) "<br><b>Compile SDK</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQCompileInstall", ""
                                    )
                                }" else "") + "<br><b>Version Name</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQVersionInstall", ""
                                    )
                                }" + (if (DataStoreUtil.getStringKV(
                                        "QQRdmUUIDInstall", ""
                                    ) != ""
                                ) "<br><b>Rdm UUID</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQRdmUUIDInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQVersionCodeInstall", ""
                                    ) != ""
                                ) "<br><b>Version Code</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQVersionCodeInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQAppSettingParamsInstall", ""
                                    ) != ""
                                ) "<br><b>AppSetting_params</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQAppSettingParamsInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQAppSettingParamsPadInstall", ""
                                    ) != ""
                                ) "<br><b>AppSetting_params_pad</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQAppSettingParamsPadInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "QQQua", ""
                                    ) != ""
                                ) "<br><b>QUA</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "QQQua", ""
                                    )
                                }" else ""), HtmlCompat.FROM_HTML_MODE_LEGACY
                            )
                            setTextIsSelectable(true)
                            setPadding(96, 48, 96, 96)
                        }
                        MaterialAlertDialogBuilder(itemView.context)
                            .setView(tv)
                            .setTitle(R.string.localQQVersionDetails)
                            .setIcon(R.drawable.phone_find_line)
                            .show()
                    } else itemView.context.showToast(R.string.longPressToViewSourceDetailsIsDisabled)
                    true
                }
            } else itemQqInstallCard.isVisible = false
        }
    }

    override fun getItemCount(): Int = 1

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    inner class LocalQQViewHolder(binding: LocalQqBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemQqInstallText = binding.itemQqInstallText
        val itemQqInstallCard = binding.itemQqInstallCard
    }
}

