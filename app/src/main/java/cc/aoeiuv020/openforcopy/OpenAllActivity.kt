package cc.aoeiuv020.openforcopy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cc.aoeiuv020.open.IntentUtils
import java.net.URLDecoder
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.webkit.MimeTypeMap





class OpenAllActivity : AppCompatActivity() {
    private val TAG = "OpenForCopy"
    private val tvProgress: TextView by lazy { findViewById(R.id.tvProgress) }
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


        findViewById<View>(R.id.btnOpen).setOnClickListener {
            val newIntent = Intent(Intent.ACTION_VIEW)
            newIntent.setDataAndType(Uri.fromFile(file), intent.type)
            try {
                startActivity(newIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                failed()
                return@setOnClickListener
            }
        }
        findViewById<View>(R.id.btnSave).setOnClickListener {
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
            }.start()
        }
    }

    private fun step(s: String) {
        runOnUiThread {
            tvProgress.text = s
        }
    }

    private fun failed(s: String = "不支持！") {
        runOnUiThread {
            Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
        }
    }

}