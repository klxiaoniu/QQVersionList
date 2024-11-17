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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.LocalTimBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class LocalTIMAdapter : RecyclerView.Adapter<LocalTIMAdapter.LocalTIMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalTIMViewHolder {
        val binding = LocalTimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalTIMViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalTIMViewHolder, position: Int) {
        val TIMVersionInstall2 = DataStoreUtil.getStringKV("TIMVersionInstall", "")
        val TIMVersionCodeInstall2 = DataStoreUtil.getStringKV("TIMVersionCodeInstall", "")
        val TIMAppSettingParamsInstall = DataStoreUtil.getStringKV("TIMAppSettingParamsInstall", "")
        val TIMQua = DataStoreUtil.getStringKV("TIMQua", "")
        val TIMRdmUUIDInstall = if (DataStoreUtil.getStringKV(
                "TIMRdmUUIDInstall", ""
            ) != ""
        ) ".${DataStoreUtil.getStringKV("TIMRdmUUIDInstall", "").split("_")[0]}" else ""
        val TIMChannelInstall = if (TIMAppSettingParamsInstall != "") DataStoreUtil.getStringKV(
            "TIMAppSettingParamsInstall", ""
        ).split("#")[3] else ""
        val TIMBasedOnQQVer =
            if (TIMQua != "") DataStoreUtil.getStringKV("TIMQua", "").split("_")[3] else ""
        holder.apply {
            if (TIMVersionInstall2 != "") {
                itemTimInstallText.text =
                    itemView.context.getString(R.string.localTIMVersion) + DataStoreUtil.getStringKV(
                        "TIMVersionInstall", ""
                    ) + TIMRdmUUIDInstall + (if (TIMVersionCodeInstall2 != "") " (${TIMVersionCodeInstall2})" else "") + (if (TIMChannelInstall != "") " - $TIMChannelInstall" else "")
                itemTimInstallCard.isVisible = true
                itemTimInstallBasedOn.text = if (TIMBasedOnQQVer != "") itemView.context.getString(
                    R.string.basedOnQQVer, TIMBasedOnQQVer
                ) else ""
                itemTimInstallBasedOn.isVisible = TIMBasedOnQQVer != ""
                itemTimInstallBasedOnCard.isVisible = TIMBasedOnQQVer != ""
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
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMAppSettingParamsPadInstall", ""
                                    ) != ""
                                ) "<br><b>AppSetting_params_pad</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMAppSettingParamsPadInstall", ""
                                    )
                                }" else "") + (if (DataStoreUtil.getStringKV(
                                        "TIMQua", ""
                                    ) != ""
                                ) "<br><b>QUA</b>: ${
                                    DataStoreUtil.getStringKV(
                                        "TIMQua", ""
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
                    } else showToast(R.string.longPressToViewSourceDetailsIsDisabled)
                    true
                }
            } else itemTimInstallCard.isVisible = false
        }
    }

    override fun getItemCount(): Int = 1

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    inner class LocalTIMViewHolder(binding: LocalTimBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val itemTimInstallCard = binding.itemTimInstallCard
        val itemTimInstallText = binding.itemTimInstallText
        val itemTimInstallBasedOn = binding.itemTimInstallBasedOn
        val itemTimInstallBasedOnCard = binding.itemTimInstallBasedOnCard
    }
}

