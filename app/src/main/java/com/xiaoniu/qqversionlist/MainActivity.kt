package com.xiaoniu.qqversionlist

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.xiaoniu.qqversionlist.Util.Companion.getVersionBig
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
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
                        val start = (responseData.indexOf("{\"versions\":"))
                        val end = (responseData.indexOf(";\n" + "      typeof"))
                        "start: $start, end: $end".log()
                        val totalJson = responseData.substring(start, end)//.apply { log() }
//                        val reader = JsonReader(StringReader(totalJson))
//                        reader.isLenient = true
//                        reader.beginObject()
//                        reader.nextName()
                        val qqVersion = Gson().fromJson<QQVersion>(totalJson, QQVersion::class.java)
//                        val qqVersion = JSON.parseObject(totalJson, QQVersion::class.java)
                        //qqVersion.versions.first().dataMap.log()
                        runOnUiThread {
//                            binding.tvContent.setText(
//                                responseData.substring(start, end))
                            adapter = MyAdapter()
                            binding.rvContent.adapter = adapter
                            binding.rvContent.layoutManager =
                                androidx.recyclerview.widget.LinearLayoutManager(this@MainActivity)
                            adapter.setData(qqVersion.versions64.toList().reversed())
                            binding.etVersionBig.setText(
                                qqVersion.versions64.last().toString().getVersionBig()
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
                guessUrl(versionBig, versionSmall)
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

    //    data class QQVersion(
//        val versions: List<DataMap>,
//        val versions64: List<DataMap>,
//    )
    data class QQVersion(
        val versions: Array<Object>,
        val versions64: Array<Object>,
    )

//    data class VersionInfo(
//        val versions: String,
//        val versionNumber: String,
//        val size: Int,
//        val featureTitle: String,
//        val imgs: List<String>,
//        val summary: List<String>,
//    )

//    data class DataMap(
//        val dataMap: Map<String, VersionInfo>,
//        val length: Int,
//    )


    //https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.75.XXXXX_64.apk
    fun guessUrl(versionBig: String, versionSmall: Int) {
        lateinit var progressDialog: ProgressDialog
        var stop = false

        var link = ""
        val thr = Thread {
            var vSmall = versionSmall
            while (!stop) {
                link = if (link == "") {
                    vSmall -= 5
                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_${versionBig}_64.apk"
                    // 先猜一次正式版
                } else
                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}_64.apk"
                progressDialog.setMessage("正在猜测下载地址：$link")
                val okHttpClient = OkHttpClient()
                val request = okhttp3.Request.Builder()
                    .url(link)
                    .build()
                val response = okHttpClient.newCall(request).execute()
                val success = response.isSuccessful
                if (success) {
                    copyText(link)
                    stop = true
                }
                vSmall += 5
            }

            toasts("已停止猜测")
            progressDialog.dismiss()

        }
        progressDialog = ProgressDialog(this).apply {
            setMessage("正在猜测下载地址")
            setCancelable(true)
            setOnCancelListener {
                stop = true
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