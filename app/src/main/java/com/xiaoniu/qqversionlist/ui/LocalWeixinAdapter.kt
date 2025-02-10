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
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.LocalWeixinBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class LocalWeixinAdapter : RecyclerView.Adapter<LocalWeixinAdapter.LocalWeixinViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalWeixinViewHolder {
        val binding = LocalWeixinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalWeixinViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocalWeixinViewHolder, position: Int) {
        val WeixinVersionInstallKV = DataStoreUtil.getStringKV("WeixinVersionInstall", "")
        val WeixinVersionCodeInstallKV = DataStoreUtil.getStringKV("WeixinVersionCodeInstall", "")
        holder.apply {
            if (WeixinVersionInstallKV != "") {
                itemWeixinInstallText.text =
                    itemView.context.getString(R.string.localWeixinVersion) + WeixinVersionInstallKV + (if (WeixinVersionCodeInstallKV != "") " (${WeixinVersionCodeInstallKV})" else "")
                itemWeixinInstallCard.isVisible = true
                itemWeixinInstallCard.setOnLongClickListener {
                    if (DataStoreUtil.getBooleanKV("longPressCard", true)) {
                        itemView.context.startActivity(Intent(
                            itemView.context, LocalAppDetailsActivity::class.java
                        ).apply { putExtra("localAppType", "Weixin") })
                    } else showToast(R.string.longPressToViewSourceDetailsIsDisabled)
                    true
                }
            } else itemWeixinInstallCard.isVisible = false
        }
    }

    override fun getItemCount(): Int = 1

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    inner class LocalWeixinViewHolder(binding: LocalWeixinBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val itemWeixinInstallText = binding.itemWeixinInstallText
        val itemWeixinInstallCard = binding.itemWeixinInstallCard
    }
}

