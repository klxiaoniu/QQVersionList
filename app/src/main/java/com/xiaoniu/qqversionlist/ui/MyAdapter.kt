package com.xiaoniu.qqversionlist.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.util.SpUtil
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

class MyAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val list = mutableListOf<QQVersionBean>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(context: Context,list: List<QQVersionBean>) {
        this.list.apply {
            clear()
            addAll(list)
            var displayJudge = SpUtil.getDisplayFirst(context, "displayFirst", true)
            if (displayJudge) {
                first().displayType = 1 // 第一项默认展开
            }

        }
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView.findViewById<TextView>(R.id.tv_content)
        val ib_expand = itemView.findViewById<Button>(R.id.ib_expand)
    }

    class ViewHolderDetail(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linear_images = itemView.findViewById<LinearLayout>(R.id.linear_images)
        val tv_version = itemView.findViewById<TextView>(R.id.tv_version)
        val tv_size = itemView.findViewById<TextView>(R.id.tv_size)
        val tv_title = itemView.findViewById<TextView>(R.id.tv_title)
        val tv_desc = itemView.findViewById<TextView>(R.id.tv_desc)
        val ib_collapse = itemView.findViewById<Button>(R.id.ib_collapse)
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].displayType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = View.inflate(parent.context, R.layout.item_version, null)
                ViewHolder(view).apply {
                    ib_expand.setOnClickListener {
                        list[adapterPosition].displayType = 1
                        notifyItemChanged(adapterPosition)
                    }
                    tv.setOnLongClickListener {
                        showDialog(it.context, list[adapterPosition].jsonString.toPrettyFormat())
                        true
                    }
                }
            }

            else -> {
                val view = View.inflate(parent.context, R.layout.item_version_detail, null)
                ViewHolderDetail(view).apply {
                    ib_collapse.setOnClickListener {
                        list[adapterPosition].displayType = 0
                        notifyItemChanged(adapterPosition)
                    }
                    tv_desc.setOnLongClickListener {
                        showDialog(it.context, list[adapterPosition].jsonString.toPrettyFormat())
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
            val result = "版本：" + bean.versionNumber + "\n大小：" + bean.size + "MB"
            holder.tv.text = result
        } else if (holder is ViewHolderDetail) {
            holder.linear_images.removeAllViews()
            bean.imgs.forEach {
                val iv = ImageView(holder.itemView.context).apply {
                    setPadding(0, 0, 10, 0)
                }
                holder.linear_images.addView(iv)
                iv.load(it)
            }
            holder.tv_version.text = "版本：${bean.versionNumber}"
            holder.tv_size.text = "大小：${bean.size}MB"
            holder.tv_title.text = bean.featureTitle
            holder.tv_desc.text = bean.summary.joinToString(separator = "\n")
        }
    }

    override fun getItemCount() = list.size

    private fun showDialog(context: Context, s: String) {
        val tv = TextView(context).apply {
            text = s
            setTextIsSelectable(true)
            setPadding(100)
        }
        MaterialAlertDialogBuilder(context).setView(tv).show()
    }
}