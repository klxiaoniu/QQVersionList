/*
    QQ Version Tool for Android™
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
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ItemVersionBinding
import com.xiaoniu.qqversionlist.databinding.ItemVersionDetailBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat
import com.xiaoniu.qqversionlist.util.dp

class VersionAdapter : ListAdapter<QQVersionBean, RecyclerView.ViewHolder>(VersionDiffCallback()) {
    // Extensions -> Number.dp
    /*private fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }*/

    class ViewHolder(val binding: ItemVersionBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemVersionDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return currentList[position].displayType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                ViewHolder(
                    ItemVersionBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                ).apply {
                    binding.ibExpand.setOnClickListener {
                        currentList[adapterPosition].displayType = 1
                        notifyItemChanged(adapterPosition)
                    }
                    binding.itemAll.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, currentList[adapterPosition].jsonString.toPrettyFormat()
                            )
                        } else {
                            Toast.makeText(
                                it.context,
                                "未开启长按查看详情功能\n请前往设置开启",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                }
            }

            else -> {
                ViewHolderDetail(
                    ItemVersionDetailBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                ).apply {
                    binding.ibCollapse.setOnClickListener {
                        currentList[adapterPosition].displayType = 0
                        notifyItemChanged(adapterPosition)
                    }
                    binding.itemAllDetail.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, currentList[adapterPosition].jsonString.toPrettyFormat()
                            )
                        } else {
                            Toast.makeText(
                                it.context,
                                "未开启长按查看详情功能\n请前往设置开启",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        true
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val bean = currentList[position]
        if (holder is ViewHolder) {
            //val result = "版本：" + bean.versionNumber + "\n额定大小：" + bean.size + " MB"
            holder.binding.apply {
                tvVersion.text = bean.versionNumber
                tvSize.text = bean.size + " MB"
                bindProgress(listProgressLine, null, tvPerSizeText, tvPerSizeCard, tvSizeCard, bean)
                if (DataStoreUtil.getString("QQVersionInstall", "") == bean.versionNumber) {
                    tvInstallCard.isVisible = true
                    tvInstall.text = "已安装"
                } else tvInstallCard.isVisible = false
            }
        } else if (holder is ViewHolderDetail) {
            holder.binding.apply {
                linearImages.removeAllViews()
                bean.imgs.forEach {
                    val iv = ImageView(holder.itemView.context).apply {
                        setPadding(0, 0, 10, 0)
                    }
                    linearImages.addView(iv)
                    iv.load(it) {
                        crossfade(200)
                    }
                }
                tvOldVersion.text = bean.versionNumber
                tvOldSize.text = bean.size + " MB"
                tvDetailVersion.text = "版本：" + bean.versionNumber
                tvDetailSize.text = "额定大小：" + bean.size + " MB"
                tvTitle.text = bean.featureTitle
                tvDesc.text = bean.summary.joinToString(separator = "\n- ", prefix = "- ")

                tvTitle.isVisible = tvTitle.text != ""

                if (DataStoreUtil.getString("QQVersionInstall", "") == bean.versionNumber) {
                    tvOldInstallCard.isVisible = true
                    tvOldInstall.text = "已安装"
                } else tvOldInstallCard.isVisible = false

                bindProgress(
                    listDetailProgressLine,
                    tvPerSize,
                    tvOldPerSizeText,
                    tvOldPerSizeCard,
                    tvOldSizeCard,
                    bean
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindProgress(
        listProgressLine: LinearProgressIndicator,
        tvPerSize: TextView?,
        tvPerSizeText: TextView,
        tvPerSizeCard: MaterialCardView,
        tvSizeCard: MaterialCardView,
        bean: QQVersionBean,
    ) {
        with(bean.isShowProgressSize) {
            tvPerSize?.isVisible = this
            listProgressLine.isVisible = this
            tvPerSizeCard.isVisible = this

            val layoutParams = tvSizeCard.layoutParams as? ViewGroup.MarginLayoutParams ?: return
            layoutParams.marginEnd = if (this) 6.dp else 0
            tvSizeCard.layoutParams = layoutParams

            if (this) {
                listProgressLine.max =
                    ((currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat()
                        ?: 0f) * 10).toInt()
                listProgressLine.progress = (bean.size.toFloat() * 10).toInt()

                tvPerSize?.text =
                    "占比历史最大包（${(currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f)} MB）：${
                        "%.2f".format(
                            bean.size.toFloat() / (currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f) * 100
                        )
                    }%"

                tvPerSizeText.text =
                    "${"%.2f".format(bean.size.toFloat() / (currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f) * 100)}%"

            }
        }

    }

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(96, 48, 96, 96)
        }
        MaterialAlertDialogBuilder(context)
            .setView(tv)
            .setTitle("JSON 详情")
            .setIcon(R.drawable.braces_line)
            .show()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val bean = currentList[position]
            when (payloads[0]) {
                "displayType" -> {
                    onBindViewHolder(holder, position)
                }

                "isShowProgressSize" -> {
                    if (holder is ViewHolder) {
                        bindProgress(
                            holder.binding.listProgressLine,
                            null,
                            holder.binding.tvPerSizeText,
                            holder.binding.tvPerSizeCard,
                            holder.binding.tvSizeCard,
                            bean
                        )
                    } else if (holder is ViewHolderDetail) {
                        bindProgress(
                            holder.binding.listDetailProgressLine,
                            holder.binding.tvPerSize,
                            holder.binding.tvOldPerSizeText,
                            holder.binding.tvOldPerSizeCard,
                            holder.binding.tvOldSizeCard,
                            bean
                        )
                    }
                }
            }
        }
    }

}

class VersionDiffCallback : DiffUtil.ItemCallback<QQVersionBean>() {
    override fun areItemsTheSame(
        oldItem: QQVersionBean,
        newItem: QQVersionBean
    ): Boolean {
        return oldItem.versions == newItem.versions
    }

    override fun areContentsTheSame(
        oldItem: QQVersionBean,
        newItem: QQVersionBean
    ): Boolean {
        return oldItem.displayType == newItem.displayType
                && oldItem.isShowProgressSize == newItem.isShowProgressSize
    }

    override fun getChangePayload(
        oldItem: QQVersionBean,
        newItem: QQVersionBean
    ): Any? {
        return if (oldItem.displayType != newItem.displayType) "displayType"
        else if (oldItem.isShowProgressSize != newItem.isShowProgressSize) "isShowProgressSize"
        else null
    }

}
