package com.masrofy.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "مصروفي\n\nإدارة مصروفاتك ودخلك بسهولة"
            textSize = 24f
            setPadding(40, 80, 40, 40)
        }

        setContentView(title)
    }
}
