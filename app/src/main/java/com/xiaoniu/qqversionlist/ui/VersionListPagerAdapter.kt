package com.xiaoniu.qqversionlist.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VersionListPagerAdapter(private val context: Context) :
    FragmentStateAdapter(context as FragmentActivity) {
    override fun getItemCount(): Int = 1

    override fun createFragment(position: Int): Fragment {
        return QQVersionListFragmentAdapter()
    }

    fun getFragementAtPosition(position: Int): QQVersionListFragmentAdapter? {
        return when (position) {
            0 -> QQVersionListFragmentAdapter()
            else -> null
        }
    }
}