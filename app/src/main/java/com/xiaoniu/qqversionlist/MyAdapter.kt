package com.xiaoniu.qqversionlist

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoniu.qqversionlist.Util.Companion.getSize
import com.xiaoniu.qqversionlist.Util.Companion.getVersionBig
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class MyAdapter : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val list = mutableListOf<Object>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(list: List<Object>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv = itemView.findViewById<TextView>(R.id.tv_content)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {
        val view = View.inflate(parent.context, R.layout.item_version, null)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {
        val str = list[position].toString()
        val result = "版本：" + str.getVersionBig() + "\n大小：" + str.getSize() + "MB"
        holder.tv.text = result
        holder.tv.setOnClickListener {
            holder.tv.text = if (holder.tv.text == result) str else result
        }
    }

    override fun getItemCount() = list.size
}