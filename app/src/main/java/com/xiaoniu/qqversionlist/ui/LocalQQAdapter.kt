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
import com.xiaoniu.qqversionlist.databinding.LocalQqBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class LocalQQAdapter : RecyclerView.Adapter<LocalQQAdapter.LocalQQViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalQQViewHolder {
        val binding = LocalQqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalQQViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalQQViewHolder, position: Int) {
        val QQTargetInstallKV = DataStoreUtil.getStringKV("QQTargetInstall", "")
        val QQMinInstallKV = DataStoreUtil.getStringKV("QQMinInstall", "")
        val QQCompileInstallKV = DataStoreUtil.getStringKV("QQCompileInstall", "")
        val QQVersionInstallKV = DataStoreUtil.getStringKV("QQVersionInstall", "")
        val QQRdmUUIDInstallKV = DataStoreUtil.getStringKV("QQRdmUUIDInstall", "")
        val QQVersionCodeInstallKV = DataStoreUtil.getStringKV("QQVersionCodeInstall", "")
        val QQAppSettingParamsInstallKV = DataStoreUtil.getStringKV("QQAppSettingParamsInstall", "")
        val QQAppSettingParamsPadInstallKV =
            DataStoreUtil.getStringKV("QQAppSettingParamsPadInstall", "")
        val QQQuaKV = DataStoreUtil.getStringKV("QQQua", "")
        val QQRdmUUIDInstallProcessed =
            if (QQRdmUUIDInstallKV != "") ".${QQRdmUUIDInstallKV.split("_")[0]}" else ""
        val QQChannelInstallProcessed =
            if (QQAppSettingParamsInstallKV != "") QQAppSettingParamsInstallKV.split("#")[3] else ""
        holder.apply {
            if (QQVersionInstallKV != "") {
                itemQqInstallText.text =
                    itemView.context.getString(R.string.localQQVersion) + QQVersionInstallKV + QQRdmUUIDInstallProcessed + (if (QQVersionCodeInstallKV != "") " (${QQVersionCodeInstallKV})" else "") + (if (QQChannelInstallProcessed != "") " - $QQChannelInstallProcessed" else "")
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
                        if (DataStoreUtil.getBooleanKV("useNewLocalPage", true)) {
                            itemView.context.startActivity(Intent(
                                itemView.context, LocalAppDetailsActivity::class.java
                            ).apply { putExtra("localAppType", "QQ") })
                        } else {
                            val localInfoAllText = (if (QQTargetInstallKV != "") "Target SDK${
                                itemView.context.getString(R.string.colon)
                            }${QQTargetInstallKV}" else "") + (if (QQMinInstallKV != "") "\nMin SDK${
                                itemView.context.getString(R.string.colon)
                            }${QQMinInstallKV}" else "") + (if (QQCompileInstallKV != "") "\nCompile SDK${
                                itemView.context.getString(R.string.colon)
                            }${
                                QQCompileInstallKV
                            }" else "") + "\nVersion Name${itemView.context.getString(R.string.colon)}${
                                QQVersionInstallKV
                            }" + (if (QQRdmUUIDInstallKV != "") "\nRdm UUID${
                                itemView.context.getString(R.string.colon)
                            }${
                                QQRdmUUIDInstallKV
                            }" else "") + (if (QQVersionCodeInstallKV != "") "\nVersion Code${
                                itemView.context.getString(R.string.colon)
                            }${
                                QQVersionCodeInstallKV
                            }" else "") + (if (QQAppSettingParamsInstallKV != "") "\nAppSetting_params${
                                itemView.context.getString(R.string.colon)
                            }${
                                QQAppSettingParamsInstallKV
                            }" else "") + (if (QQAppSettingParamsPadInstallKV != "") "\nAppSetting_params_pad${
                                itemView.context.getString(R.string.colon)
                            }${
                                QQAppSettingParamsPadInstallKV
                            }" else "") + (if (QQQuaKV != "") "\nQUA${
                                itemView.context.getString(R.string.colon)
                            }${QQQuaKV}" else "")

                            val dialogLocalQqTimInfoBinding = DialogLocalQqTimInfoBinding.inflate(
                                LayoutInflater.from(itemView.context)
                            )

                            MaterialAlertDialogBuilder(itemView.context).setView(
                                dialogLocalQqTimInfoBinding.root
                            ).setTitle(R.string.localQQVersionDetails)
                                .setIcon(R.drawable.phone_find_line).apply {
                                    dialogLocalQqTimInfoBinding.apply {
                                        val dialogLocalSdkDesc =
                                            if (QQTargetInstallKV != "" && QQMinInstallKV != "" && QQChannelInstallProcessed != "") "Target $QQTargetInstallKV | Min $QQMinInstallKV | Compile $QQCompileInstallKV" else "Target $QQTargetInstallKV | Min $QQMinInstallKV"

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
                                            setCellDescription(QQVersionInstallKV)
                                            this.setOnClickListener {
                                                context.copyText(
                                                    "Version Name${
                                                        itemView.context.getString(R.string.colon)
                                                    }$QQVersionInstallKV"
                                                )
                                            }
                                        }
                                        dialogLocalRdmUuid.apply {
                                            if (QQRdmUUIDInstallKV != "") {
                                                setCellDescription(QQRdmUUIDInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Rdm UUID${
                                                            itemView.context.getString(R.string.colon)
                                                        }$QQRdmUUIDInstallKV"
                                                    )
                                                }
                                            } else dialogLocalRdmUuid.isVisible = false
                                        }
                                        dialogLocalVersionCode.apply {
                                            if (QQVersionCodeInstallKV != "") {
                                                setCellDescription(QQVersionCodeInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "Version Code${
                                                            itemView.context.getString(R.string.colon)
                                                        }$QQVersionCodeInstallKV"
                                                    )
                                                }
                                            } else dialogLocalVersionCode.isVisible = false
                                        }
                                        dialogLocalAppsettingParams.apply {
                                            if (QQAppSettingParamsInstallKV != "") {
                                                setCellDescription(QQAppSettingParamsInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params${
                                                            itemView.context.getString(R.string.colon)
                                                        }$QQAppSettingParamsInstallKV"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParams.isVisible = false
                                        }
                                        dialogLocalAppsettingParamsPad.apply {
                                            if (QQAppSettingParamsPadInstallKV != "") {
                                                setCellDescription(QQAppSettingParamsPadInstallKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "AppSetting_params_pad${
                                                            itemView.context.getString(R.string.colon)
                                                        }$QQAppSettingParamsPadInstallKV"
                                                    )
                                                }
                                            } else dialogLocalAppsettingParamsPad.isVisible = false
                                        }
                                        dialogLocalQua.apply {
                                            if (QQQuaKV != "") {
                                                setCellDescription(QQQuaKV)
                                                this.setOnClickListener {
                                                    context.copyText(
                                                        "QUA${
                                                            itemView.context.getString(
                                                                R.string.colon
                                                            )
                                                        }$QQQuaKV"
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

