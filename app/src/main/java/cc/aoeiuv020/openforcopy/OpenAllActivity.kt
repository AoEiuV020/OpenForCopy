package cc.aoeiuv020.openforcopy

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.open.IntentUtils
import java.io.File
import java.net.URLDecoder

class OpenAllActivity : AppCompatActivity() {
    private val TAG = "OpenForCopy"
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_all)

        // 开启该模式不会抛出FileUriExposedException异常，以便传递原始file协议给老应用，
        StrictMode.setThreadPolicy(ThreadPolicy.Builder().detectAll().penaltyLog().build())
        StrictMode.setVmPolicy(VmPolicy.Builder().detectAll().penaltyLog().build())

        Log.i(TAG, IntentUtils.intentToString(intent))

        val data = intent.data
        if (data == null) {
            failed()
            return
        }
        val host = data.host
        if (host == null) {
            failed()
            return
        }
        val path = data.path
        if (path == null) {
            failed()
            return
        }
        val name = URLDecoder.decode(
            path.substring(path.lastIndexOf('/') + 1),
            "UTF-8"
        )
        val folder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .resolve("OpenForCopy")
                .resolve(host)
        folder.mkdirs()
        val file = folder.resolve(name)

        Log.i(TAG, "onCreate: file=$file")

        showInfo(host, file)

        val button = findViewById<View>(R.id.button);
        button.setOnClickListener {
            if (file.exists()) {
                AlertDialog.Builder(this)
                    .setMessage("是否覆盖保存并打开")
                    .setPositiveButton("覆盖并打开") { _, _ ->
                        save(data, file) {
                            open(file)
                        }
                    }
                    .setNegativeButton("直接打开") { _, _ ->
                        open(file)
                    }
                    .show()
            } else {
                save(data, file) {
                    open(file)
                }
            }
        }
        button.performClick()
    }

    @SuppressLint("SetTextI18n")
    private fun showInfo(host: String, file: File) {
        val tvInfo: TextView = findViewById(R.id.tvInfo)
        val from = if (host == "com.tencent.mobileqq.fileprovider") {
            "QQ"
        } else {
            host
        }
        tvInfo.text = """
            代理打开文件: ${file.name}
            发起打开来自: $from
        """.trimIndent()
    }

    private fun save(data: Uri, file: File, onSuccess: Runnable) {
        step("正在转存。。。")
        Thread {
            try {
                contentResolver.openInputStream(data)!!.use { input ->
                    file.outputStream().use { output ->
                        step("正在写入。。。")
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                failed()
                return@Thread
            }
            step("转存完成")
            ok()
            onSuccess.run()
        }.start()
    }

    private fun open(file: File) {
        val newIntent = Intent(Intent.ACTION_VIEW)
        newIntent.setDataAndType(Uri.fromFile(file), intent.type)
        try {
            startActivity(newIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            failed()
            return
        }
    }

    private fun ok() {
        runOnUiThread {
            progressDialog?.dismiss()
        }
    }

    private fun step(s: String) {
        runOnUiThread {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(this, "请稍后...", s, true, true)
            } else {
                progressDialog?.setMessage(s)
            }
        }
    }

    private fun failed(s: String = "不支持！") {
        runOnUiThread {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

}