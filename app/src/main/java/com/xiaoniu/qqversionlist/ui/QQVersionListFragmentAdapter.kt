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
import com.xiaoniu.qqversionlist.databinding.RecycleQqVersionBinding
import com.xiaoniu.qqversionlist.util.pxToDp

// 将视图绑定放在 Fragment 前声明，否则在旋转屏幕时会导致 Fragment 里的数据销毁
// 相信用户内存的力量（逃）
private var _fragmentBinding: RecycleQqVersionBinding? = null

class QQVersionListFragmentAdapter : Fragment() {
    private val fragmentBinding get() = _fragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = RecycleQqVersionBinding.inflate(inflater, container, false)
        val view = fragmentBinding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        versionListStaggeredGridLayout(requireActivity() as MainActivity)
        fragmentBinding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll(dy)
            }
        })
    }

    fun versionListStaggeredGridLayout(thisActivity: MainActivity) {
        val concatenated = ConcatAdapter(thisActivity.localQQAdapter, thisActivity.versionAdapter)
        val screenWidthDp = (Resources.getSystem().displayMetrics.widthPixels).pxToDp
        val screenHeightDp = (Resources.getSystem().displayMetrics.heightPixels).pxToDp
        fragmentBinding.rvContent.apply {
            adapter = concatenated
            layoutManager = if (screenHeightDp >= 600) {
                when {
                    screenWidthDp in 600..840 -> StaggeredGridLayoutManager(
                        2, StaggeredGridLayoutManager.VERTICAL
                    )

                    screenWidthDp > 840 -> StaggeredGridLayoutManager(
                        3, StaggeredGridLayoutManager.VERTICAL
                    )

                    else -> LinearLayoutManager(thisActivity)
                }
            } else LinearLayoutManager(thisActivity)
        }
    }

    private fun handleScroll(dy: Int) {
        val thisActivity = requireActivity() as MainActivity
        if (dy > 0) thisActivity.binding.btnGuess.shrink()
        else if (dy < 0) thisActivity.binding.btnGuess.extend()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentBinding = null
    }
}