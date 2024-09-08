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

class QQVersionListFragmentAdapter : Fragment() {
    private var _fragmentBinding: RecycleQqVersionBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!
    private lateinit var thisActivity: MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = RecycleQqVersionBinding.inflate(inflater, container, false)
        val view = fragmentBinding.root
        thisActivity = requireActivity() as MainActivity
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        versionListStaggeredGridLayout()
        fragmentBinding.rvContent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll(dy)
            }
        })
    }

    fun versionListStaggeredGridLayout() {
        fragmentBinding.rvContent.apply {
            val concatenated =
                ConcatAdapter(thisActivity.localQQAdapter, thisActivity.versionAdapter)
            adapter = concatenated
            val screenWidthDp = (Resources.getSystem().displayMetrics.widthPixels).pxToDp
            val screenHeightDp = (Resources.getSystem().displayMetrics.heightPixels).pxToDp
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
        if (dy > 0) thisActivity.binding.btnGuess.shrink()
        else if (dy < 0) thisActivity.binding.btnGuess.extend()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentBinding = null
    }
}