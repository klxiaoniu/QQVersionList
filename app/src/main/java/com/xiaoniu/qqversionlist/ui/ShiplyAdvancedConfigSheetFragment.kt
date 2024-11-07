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

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xiaoniu.qqversionlist.QVTApplication.Companion.SHIPLY_DEFAULT_APPID
import com.xiaoniu.qqversionlist.QVTApplication.Companion.SHIPLY_DEFAULT_SDK_VERSION
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.databinding.BottomsheetShiplyAdvancedConfigBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import java.util.Locale

class ShiplyAdvancedConfigSheetFragment : BottomSheetDialogFragment() {
    private lateinit var shiplyAdvancedConfigSheetBinding: BottomsheetShiplyAdvancedConfigBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottomsheet_shiply_advanced_config, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        shiplyAdvancedConfigSheetBinding = BottomsheetShiplyAdvancedConfigBinding.bind(view)
        val shiplyAdvancedConfigSheetBehavior = (this.dialog as BottomSheetDialog).behavior
        shiplyAdvancedConfigSheetBehavior.isDraggable = false
        this@ShiplyAdvancedConfigSheetFragment.isCancelable = true
        shiplyAdvancedConfigSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        this@ShiplyAdvancedConfigSheetFragment.isCancelable = false
        shiplyAdvancedConfigSheetBinding.apply {
            shiplyAppid.helperText =
                getString(R.string.shiplyGeneralOptionalHelpText) + SHIPLY_DEFAULT_APPID
            shiplyModel.helperText =
                getString(R.string.shiplyGeneralOptionalHelpText) + Build.MODEL.toString()
            shiplyOsVersion.helperText =
                getString(R.string.shiplyGeneralOptionalHelpText) + SDK_INT.toString()
            shiplySdkVersion.helperText =
                getString(R.string.shiplyGeneralOptionalHelpText) + SHIPLY_DEFAULT_SDK_VERSION
            shiplyLanguage.helperText =
                getString(R.string.shiplyGeneralOptionalHelpText) + Locale.getDefault().language.toString()

            DataStoreUtil.apply {
                shiplyAppid.editText?.setText(getStringKV("shiplyAppid", ""))
                shiplyOsVersion.editText?.setText(
                    getStringKV("shiplyOsVersion", "")
                )
                shiplyModel.editText?.setText(getStringKV("shiplyModel", ""))
                shiplySdkVersion.editText?.setText(
                    getStringKV("shiplySdkVersion", "")
                )
                shiplyLanguage.editText?.setText(
                    getStringKV("shiplyLanguage", "")
                )

                btnShiplyConfigSave.setOnClickListener {
                    shiplyAppid.clearFocus()
                    shiplyOsVersion.clearFocus()
                    shiplyModel.clearFocus()
                    shiplySdkVersion.clearFocus()
                    shiplyLanguage.clearFocus()
                    val imm =
                        requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(shiplyLanguage.windowToken, 0)
                    val shiplyConfigList = listOf(
                        mapOf(
                            "key" to "shiplyAppid",
                            "value" to shiplyAppid.editText?.text.toString(),
                            "type" to "String"
                        ), mapOf(
                            "key" to "shiplyOsVersion",
                            "value" to shiplyOsVersion.editText?.text.toString(),
                            "type" to "String"
                        ), mapOf(
                            "key" to "shiplyModel",
                            "value" to shiplyModel.editText?.text.toString(),
                            "type" to "String"
                        ), mapOf(
                            "key" to "shiplySdkVersion",
                            "value" to shiplySdkVersion.editText?.text.toString(),
                            "type" to "String"
                        ), mapOf(
                            "key" to "shiplyLanguage",
                            "value" to shiplyLanguage.editText?.text.toString(),
                            "type" to "String"
                        )
                    )
                    batchPutKVAsync(shiplyConfigList)
                    requireContext().showToast(R.string.saved)
                    this@ShiplyAdvancedConfigSheetFragment.isCancelable = true
                    shiplyAdvancedConfigSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
            }

            btnShiplyConfigBack.setOnClickListener {
                val imm =
                    requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(shiplyLanguage.windowToken, 0)
                this@ShiplyAdvancedConfigSheetFragment.isCancelable = true
                shiplyAdvancedConfigSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }

            dragHandleView.setOnClickListener {
                val imm =
                    requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(shiplyLanguage.windowToken, 0)
                this@ShiplyAdvancedConfigSheetFragment.isCancelable = true
                shiplyAdvancedConfigSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    companion object {
        const val TAG = "ShiplyAdvancedConfigSheet"
    }
}