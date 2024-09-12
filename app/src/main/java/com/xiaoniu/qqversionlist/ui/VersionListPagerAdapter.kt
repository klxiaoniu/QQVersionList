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

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VersionListPagerAdapter(context: Context) :
    FragmentStateAdapter(context as FragmentActivity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> QQVersionListFragmentAdapter()
            1 -> TIMVersionListFragmentAdapter()
            else -> throw IllegalArgumentException(position.toString())
        }
    }
}
