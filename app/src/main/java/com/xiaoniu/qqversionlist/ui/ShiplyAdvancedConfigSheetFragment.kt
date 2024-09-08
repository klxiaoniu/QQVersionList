package com.xiaoniu.qqversionlist.ui

import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.TipTimeApplication.Companion.SHIPLY_DEFAULT_APPID
import com.xiaoniu.qqversionlist.TipTimeApplication.Companion.SHIPLY_DEFAULT_SDK_VERSION
import com.xiaoniu.qqversionlist.databinding.BottomsheetShiplyAdvancedConfigBinding
import com.xiaoniu.qqversionlist.util.DataStoreUtil
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
                shiplyAppid.editText?.setText(getString("shiplyAppid", ""))
                shiplyOsVersion.editText?.setText(
                    getString("shiplyOsVersion", "")
                )
                shiplyModel.editText?.setText(getString("shiplyModel", ""))
                shiplySdkVersion.editText?.setText(
                    getString("shiplySdkVersion", "")
                )
                shiplyLanguage.editText?.setText(
                    getString("shiplyLanguage", "")
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
                    putStringAsync(
                        "shiplyAppid", shiplyAppid.editText?.text.toString()
                    )
                    putStringAsync(
                        "shiplyOsVersion", shiplyOsVersion.editText?.text.toString()
                    )
                    putStringAsync(
                        "shiplyModel", shiplyModel.editText?.text.toString()
                    )
                    putStringAsync(
                        "shiplySdkVersion", shiplySdkVersion.editText?.text.toString()
                    )
                    putStringAsync(
                        "shiplyLanguage", shiplyLanguage.editText?.text.toString()
                    )
                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
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