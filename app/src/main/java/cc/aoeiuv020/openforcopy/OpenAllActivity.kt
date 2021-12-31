package cc.aoeiuv020.openforcopy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import cc.aoeiuv020.open.IntentUtils

class OpenAllActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_all)

        findViewById<TextView>(R.id.tvContent).text = IntentUtils.intentToString(intent)
    }
}