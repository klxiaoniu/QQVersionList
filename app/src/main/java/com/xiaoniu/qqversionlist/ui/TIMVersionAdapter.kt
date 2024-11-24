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
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.TIMVersionBean
import com.xiaoniu.qqversionlist.databinding.ItemTimVersionBinding
import com.xiaoniu.qqversionlist.databinding.ItemTimVersionDetailBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.Extensions.dp
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

class TIMVersionAdapter :
    ListAdapter<TIMVersionBean, RecyclerView.ViewHolder>(TIMVersionDiffCallback()) {
    private var getVersionTCloud = DataStoreUtil.getBooleanKV("versionTCloud", true)
    private var getVersionTCloudThickness =
        DataStoreUtil.getStringKV("versionTCloudThickness", "System")

    class ViewHolder(val binding: ItemTimVersionBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemTimVersionDetailBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return currentList[position].displayType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> { // 卡片收起态
                ViewHolder(
                    ItemTimVersionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibTimExpand.setOnClickListener {
                        currentList[bindingAdapterPosition].displayType = 1
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardTimAll.setOnLongClickListener {
                        longPressCard(bindingAdapterPosition, it)
                        true
                    }
                }
            }

            else -> {
                ViewHolderDetail(
                    ItemTimVersionDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    ), parent.context
                ).apply {
                    binding.ibTimCollapse.setOnClickListener {
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
                    tvTimVersion.text = bean.version
                    bindDisplayInstall(tvTimInstall, tvTimInstallCard, bean)
                    bindVersionTCloud(tvTimVersion, holder.context)
                    bindAccessibilityTag(accessibilityTimTag, holder.context, bean)
                    bindQQNTTag(qqntTimTag, bean)
                }
            }

            is ViewHolderDetail -> {
                holder.binding.apply {
                    val fix = bean.fix
                    val new = bean.new
                    tvTimOldVersion.text = bean.version
                    tvTimDetailVersion.text =
                        holder.itemView.context.getString(R.string.version) + bean.version
                    tvTimDetailDate.text =
                        holder.itemView.context.getString(R.string.releaseDateTIM) + bean.datetime
                    if (fix == "" && new == "") tvTimDesc.isVisible = false
                    else tvTimDesc.text = new.split("<br/>")
                        .joinToString(separator = "\n") + (if (new != "") "\n" else "") + (if (fix != "") fix.split(
                        "<br/>"
                    ).joinToString(separator = "\n") else "")
                    bindDisplayInstall(tvTimOldInstall, tvTimOldInstallCard, bean)
                    bindVersionTCloud(tvTimOldVersion, holder.context)
                    bindAccessibilityTag(accessibilityTimOldTag, holder.context, bean)
                    bindQQNTTag(qqntTimOldTag, bean)
                }
            }
        }
    }

    private fun longPressCard(bindingAdapterPosition: Int, it: View) {
        if (DataStoreUtil.getBooleanKV("longPressCard", true)) showDialog(
            it.context, currentList[bindingAdapterPosition].jsonString.toPrettyFormat()
        ) else showToast(R.string.longPressToViewSourceDetailsIsDisabled)
    }

    private fun bindDisplayInstall(
        tvInstall: TextView, tvInstallCard: MaterialCardView, bean: TIMVersionBean
    ) {
        if (bean.displayInstall) {
            tvInstallCard.isVisible = true
            tvInstall.text = tvInstall.context.getString(R.string.installed)
            if (bean.isAccessibility || bean.isQQNTFramework) {
                val marginLayoutParams = tvInstallCard.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.marginStart = 3.dp
                tvInstallCard.layoutParams = marginLayoutParams
            } else {
                val marginLayoutParams = tvInstallCard.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.marginStart = 6.dp
                tvInstallCard.layoutParams = marginLayoutParams
            }
        } else tvInstallCard.isVisible = false
    }

    private fun bindAccessibilityTag(
        accessibilityTag: ImageView, context: Context, bean: TIMVersionBean
    ) {
        if (bean.isAccessibility) {
            accessibilityTag.contentDescription = String(
                Base64.decode(
                    context.getString(R.string.accessibilityTag), Base64.NO_WRAP
                ), Charsets.UTF_8
            )
            accessibilityTag.isVisible = true
        } else accessibilityTag.isVisible = false
    }

    private fun bindQQNTTag(qqntTag: ImageView, bean: TIMVersionBean) {
        qqntTag.isVisible = bean.isQQNTFramework
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
                    holder.binding.tvTimInstall, holder.binding.tvTimInstallCard, bean
                ) else if (holder is ViewHolderDetail) bindDisplayInstall(
                    holder.binding.tvTimOldInstall, holder.binding.tvTimOldInstallCard, bean
                )

                "isTCloud" -> if (holder is ViewHolder) bindVersionTCloud(
                    holder.binding.tvTimVersion, holder.context
                ) else if (holder is ViewHolderDetail) bindVersionTCloud(
                    holder.binding.tvTimOldVersion, holder.context
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

    class TIMVersionDiffCallback : DiffUtil.ItemCallback<TIMVersionBean>() {
        override fun areItemsTheSame(
            oldItem: TIMVersionBean, newItem: TIMVersionBean
        ): Boolean {
            return oldItem.jsonString == newItem.jsonString
        }

        override fun areContentsTheSame(
            oldItem: TIMVersionBean, newItem: TIMVersionBean
        ): Boolean {
            return oldItem.displayType == newItem.displayType && oldItem.displayInstall == newItem.displayInstall
        }

        override fun getChangePayload(
            oldItem: TIMVersionBean, newItem: TIMVersionBean
        ): Any? {
            return if (oldItem.displayType != newItem.displayType) "displayType"
            else if (oldItem.displayInstall != newItem.displayInstall) "displayInstall"
            else null
        }
    }
}

