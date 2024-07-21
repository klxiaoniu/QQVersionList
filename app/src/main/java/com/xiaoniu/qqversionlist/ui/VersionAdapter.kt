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
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
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

    private var getProgressSize = DataStoreUtil.getBoolean("progressSize", false)
    private var getVersionTCloud = DataStoreUtil.getBoolean("versionTCloud", true)

    class ViewHolder(val binding: ItemVersionBinding, val context: Context) :
        RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemVersionDetailBinding, val context: Context) :
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
                    ), parent.context
                ).apply {
                    binding.ibExpand.setOnClickListener {
                        currentList[adapterPosition].displayType = 1
                        notifyItemChanged(adapterPosition)
                    }
                    binding.cardAll.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, currentList[adapterPosition].jsonString.toPrettyFormat()
                            )
                        } else {
                            Toast.makeText(
                                it.context,
                                "未开启长按查看 JSON 详情功能，请前往设置开启",
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
                    ), parent.context
                ).apply {
                    binding.ibCollapse.setOnClickListener {
                        currentList[adapterPosition].displayType = 0
                        notifyItemChanged(adapterPosition)
                    }
                    binding.cardAllDetail.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, currentList[adapterPosition].jsonString.toPrettyFormat()
                            )
                        } else {
                            Toast.makeText(
                                it.context,
                                "未开启长按查看 JSON 详情功能，请前往设置开启",
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
                bindDisplayInstall(tvInstall, tvInstallCard, bean)
                bindVersionTCloud(tvVersion, bean, holder.context)
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
                        crossfade(true)
                        transformations(RoundedCornersTransformation(2.dp.toFloat()))
                    }
                }
                tvOldVersion.text = bean.versionNumber
                tvOldSize.text = bean.size + " MB"
                tvDetailVersion.text = "版本：" + bean.versionNumber
                tvDetailSize.text = "额定大小：" + bean.size + " MB"
                tvTitle.text = bean.featureTitle
                tvDesc.text = bean.summary.joinToString(separator = "\n- ", prefix = "- ")

                tvTitle.isVisible = tvTitle.text != ""

                bindDisplayInstall(tvOldInstall, tvOldInstallCard, bean)
                bindVersionTCloud(tvOldVersion, bean, holder.context)

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
        with(getProgressSize) {
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

    @SuppressLint("SetTextI18n")
    private fun bindDisplayInstall(
        tvInstall: TextView, tvInstallCard: MaterialCardView, bean: QQVersionBean
    ) {
        if (bean.displayInstall) {
            tvInstallCard.isVisible = true
            tvInstall.text = "已安装"
        } else {
            tvInstallCard.isVisible = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindVersionTCloud(
        tvVersion: TextView, bean: QQVersionBean, context: Context
    ) {
        if (getVersionTCloud) {
            val TCloudFont = ResourcesCompat.getFont(context, R.font.tcloud_number_vf)
            tvVersion.typeface = TCloudFont
        } else {
            tvVersion.setTypeface(null, Typeface.NORMAL)
        }
    }

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(96, 48, 96, 96)
        }
        MaterialAlertDialogBuilder(context).setView(tv).setTitle("JSON 详情")
            .setIcon(R.drawable.braces_line).show()
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val bean = currentList[position]
            when (payloads[0]) {
                "displayType" -> {
                    onBindViewHolder(holder, position)
                }

                "displayInstall" -> {
                    if (holder is ViewHolder) {
                        bindDisplayInstall(
                            holder.binding.tvInstall, holder.binding.tvInstallCard, bean
                        )
                    } else if (holder is ViewHolderDetail) {
                        bindDisplayInstall(
                            holder.binding.tvOldInstall, holder.binding.tvOldInstallCard, bean
                        )
                    }
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

                "isTCloud" -> {
                    if (holder is ViewHolder) {
                        bindVersionTCloud(
                            holder.binding.tvVersion, bean, holder.context
                        )
                    } else if (holder is ViewHolderDetail) {
                        bindVersionTCloud(
                            holder.binding.tvOldVersion, bean, holder.context
                        )
                    }
                }
            }
        }
    }

    fun updateItemProperty(payloads: Any?) {
        when (payloads) {
            "isShowProgressSize" -> {
                getProgressSize = DataStoreUtil.getBoolean("progressSize", false)
                notifyItemRangeChanged(0, currentList.size, "isShowProgressSize")
            }

            "isTCloud" -> {
                getVersionTCloud = DataStoreUtil.getBoolean("versionTCloud", true)
                notifyItemRangeChanged(0, currentList.size, "isTCloud")
            }
        }
    }

}

class VersionDiffCallback : DiffUtil.ItemCallback<QQVersionBean>() {
    override fun areItemsTheSame(
        oldItem: QQVersionBean, newItem: QQVersionBean
    ): Boolean {
        return oldItem.versions == newItem.versions
    }

    override fun areContentsTheSame(
        oldItem: QQVersionBean, newItem: QQVersionBean
    ): Boolean {
        return oldItem.displayType == newItem.displayType && oldItem.displayInstall == newItem.displayInstall
    }

    override fun getChangePayload(
        oldItem: QQVersionBean, newItem: QQVersionBean
    ): Any? {
        return if (oldItem.displayType != newItem.displayType) "displayType"
        else if (oldItem.displayInstall != newItem.displayInstall) "displayInstall"
        else null
    }

}
