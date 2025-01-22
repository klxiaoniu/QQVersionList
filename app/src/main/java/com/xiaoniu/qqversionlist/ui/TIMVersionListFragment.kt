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
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.xiaoniu.qqversionlist.databinding.RecycleTimVersionBinding
import com.xiaoniu.qqversionlist.util.Extensions.pxToDp

class TIMVersionListFragment : Fragment() {
    private var _fragmentBinding: RecycleTimVersionBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = RecycleTimVersionBinding.inflate(inflater, container, false)
        val view = fragmentBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        versionListStaggeredGridLayout(requireActivity() as MainActivity)
        fragmentBinding.rvTimContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        versionListStaggeredGridLayout(this.activity as MainActivity)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        versionListStaggeredGridLayout(this.activity as MainActivity)
    }

    override fun onResume() {
        super.onResume()
        versionListStaggeredGridLayout(this.activity as MainActivity)
    }

    override fun onStart() {
        super.onStart()
        versionListStaggeredGridLayout(this.activity as MainActivity)
    }

    // RecyclerView 在某些机型上不会根据视图更新列表高度，所以需要通知列表重绘
    @SuppressLint("NotifyDataSetChanged")
    private fun versionListStaggeredGridLayout(thisActivity: MainActivity) {
        val concatenated =
            ConcatAdapter(thisActivity.localTIMAdapter, thisActivity.timVersionAdapter)
        val screenWidthDp = (Resources.getSystem().displayMetrics.widthPixels).pxToDp
        val screenHeightDp = (Resources.getSystem().displayMetrics.heightPixels).pxToDp
        fragmentBinding.rvTimContent.apply {
            // 当横纵逻辑像素都大于 600 时，根据横向逻辑像素的不同区间显示不同的瀑布流布局
            // 小于 600 时显示线性布局
            if (screenHeightDp >= 600) when {
                screenWidthDp in 600..840 -> {
                    adapter = concatenated
                    layoutManager = StaggeredGridLayoutManager(
                        2, StaggeredGridLayoutManager.VERTICAL
                    )
                }

                screenWidthDp > 840 -> {
                    adapter = concatenated
                    layoutManager = StaggeredGridLayoutManager(
                        3, StaggeredGridLayoutManager.VERTICAL
                    )
                }

                else -> if (layoutManager is LinearLayoutManager) adapter?.notifyDataSetChanged() else {
                    adapter = concatenated
                    layoutManager = LinearLayoutManager(thisActivity)
                }
            } else if (layoutManager is LinearLayoutManager) adapter?.notifyDataSetChanged() else {
                adapter = concatenated
                layoutManager = LinearLayoutManager(thisActivity)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentBinding = null
    }
}