package com.xiaoniu.qqversionlist.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.xiaoniu.qqversionlist.R
import com.xiaoniu.qqversionlist.data.QQVersionBean
import com.xiaoniu.qqversionlist.databinding.ActivityMainBinding
import com.xiaoniu.qqversionlist.databinding.DialogGuessBinding
import com.xiaoniu.qqversionlist.util.ClipboardUtil.copyText
import com.xiaoniu.qqversionlist.util.InfoUtil.dialogError
import com.xiaoniu.qqversionlist.util.InfoUtil.showToast
import com.xiaoniu.qqversionlist.util.LogUtil.log
import com.xiaoniu.qqversionlist.util.SpUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        adapter = MyAdapter()
        binding.rvContent.adapter = adapter
        binding.rvContent.layoutManager = LinearLayoutManager(this)
        val recyclerView: RecyclerView = findViewById(R.id.rv_content)

        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(5)))
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false)

        initButtons()

        WindowCompat.setDecorFitsSystemWindows(window, false)

    }

    fun Context.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    class VerticalSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {

                // 对于每一项都添加底部间距
                bottom = space
                // 如果不是第一行，则添加顶部间距
                if (parent.getChildAdapterPosition(view) != 0) {
                    top = space
                }
            }
        }
    }


    private fun initButtons() {

        var currentQQVersion = ""

        fun getData() {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val okHttpClient = OkHttpClient()
                    val request =
                        Request.Builder().url("https://im.qq.com/rainbow/androidQQVersionList")
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
                            val json = it.substring(pstart, pend)
                            Gson().fromJson(json, QQVersionBean::class.java).apply {
                                jsonString = json
                            }
                        }
                        withContext(Dispatchers.Main) {
                            adapter.setData(qqVersion)
                            currentQQVersion = qqVersion.first().versionNumber
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    dialogError(e)
                }
            }
        }

        getData()

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.btn_get -> {
                    getData()
                    true
                }

                R.id.btn_about -> {
                    MaterialAlertDialogBuilder(this).setTitle("关于")
                        .setMessage("QQ 版本列表实用工具\n\n作者：快乐小牛、有鲫雪狐\n\n版本：" + packageManager.getPackageInfo(
                            packageName, 0
                        ).let {
                            @Suppress("DEPRECATION") it.versionName + "(" + (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode else it.versionCode) + ")"
                        } + "\n\n内部使用，禁止外传\n\n2023.8.9").setPositiveButton("确定", null)
                        .setIcon(R.drawable.information_line).show()
                    true
                }

                else -> false
            }
        }



        binding.btnGuess.setOnClickListener {
            val dialogGuessView: View = layoutInflater.inflate(R.layout.dialog_guess, null)

            val dialogGuessBinding = DialogGuessBinding.bind(dialogGuessView)
            dialogGuessBinding.etVersionBig.editText?.setText(
                currentQQVersion
            )

            dialogGuessBinding.spinnerVersion.onItemSelectedListener =
                object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long
                    ) {
                        if (position == 0 || position == 2) {
                            dialogGuessBinding.etVersionSmall.visibility = View.VISIBLE
                        } else if (position == 1) {
                            dialogGuessBinding.etVersionSmall.visibility = View.GONE
                        }
                        SpUtil.putInt(this@MainActivity, "version", position)
                    }

                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    }
                }

            val dialogGuess =
                MaterialAlertDialogBuilder(this).setTitle("猜版").setIcon(R.drawable.search_line)
                    .setView(dialogGuessView).setCancelable(false).create()
            dialogGuess.show()


            dialogGuessBinding.btnGuessStart.setOnClickListener {
                try {
                    val versionBig = dialogGuessBinding.etVersionBig.editText?.text.toString()
                    val mode = dialogGuessBinding.spinnerVersion.selectedItemPosition
                    var versionSmall = 5
                    if (mode == MODE_TEST || mode == MODE_UNOFFICIAL) {
                        versionSmall =
                            dialogGuessBinding.etVersionSmall.editText?.text.toString().toInt()
                    }
                    if (versionSmall % 5 != 0) throw Exception("小版本确定不填5的倍数？")
                    SpUtil.putInt(this, "versionSmall", versionSmall)

                    guessUrl(versionBig, versionSmall, mode)

                } catch (e: Exception) {
                    e.printStackTrace()
                    dialogError(e)
                }
            }

            dialogGuessBinding.btnGuessCancel.setOnClickListener {
                dialogGuess.dismiss()
            }

            val memVersion = SpUtil.getInt(this, "version", -1)
            if (memVersion != -1) {
                dialogGuessBinding.spinnerVersion.setSelection(memVersion)
            }
            val memVersionSmall = SpUtil.getInt(this, "versionSmall", -1)
            if (memVersionSmall != -1) {
                dialogGuessBinding.etVersionSmall.editText?.setText(memVersionSmall.toString())
            }
        }

    }


    /**
     * 获取文件大小（以MB为单位）
     *
     * @param urlString 文件的URL字符串
     * @param callback 回调函数，接收文件大小（以MB为单位）作为参数
     */
    private fun getFileSizeInMB(urlString: String, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"

                val fileSize = connection.contentLength.toDouble()
                val fileSizeInMB = fileSize / (1024 * 1024)

                withContext(Dispatchers.Main) {
                    callback("%.2f".format(fileSizeInMB))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback("Error")
                }
            }
        }
    }


    //https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_8.9.75.XXXXX_64.apk
    private fun guessUrl(versionBig: String, versionSmall: Int, mode: Int) {
        // 绑定 AlertDialog 加载对话框布局
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        val progressSpinner = dialogView.findViewById<ProgressBar>(R.id.progress_spinner)
        val loadingMessage = dialogView.findViewById<TextView>(R.id.loading_message)

        val successButton = layoutInflater.inflate(R.layout.success_button, null)
        val shareButton = successButton.findViewById<Button>(R.id.dialog_share_line)
        val downloadButton = successButton.findViewById<Button>(R.id.dialog_download_line_2)
        val stopButton = successButton.findViewById<Button>(R.id.dialog_stop_line)
        val continueButton = successButton.findViewById<Button>(R.id.dialog_play_line)
        val copyAndStopButton = successButton.findViewById<Button>(R.id.dialog_copy)

        var status = STATUS_ONGOING

        val progressDialog =
            MaterialAlertDialogBuilder(this).setView(dialogView).setCancelable(false).create()

        fun updateProgressDialogMessage(newMessage: String) {
            if (progressDialog.isShowing) {
                loadingMessage.text = newMessage
            }
        }

        var link = ""
        val thread = Thread {
            var vSmall = versionSmall
            try {
                while (true) {
                    when (status) {
                        STATUS_ONGOING -> {
                            if (mode == MODE_TEST) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_$versionBig.${vSmall}_64.apk"
                            } else if (mode == MODE_UNOFFICIAL) {
                                link =
                                    "https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%20$versionBig.${vSmall}%2064.apk"
                            } else if (mode == MODE_OFFICIAL) {
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
                            updateProgressDialogMessage("正在猜测下载地址：$link")
                            val okHttpClient = OkHttpClient()
                            val request = Request.Builder().url(link).build()
                            val response = okHttpClient.newCall(request).execute()
                            val success = response.isSuccessful
                            if (success) {
                                status = STATUS_PAUSE
                                runOnUiThread {
                                    if (successButton.parent != null) {
                                        (successButton.parent as ViewGroup).removeView(successButton)
                                    }

                                    val successMaterialDialog =
                                        MaterialAlertDialogBuilder(this).setTitle("猜测成功")
                                            .setMessage("下载地址：$link")
                                            .setIcon(R.drawable.check_circle).setView(successButton)
                                            .setCancelable(false).show()

                                    // 复制并停止按钮点击事件
                                    copyAndStopButton.setOnClickListener {
                                        copyText(link)
                                        successMaterialDialog.dismiss()
                                        status = STATUS_END
                                    }

                                    // 继续按钮点击事件
                                    continueButton.setOnClickListener {
                                        vSmall += 5
                                        successMaterialDialog.dismiss()
                                        status = STATUS_ONGOING
                                    }

                                    // 停止按钮点击事件
                                    stopButton.setOnClickListener {
                                        successMaterialDialog.dismiss()
                                        status = STATUS_END
                                    }

                                    // 分享按钮点击事件
                                    shareButton.setOnClickListener {
                                        successMaterialDialog.dismiss()

                                        getFileSizeInMB(link) { appSize ->
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    if (mode == MODE_OFFICIAL) "Android QQ $versionBig 正式版（大小：$appSize MB）\n\n下载地址：$link"
                                                    else "Android QQ $versionBig.$vSmall 测试版（大小：$appSize MB）\n\n下载地址：$link"
                                                )
                                            }
                                            startActivity(
                                                Intent.createChooser(
                                                    shareIntent, "分享到"
                                                )
                                            )
                                            status = STATUS_END
                                        }
                                    }

                                    // 下载按钮点击事件
                                    downloadButton.setOnClickListener {
                                        val request = DownloadManager.Request(Uri.parse(link))
                                        val downloadManager =
                                            getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                        downloadManager.enqueue(request)
                                        successMaterialDialog.dismiss()
                                        status = STATUS_END
                                    }

                                }
                            } else {
                                vSmall += 5
                            }
                        }

                        STATUS_PAUSE -> {
                            sleep(500)
                        }

                        STATUS_END -> {
                            showToast("已停止猜测")
                            progressDialog.dismiss()
                            break
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dialogError(e)
            }
        }


        // AlertDialog
        progressSpinner.visibility = View.VISIBLE
        val buttonCancel = dialogView.findViewById<Button>(R.id.dialog_button_cancel)
        loadingMessage.text = "正在猜测下载地址"

        buttonCancel.setOnClickListener {
            status = STATUS_END
            progressDialog.dismiss()
        }

        progressDialog.show()
        thread.start()
    }


    companion object {
        const val STATUS_ONGOING = 0
        const val STATUS_PAUSE = 1
        const val STATUS_END = 2

        const val MODE_TEST = 0
        const val MODE_OFFICIAL = 1
        const val MODE_UNOFFICIAL = 2  //空格猜版
    }

}