package com.xiaoniu.qqversionlist.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.StringUtil.getSize
import com.xiaoniu.qqversionlist.util.StringUtil.getVersionBig
import com.xiaoniu.qqversionlist.util.StringUtil.toPrettyFormat

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val list = mutableListOf<String>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<String>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView.findViewById<TextView>(R.id.tv_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = View.inflate(parent.context, R.layout.item_version, null)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val str = list[position]
        val result = "版本：" + str.getVersionBig() + "\n大小：" + str.getSize() + "MB"
        holder.tv.text = result
        holder.tv.setOnClickListener {
            holder.tv.text = if (holder.tv.text == result) str.toPrettyFormat() else result
        }
        holder.tv.setOnLongClickListener {
            val tv = TextView(it.context).apply {
                text = holder.tv.text
                setTextIsSelectable(true)
                setPadding(100)
            }
            MaterialAlertDialogBuilder(it.context).setView(tv).show()
            true
        }
    }

    override fun getItemCount() = list.size
}