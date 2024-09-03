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
import android.content.Context
import android.graphics.Typeface
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
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

private var getProgressSize = DataStoreUtil.getBoolean("progressSize", false)
private var getVersionTCloud = DataStoreUtil.getBoolean("versionTCloud", true)
private var getVersionTCloudThickness = DataStoreUtil.getString("versionTCloudThickness", "System")

class VersionAdapter : ListAdapter<QQVersionBean, RecyclerView.ViewHolder>(VersionDiffCallback()) {

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
                        currentList[bindingAdapterPosition].displayType = 1
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardAll.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context,
                                currentList[bindingAdapterPosition].jsonString.toPrettyFormat()
                            )
                        } else Toast.makeText(
                            it.context,
                            R.string.longPressToViewSourceDetailsIsDisabledPleaseGoToSettingsToTurnItOn,
                            Toast.LENGTH_SHORT
                        ).show()
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
                        currentList[bindingAdapterPosition].displayType = 0
                        notifyItemChanged(bindingAdapterPosition)
                    }
                    binding.cardAllDetail.setOnLongClickListener {
                        if (DataStoreUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context,
                                currentList[bindingAdapterPosition].jsonString.toPrettyFormat()
                            )
                        } else Toast.makeText(
                            it.context,
                            R.string.longPressToViewSourceDetailsIsDisabledPleaseGoToSettingsToTurnItOn,
                            Toast.LENGTH_SHORT
                        ).show()

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
                //val result = "版本：" + bean.versionNumber + "\n额定大小：" + bean.size + " MB"
                holder.binding.apply {
                    tvVersion.text = bean.versionNumber
                    tvSize.text = bean.size + " MB"
                    bindProgress(
                        listProgressLine, null, tvPerSizeText, tvPerSizeCard, tvSizeCard, bean
                    )
                    bindDisplayInstall(tvInstall, tvInstallCard, bean)
                    bindVersionTCloud(tvVersion, holder.context)
                    bindAccessibilityTag(accessibilityTag, holder.context, bean)
                }
            }

            is ViewHolderDetail -> {
                holder.binding.apply {
                    linearImages.removeAllViews()
                    bean.imgs.forEachIndexed { index, s ->
                        val iv = ImageView(holder.itemView.context).apply {
                            setPadding(0, 0, if (index == bean.imgs.size - 1) 0 else 4.dp, 0)
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, 150.dp
                            )
                        }
                        linearImages.addView(iv)
                        iv.load(s) {
                            crossfade(true)
                            transformations(RoundedCornersTransformation(2.dp.toFloat()))
                        }
                    }
                    tvOldVersion.text = bean.versionNumber
                    tvOldSize.text = bean.size + " MB"
                    tvDetailVersion.text =
                        holder.itemView.context.getString(R.string.version) + bean.versionNumber
                    tvDetailSize.text =
                        holder.itemView.context.getString(R.string.reatedFileSize) + bean.size + " MB"
                    tvTitle.text = bean.featureTitle
                    tvDesc.text = bean.summary.joinToString(separator = "\n- ", prefix = "- ")

                    tvTitle.isVisible = tvTitle.text != ""

                    bindDisplayInstall(tvOldInstall, tvOldInstallCard, bean)
                    bindVersionTCloud(tvOldVersion, holder.context)
                    bindAccessibilityTag(accessibilityOldTag, holder.context, bean)

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
                val listMaxSize =
                    currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f

                tvPerSize?.text =
                    "${tvPerSizeCard.context.getString(R.string.currentSizeVsLargestHistoricalPackage)}${listMaxSize} MB${
                        tvPerSizeCard.context.getString(
                            R.string.endParenthesis
                        )
                    }${
                        ("%.2f".format(bean.size.toFloat() / listMaxSize * 100)).replace(
                            "100.00", "100"
                        )
                    }%"
                tvPerSizeText.text = "${
                    ("%.2f".format(bean.size.toFloat() / listMaxSize * 100)).replace(
                        "100.00", "100"
                    )
                }%"

                // 进度条这里不能直接用 listMaxSize
                // 因为鬼知道 Material 上游的什么 Bug，导致这里要故意拖点时间让 Material 进度指示条视图加载完毕才能正确显示进度更新，否则会显示异常
                listProgressLine.apply {
                    max = ((currentList.maxByOrNull { it.size.toFloat() }?.size?.toFloat()
                        ?: 0f) * 100).toInt()
                    progress = (bean.size.toFloat() * 100).toInt()
                }
            }
        }
    }

    private fun bindDisplayInstall(
        tvInstall: TextView, tvInstallCard: MaterialCardView, bean: QQVersionBean
    ) {
        if (bean.displayInstall) {
            tvInstallCard.isVisible = true
            tvInstall.text = tvInstall.context.getString(R.string.installed)
            if (bean.isAccessibility) {
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
        accessibilityTag: ImageView, context: Context, bean: QQVersionBean
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

    private fun bindVersionTCloud(
        tvVersion: TextView, context: Context
    ) {
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
                            holder.binding.tvVersion, holder.context
                        )
                    } else if (holder is ViewHolderDetail) {
                        bindVersionTCloud(
                            holder.binding.tvOldVersion, holder.context
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
                getVersionTCloudThickness =
                    DataStoreUtil.getString("versionTCloudThickness", "System")
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
