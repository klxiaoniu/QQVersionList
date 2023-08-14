package com.xiaoniu.qqversionlist

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xiaoniu.qqversionlist.Util.Companion.getVersionBig
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import java.lang.Thread.sleep
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGet.setOnClickListener {
            thread {
                try {
                    val okHttpClient = OkHttpClient()
                    val request = okhttp3.Request.Builder()
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
                            val start = it.indexOf("{\"versions")
                            val end = it.indexOf(",\"length")
                            it.substring(start, end)
                        }
                        runOnUiThread {
                            adapter = MyAdapter()
                            binding.rvContent.adapter = adapter
                            binding.rvContent.layoutManager =
                                androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
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

        val memVersion = getSharedPreferences("data", MODE_PRIVATE).getInt("version", -1)
        if (memVersion != -1) {
            binding.spinnerVersion.setSelection(memVersion)
        }
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
                getSharedPreferences("data", MODE_PRIVATE).edit()
                    .putInt("version", position)
                    .apply()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
            }
        }

        binding.btnGuess.setOnClickListener {
            binding.llGuess.visibility = if (binding.llGuess.visibility == View.VISIBLE) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }

        val memVersionSmall = getSharedPreferences("data", MODE_PRIVATE).getInt("versionSmall", -1)
        if (memVersionSmall != -1) {
            binding.etVersionSmall.setText(memVersionSmall.toString())
        }
        binding.btnGuessStart.setOnClickListener {
            try {
                val versionBig = binding.etVersionBig.text.toString()
                val versionSmall = binding.etVersionSmall.text.toString().toInt()
                if (versionSmall % 5 != 0) throw Exception("小版本确定不填5的倍数？")
                getSharedPreferences("data", MODE_PRIVATE).edit()
                    .putInt("versionSmall", versionSmall)
                    .apply()
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


        binding.btnGet.performClick()
    }

    fun Any.log(): Any {
        Log.i("QQVersionList", this.toString())
        return this
    }


    //https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.75.XXXXX_64.apk
    fun guessUrl(versionBig: String, versionSmall: Int, mode: Int) {
        lateinit var progressDialog: ProgressDialog
        var status = 0 //0:进行中，1：暂停，2：结束

        var link: String = ""
        val thr = Thread {
            var vSmall = versionSmall
            try {
                while (true) {
                    when (status) {
                        0 -> {
                            if (mode == 0) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}_64.apk"
                            } else {
                                if (link == "") {
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}_64.apk"
                                } else if (link.endsWith("HB.apk")) {
                                    status = 2
                                    continue
                                } else {
                                    link =
                                        "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}_64_HB.apk"
                                }
                            }
                            progressDialog.setMessage("正在猜测下载地址：$link")
                            val okHttpClient = OkHttpClient()
                            val request = okhttp3.Request.Builder()
                                .url(link)
                                .build()
                            val response = okHttpClient.newCall(request).execute()
                            val success = response.isSuccessful
                            if (success) {
                                status = 1
                                runOnUiThread {
                                    MaterialAlertDialogBuilder(this)
                                        .setTitle("猜测成功")
                                        .setMessage("下载地址：$link")
                                        .setPositiveButton("复制并停止") { _, _ ->
                                            copyText(link)
                                            status = 2
                                        }
                                        .setNegativeButton("仅停止") { _, _ ->
                                            status = 2
                                        }
                                        .setNeutralButton("继续猜测") { _, _ ->
                                            status = 0
                                        }
                                        .setCancelable(false)
                                        .show()
                                }
                            }
                            vSmall += 5

                        }

                        1 -> {
                            sleep(500)
                        }

                        2 -> {
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
                status = 2
            }
            show()
        }
        thr.start()
    }

    fun copyText(text: String) {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
        toasts("已复制：$text")
    }

    fun toasts(text: String) {
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun dlgErr(e: Exception) {
        runOnUiThread {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("程序出错，联系小牛")
                .setMessage(e.toString())
                .setPositiveButton("确定", null)
                .setNeutralButton("复制") { _, _ ->
                    copyText(e.toString())
                }
                .show()
        }
    }
}