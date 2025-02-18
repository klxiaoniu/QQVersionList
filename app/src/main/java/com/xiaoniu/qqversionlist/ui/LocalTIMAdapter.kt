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

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.DialogLocalQqTimInfoBinding
import com.xiaoniu.qqversionlist.databinding.LocalTimBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class LocalTIMAdapter : RecyclerView.Adapter<LocalTIMAdapter.LocalTIMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalTIMViewHolder {
        val binding = LocalTimBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalTIMViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalTIMViewHolder, position: Int) {
        val TIMTargetInstallKV = DataStoreUtil.getStringKV("TIMTargetInstall", "")
        val TIMMinInstallKV = DataStoreUtil.getStringKV("TIMMinInstall", "")
        val TIMCompileInstallKV = DataStoreUtil.getStringKV("TIMCompileInstall", "")
        val TIMVersionInstallKV = DataStoreUtil.getStringKV("TIMVersionInstall", "")
        val TIMRdmUUIDInstallKV = DataStoreUtil.getStringKV("TIMRdmUUIDInstall", "")
        val TIMVersionCodeInstallKV = DataStoreUtil.getStringKV("TIMVersionCodeInstall", "")
        val TIMAppSettingParamsInstallKV =
            DataStoreUtil.getStringKV("TIMAppSettingParamsInstall", "")
        val TIMAppSettingParamsPadInstallKV =
            DataStoreUtil.getStringKV("TIMAppSettingParamsPadInstall", "")
        val TIMQuaKV = DataStoreUtil.getStringKV("TIMQua", "")
        val TIMRdmUUIDInstallProcessed =
            if (TIMRdmUUIDInstallKV != "") ".${TIMRdmUUIDInstallKV.split("_")[0]}" else ""
        val TIMChannelInstallProcessed =
            if (TIMAppSettingParamsInstallKV != "") TIMAppSettingParamsInstallKV.split("#")[3] else ""
        val TIMBasedOnQQVer = if (TIMQuaKV != "") TIMQuaKV.split("_")[3] else ""
        holder.apply {
            if (TIMVersionInstallKV != "") {
                itemTimInstallText.text =
                    itemView.context.getString(R.string.localTIMVersion) + TIMVersionInstallKV + TIMRdmUUIDInstallProcessed + (if (TIMVersionCodeInstallKV != "") " (${TIMVersionCodeInstallKV})" else "") + (if (TIMChannelInstallProcessed != "") " - $TIMChannelInstallProcessed" else "")
                itemTimInstallCard.isVisible = true
                itemTimInstallBasedOn.text = if (TIMBasedOnQQVer != "") itemView.context.getString(
                    R.string.basedOnQQVer, TIMBasedOnQQVer
                ) else ""
                itemTimInstallBasedOn.isVisible = TIMBasedOnQQVer != ""
                itemTimInstallBasedOnCard.isVisible = TIMBasedOnQQVer != ""
                itemTimInstallCard.setOnLongClickListener {
                    if (DataStoreUtil.getBooleanKV("longPressCard", true)) {
                        if (DataStoreUtil.getBooleanKV("useNewLocalPage", true)) {
                            itemView.context.startActivity(Intent(
                                itemView.context, LocalAppDetailsActivity::class.java
                            ).apply {
                                putExtra("localAppType", "TIM")
                            })
                        } else {
                            val localInfoAllText = (if (TIMTargetInstallKV != "") "Target SDK${
                                itemView.context.getString(R.string.colon)
                            }${
                                TIMTargetInstallKV
                            }" else "") + (if (TIMMinInstallKV != "") "\nMin SDK${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMMinInstallKV
                            }" else "") + (if (TIMCompileInstallKV != "") "\nCompile SDK${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMCompileInstallKV
                            }" else "") + "\nVersion Name${itemView.context.getString(R.string.colon)}${
                                TIMVersionInstallKV
                            }" + (if (TIMRdmUUIDInstallKV != "") "\nRdm UUID${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMRdmUUIDInstallKV
                            }" else "") + (if (TIMVersionCodeInstallKV != "") "\nVersion Code${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMVersionCodeInstallKV
                            }" else "") + (if (TIMAppSettingParamsInstallKV != "") "\nAppSetting_params${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMAppSettingParamsInstallKV
                            }" else "") + (if (TIMAppSettingParamsPadInstallKV != "") "\nAppSetting_params_pad${
                                itemView.context.getString(
                                    R.string.colon
                                )
                            }${
                                TIMAppSettingParamsPadInstallKV
                            }" else "") + (if (TIMQuaKV != "") "\nQUA${itemView.context.getString(R.string.colon)}${
                                TIMQuaKV
                            }" else "")

                            val dialogLocalQqTimInfoBinding = DialogLocalQqTimInfoBinding.inflate(
                                LayoutInflater.from(itemView.context)
                            )

                            MaterialAlertDialogBuilder(itemView.context).setView(
                                dialogLocalQqTimInfoBinding.root
                            ).setTitle(R.string.localTIMVersionDetails)
                                .setIcon(R.drawable.phone_find_line).apply {
                                    dialogLocalQqTimInfoBinding.apply {
                                        val dialogLocalSdkDesc =
                                            if (TIMTargetInstallKV != "" && TIMMinInstallKV != "" && TIMChannelInstallProcessed != "") "Target $TIMTargetInstallKV | Min $TIMMinInstallKV | Compile $TIMCompileInstallKV" else "Target $TIMTargetInstallKV | Min $TIMMinInstallKV"

                                        dialogLocalSdk.apply {
                                            setCellDescription(dialogLocalSdkDesc)
                                            this.setOnClickListener {
                                                context.copyText(
                                                    "Android SDK${
                                                        itemView.context.getString(R.string.colon)
                                                    }$dialogLocalSdkDesc"
                                                )
                                            }
                                        }
                                        dialogLocalVersionName.apply {
                                            setCellDescription(TIMVersionInstallKV)
                                            this.setOnClickListener {
                                                context.copyText(
                                                    "Version Name${
                                                        itemView.context.getString(R.string.colon)
                                                    }$TIMVersionInstallKV"
                                                )
                                            }
                                        }
                                        dialogLocalRdmUuid.apply {
                                            if (TIMRdmUUIDInstallKV != "") {
                                                setCellDescription(TIMRdmUUIDInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Rdm UUID${
                                                            itemView.context.getString(R.string.colon)
                                                        }$TIMRdmUUIDInstallKV"
                                                    )
                                                }
                                            } else dialogLocalRdmUuid.isVisible = false
                                        }
                                        dialogLocalVersionCode.apply {
                                            if (TIMVersionCodeInstallKV != "") {
                                                setCellDescription(TIMVersionCodeInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Version Code${
                                                            itemView.context.getString(R.string.colon)
                                                        }$TIMVersionCodeInstallKV"
                                                    )
                                                }
                                            } else dialogLocalVersionCode.isVisible = false
                                        }
                                        dialogLocalAppsettingParams.apply {
                                            if (TIMAppSettingParamsInstallKV != "") {
                                                setCellDescription(TIMAppSettingParamsInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params${
                                                            itemView.context.getString(R.string.colon)
                                                        }$TIMAppSettingParamsInstallKV"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParams.isVisible = false
                                        }
                                        dialogLocalAppsettingParamsPad.apply {
                                            if (TIMAppSettingParamsPadInstallKV != "") {
                                                setCellDescription(TIMAppSettingParamsPadInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params_pad${
                                                            itemView.context.getString(R.string.colon)
                                                        }$TIMAppSettingParamsPadInstallKV"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParamsPad.isVisible = false
                                        }
                                        dialogLocalQua.apply {
                                            if (TIMQuaKV != "") {
                                                setCellDescription(TIMQuaKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "QUA${
                                                            itemView.context.getString(
                                                                R.string.colon
                                                            )
                                                        }$TIMQuaKV"
                                                    )
                                                }
                                            } else dialogLocalQua.isVisible = false
                                        }
                                        dialogLocalCopyAll.setOnClickListener {
                                            context.copyText(localInfoAllText)
                                        }
                                    }
                                }.show()
                        }
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

