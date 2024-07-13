package com.xiaoniu.qqversionlist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyTextWithContext

class ShiplyUrlListAdapter(private val urlList: List<String>) :
    RecyclerView.Adapter<ShiplyUrlListAdapter.ShiplyUrlViewHolder>() {

    inner class ShiplyUrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shiplyUrlText = itemView.findViewById<TextView>(R.id.shiply_url_text)
        var currentUrl: String? = null

        init {
            val shiplyUrlCard = itemView.findViewById<View>(R.id.shiply_url_card)
            shiplyUrlCard.setOnClickListener {
                currentUrl?.let { url ->
                    copyTextWithContext(itemView.context, url)
                }
            }
            shiplyUrlCard.setOnLongClickListener {
                currentUrl?.let { url ->
                    copyTextWithContext(itemView.context, url)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShiplyUrlViewHolder {
        val urlCardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shiply_back_url_card, parent, false)
        return ShiplyUrlViewHolder(urlCardView)
    }

    override fun onBindViewHolder(holder: ShiplyUrlViewHolder, position: Int) {
        val currentUrl = urlList[position]
        holder.shiplyUrlText.text = currentUrl
        holder.currentUrl = currentUrl
    }

    override fun getItemCount(): Int {
        return urlList.size
    }
}