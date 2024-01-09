package com.xiaoniu.qqversionlist.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.InfoUtil.dlgErr
import com.xiaoniu.qqversionlist.util.InfoUtil.toasts
import com.xiaoniu.qqversionlist.util.LogUtil.log
import com.xiaoniu.qqversionlist.util.SpUtil
import com.xiaoniu.qqversionlist.util.StringUtil.getVersionBig
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButtons()
        initSpinner()
        initData()

        binding.btnGet.performClick()
    }

    private fun initButtons() {
        binding.btnGet.setOnClickListener {
            thread {
                try {
                    val okHttpClient = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://im.qq.com/rainbow/androidQQVersionList")
                        .build()
                    val response = okHttpClient.newCall(request).execute()
                    val responseData = response.body?.string()
                    if (responseData != null) {
                        val start = (responseData.indexOf("versions64\":[")) + 12
                        val end = (responseData.indexOf(";\n" + "      typeof"))
                        "start: $start, end: $end".log()
                        val totalJson = responseData.substring(start, end)//.apply { log() }
                        val qqVersion = totalJson.split("},{").reversed().map {
                            val pstart = it.indexOf("{\"versions")
                            val pend = it.indexOf(",\"length")
                            it.substring(pstart, pend)
                        }
                        runOnUiThread {
                            adapter = MyAdapter()
                            binding.rvContent.adapter = adapter
                            binding.rvContent.layoutManager =
                                LinearLayoutManager(this@MainActivity)
                            adapter.setData(qqVersion)
                            binding.etVersionBig.setText(
                                qqVersion.first().toString().getVersionBig()
                            )
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dlgErr(e)
                }
            }
        }

        binding.btnGuess.setOnClickListener {
            binding.llGuess.visibility = if (binding.llGuess.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        binding.btnGuessStart.setOnClickListener {
            try {
                val versionBig = binding.etVersionBig.text.toString()
                val versionSmall = binding.etVersionSmall.text.toString().toInt()
                if (versionSmall % 5 != 0) throw Exception("小版本确定不填5的倍数？")
                SpUtil.putInt(this, "versionSmall", versionSmall)
                val mode = binding.spinnerVersion.selectedItemPosition
                guessUrl(versionBig, versionSmall, mode)
            } catch (e: Exception) {
                e.printStackTrace()
                dlgErr(e)
            }
        }

        binding.btnAbout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("关于")
                .setMessage("QQ版本列表\n\n作者：快乐小牛\n\n内部使用，禁止外传\n\n2023.8.9")
                .setPositiveButton("确定", null)
                .show()
        }

    }

    private fun initData() {
        val memVersion = SpUtil.getInt(this, "version", -1)
        if (memVersion != -1) {
            binding.spinnerVersion.setSelection(memVersion)
        }
        val memVersionSmall = SpUtil.getInt(this, "versionSmall", -1)
        if (memVersionSmall != -1) {
            binding.etVersionSmall.setText(memVersionSmall.toString())
        }
    }

    private fun initSpinner() {
        binding.spinnerVersion.onItemSelectedListener = object :
            android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    binding.etVersionSmall.visibility = View.VISIBLE
                } else {
                    binding.etVersionSmall.visibility = View.GONE
                }
                SpUtil.putInt(this@MainActivity, "version", position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
        }
    }


    //https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.75.XXXXX_64.apk
    private fun guessUrl(versionBig: String, versionSmall: Int, mode: Int) {
        lateinit var progressDialog: ProgressDialog
        var status = STATUS_ONGOING

        var link = ""
        val thr = Thread {
            var vSmall = versionSmall
            try {
                while (true) {
                    when (status) {
                        STATUS_ONGOING -> {
                            if (mode == MODE_TEST) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}_64.apk"
                            } else {
                                if (link == "") {
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}_64.apk"
                                } else if (link.endsWith("HB.apk")) {
                                    status = STATUS_END
                                    continue
                                } else {
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}_64_HB.apk"
                                }
                            }
                            progressDialog.setMessage("正在猜测下载地址：$link")
                            val okHttpClient = OkHttpClient()
                            val request = Request.Builder()
                                .url(link)
                                .build()
                            val response = okHttpClient.newCall(request).execute()
                            val success = response.isSuccessful
                            if (success) {
                                status = STATUS_PAUSE
                                runOnUiThread {
                                    MaterialAlertDialogBuilder(this)
                                        .setTitle("猜测成功")
                                        .setMessage("下载地址：$link")
                                        .setPositiveButton("复制并停止") { _, _ ->
                                            copyText(link)
                                            status = STATUS_END
                                        }
                                        .setNegativeButton("仅停止") { _, _ ->
                                            status = STATUS_END
                                        }
                                        .setNeutralButton("继续猜测") { _, _ ->
                                            status = STATUS_ONGOING
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            }
                            vSmall += 5
                        }

                        STATUS_PAUSE -> {
                            sleep(500)
                        }

                        STATUS_END -> {
                            toasts("已停止猜测")
                            progressDialog.dismiss()
                            break
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dlgErr(e)
            }
        }
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在猜测下载地址")
            setCancelable(true)
            setOnCancelListener {
                status = STATUS_END
            }
            show()
        }
        thr.start()
    }


    companion object {
        const val STATUS_ONGOING = 0
        const val STATUS_PAUSE = 1
        const val STATUS_END = 2

        const val MODE_TEST = 0
        const val MODE_OFFICIAL = 1
    }

}