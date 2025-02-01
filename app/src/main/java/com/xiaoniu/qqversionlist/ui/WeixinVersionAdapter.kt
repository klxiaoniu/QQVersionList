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
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.WeixinVersionBean
import com.xiaoniu.qqversionlist.databinding.ItemWeixinVersionBinding
import com.xiaoniu.qqversionlist.databinding.ItemWeixinVersionDetailBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.Extensions.dp
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast

class WeixinVersionAdapter :
    ListAdapter<WeixinVersionBean, RecyclerView.ViewHolder>(WeixinVersionDiffCallback()) {
    private var getVersionTCloud = DataStoreUtil.getBooleanKV("versionTCloud", true)
    private var getVersionTCloudThickness =
        DataStoreUtil.getStringKV("versionTCloudThickness", "System")

    class ViewHolder(val binding: ItemWeixinVersionBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemWeixinVersionDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return currentList[position].displayType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> { // 卡片收起态
                ViewHolder(
                    ItemWeixinVersionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibWeixinExpand.setOnClickListener {
                        currentList[bindingAdapterPosition].displayType = 1
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardWeixinAll.setOnLongClickListener {
                        longPressCard(bindingAdapterPosition, it)
                        true
                    }
                }
            }

            else -> {
                ViewHolderDetail(
                    ItemWeixinVersionDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibWeixinCollapse.setOnClickListener {
                        currentList[bindingAdapterPosition].displayType = 0
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardAllDetail.setOnLongClickListener {
                        longPressCard(bindingAdapterPosition, it)
                        true
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = currentList[position]
        when (holder) {
            is ViewHolder -> {
                holder.binding.apply {
                    tvWeixinVersion.text = bean.version
                    bindDisplayInstall(tvWeixinInstall, tvWeixinInstallCard, bean)
                    bindVersionTCloud(tvWeixinVersion, holder.context)
                }
            }

            is ViewHolderDetail -> {
                holder.binding.apply {
                    tvWeixinOldVersion.text = bean.version
                    tvWeixinDetailVersion.text =
                        holder.itemView.context.getString(R.string.version) + bean.version
                    tvWeixinDetailDate.text =
                        holder.itemView.context.getString(R.string.releaseDateTIM) + bean.datetime
                    bindDisplayInstall(tvWeixinOldInstall, tvWeixinOldInstallCard, bean)
                    bindVersionTCloud(tvWeixinOldVersion, holder.context)
                    tvWeixinCatalogLink.setOnClickListener {
                        val uri =
                            Uri.parse("https://weixin.qq.com/updates?platform=android&version=${bean.version}")
                        val customTabsIntent = CustomTabsIntent.Builder().build()
                        customTabsIntent.launchUrl(it.context, uri)
                    }
                }
            }
        }
    }

    private fun longPressCard(bindingAdapterPosition: Int, it: View) {
        if (DataStoreUtil.getBooleanKV("longPressCard", true)) {
            val uri =
                Uri.parse("https://weixin.qq.com/updates?platform=android&version=${currentList[bindingAdapterPosition].version}")
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(it.context, uri)
        } else showToast(R.string.longPressToViewSourceDetailsIsDisabled)
    }

    private fun bindDisplayInstall(
        tvInstall: TextView, tvInstallCard: MaterialCardView, bean: WeixinVersionBean
    ) {
        if (bean.displayInstall) {
            tvInstallCard.isVisible = true
            tvInstall.text = tvInstall.context.getString(R.string.installed)
            val marginLayoutParams = tvInstallCard.layoutParams as ViewGroup.MarginLayoutParams
            marginLayoutParams.marginStart = 6.dp
            tvInstallCard.layoutParams = marginLayoutParams
        } else tvInstallCard.isVisible = false
    }

    private fun bindVersionTCloud(tvVersion: TextView, context: Context) {
        if (getVersionTCloud) {
            val TCloudFont: Typeface = when (getVersionTCloudThickness) {
                "Light" -> ResourcesCompat.getFont(context, R.font.tcloud_number_light)!!
                "Regular" -> ResourcesCompat.getFont(context, R.font.tcloud_number_regular)!!
                "Bold" -> ResourcesCompat.getFont(context, R.font.tcloud_number_bold)!!
                else -> ResourcesCompat.getFont(context, R.font.tcloud_number_vf)!!
            }
            tvVersion.typeface = TCloudFont
        } else tvVersion.setTypeface(null, Typeface.NORMAL)
    }

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(96, 48, 96, 96)
        }
        MaterialAlertDialogBuilder(context).setView(tv).setTitle(R.string.jsonDetails)
            .setIcon(R.drawable.braces_line).show()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else {
            val bean = currentList[position]
            when (payloads[0]) {
                "displayType" -> onBindViewHolder(holder, position)

                "displayInstall" -> if (holder is ViewHolder) bindDisplayInstall(
                    holder.binding.tvWeixinInstall, holder.binding.tvWeixinInstallCard, bean
                ) else if (holder is ViewHolderDetail) bindDisplayInstall(
                    holder.binding.tvWeixinOldInstall, holder.binding.tvWeixinOldInstallCard, bean
                )

                "isTCloud" -> if (holder is ViewHolder) bindVersionTCloud(
                    holder.binding.tvWeixinVersion, holder.context
                ) else if (holder is ViewHolderDetail) bindVersionTCloud(
                    holder.binding.tvWeixinOldVersion, holder.context
                )
            }
        }
    }

    fun updateItemProperty(payloads: Any?) {
        when (payloads) {
            "isTCloud" -> {
                getVersionTCloud = DataStoreUtil.getBooleanKV("versionTCloud", true)
                getVersionTCloudThickness =
                    DataStoreUtil.getStringKV("versionTCloudThickness", "System")
                notifyItemRangeChanged(0, currentList.size, "isTCloud")
            }
        }
    }

    class WeixinVersionDiffCallback : DiffUtil.ItemCallback<WeixinVersionBean>() {
        override fun areItemsTheSame(
            oldItem: WeixinVersionBean, newItem: WeixinVersionBean
        ): Boolean {
            return "${oldItem.version} ${oldItem.datetime}" == "${newItem.version} ${newItem.datetime}"
        }

        override fun areContentsTheSame(
            oldItem: WeixinVersionBean, newItem: WeixinVersionBean
        ): Boolean {
            return oldItem.displayType == newItem.displayType && oldItem.displayInstall == newItem.displayInstall
        }

        override fun getChangePayload(
            oldItem: WeixinVersionBean, newItem: WeixinVersionBean
        ): Any? {
            return if (oldItem.displayType != newItem.displayType) "displayType"
            else if (oldItem.displayInstall != newItem.displayInstall) "displayInstall"
            else null
        }
    }
}

