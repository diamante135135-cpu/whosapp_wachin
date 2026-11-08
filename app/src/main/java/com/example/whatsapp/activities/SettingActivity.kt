package com.example.whatsapp.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.whatsapp.AccountActivity
import com.example.whatsapp.NotificationActivity
import com.example.whatsapp.R
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)
        val savedName = prefs.getString("username", null)
        tvSettingsUserName.text = if (!savedName.isNullOrEmpty()) savedName else "Usuario"

        account_layout.setOnClickListener(View.OnClickListener {
            val intent1 = Intent(this, AccountActivity::class.java)
            startActivity(intent1)
        })

        notification_layout.setOnClickListener(View.OnClickListener {
            val intent1 = Intent(this, NotificationActivity::class.java)
            startActivity(intent1)
        })

        val ivArrow = findViewById<ImageView>(R.id.ivSettingsArrow)
        ivArrow.setOnClickListener {
            val homeIntent = Intent(this@SettingActivity, HomeActivity::class.java)
            startActivity(homeIntent)
        }
    }
}
