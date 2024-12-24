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

package com.xiaoniu.qqversionlist.ui.components.cell

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.CellSingleClickBinding

@SuppressLint("CustomViewStyleable")
class CellSingleClick @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val binding: CellSingleClickBinding =
        CellSingleClickBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.Cell, defStyleAttr, 0)
        val title = a.getString(R.styleable.Cell_cellTitle)
        val iconResId = a.getResourceId(R.styleable.Cell_cellIcon, 0)
        val description = a.getString(R.styleable.Cell_cellDescription)
        a.recycle()
        if (title == null) throw IllegalArgumentException("Title and type are required attributes for CellSwitch.")

        binding.title.text = title

        if (iconResId != 0) binding.icon.apply {
            setImageResource(iconResId)
            isVisible = true
        } else binding.icon.isVisible = false

        if (description != null) binding.description.apply {
            text = description
            isVisible = true
        } else binding.description.isVisible = false

        setOnClickListener { v ->
            performClick()
            onClick?.invoke(v)
        }
    }

    var onClick: ((View) -> Unit)? = null

    fun setCellOnClickListener(listener: (View) -> Unit) {
        onClick = listener
    }
}
