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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ItemVersionBinding
import com.xiaoniu.qqversionlist.databinding.ItemVersionDetailBinding
import com.xiaoniu.qqversionlist.util.SpUtil
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

class VersionAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<QQVersionBean>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<QQVersionBean>) {
        this.list.apply {
            clear()
            addAll(list)
            val displayJudge = SpUtil.getBoolean("displayFirst", true)
            if (displayJudge) {
                first().displayType = 1 // 第一项默认展开
            }

        }
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemVersionBinding) : RecyclerView.ViewHolder(binding.root)

    class ViewHolderDetail(val binding: ItemVersionDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return list[position].displayType
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
                        list[adapterPosition].displayType = 1
                        notifyItemChanged(adapterPosition)
                    }
                    binding.itemAll.setOnLongClickListener {
                        if (SpUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, list[adapterPosition].jsonString.toPrettyFormat()
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
                        list[adapterPosition].displayType = 0
                        notifyItemChanged(adapterPosition)
                    }
                    binding.itemAllDetail.setOnLongClickListener {
                        if (SpUtil.getBoolean("longPressCard", true)) {
                            showDialog(
                                it.context, list[adapterPosition].jsonString.toPrettyFormat()
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
        val bean = list[position]
        if (holder is ViewHolder) {
            val result = "版本：" + bean.versionNumber + "\n大小：" + bean.size + " MB"
            holder.binding.tvContent.text = result
            if (!SpUtil.getBoolean("progressSize", false)) {
                holder.binding.listProgressLine.visibility = View.GONE
            } else {
                holder.binding.listProgressLine.visibility = View.VISIBLE
                holder.binding.listProgressLine.max =
                    ((list.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f) * 10).toInt()
                holder.binding.listProgressLine.progress = (bean.size.toFloat() * 10).toInt()
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
                tvContentDetail.text = "版本：" + bean.versionNumber + "\n大小：" + bean.size + " MB"
                tvTitle.text = bean.featureTitle
                tvDesc.text = bean.summary.joinToString(separator = "\n- ", prefix = "- ")
                tvPerSize.text =
                    "占比历史最大包（${(list.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f)} MB）：${
                        "%.2f".format(
                            bean.size.toFloat() / (list.maxByOrNull { it.size.toFloat() }?.size?.toFloat() ?: 0f) * 100
                        )
                    }%"

                if (!SpUtil.getBoolean("progressSize", false)) {
                    holder.binding.listDetailProgressLine.visibility = View.GONE
                    holder.binding.tvPerSize.visibility = View.GONE
                } else {
                    holder.binding.listDetailProgressLine.visibility = View.VISIBLE
                    holder.binding.tvPerSize.visibility = View.VISIBLE
                    holder.binding.listDetailProgressLine.max =
                        ((list.maxByOrNull { it.size.toFloat() }?.size?.toFloat()
                            ?: 0f) * 10).toInt()
                    holder.binding.listDetailProgressLine.progress =
                        (bean.size.toFloat() * 10).toInt()
                }

                if (tvTitle.text == "") {
                    holder.binding.tvTitle.visibility = View.GONE
                } else {
                    holder.binding.tvTitle.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun getItemCount() = list.size

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(96, 48, 96, 96)
        }
        MaterialAlertDialogBuilder(context)
            .setView(tv)
            .setTitle("Json 详情")
            .setIcon(R.drawable.braces_line)
            .show()
    }
}