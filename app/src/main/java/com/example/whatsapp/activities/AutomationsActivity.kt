package com.example.whatsapp.activities

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R

class AutomationsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_automations)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAutomations)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val switchAutoReply = findViewById<Switch>(R.id.switchAutoReply)
        val etAutoReplyMessage = findViewById<EditText>(R.id.etAutoReplyMessage)
        val btnSaveAutomation = findViewById<Button>(R.id.btnSaveAutomation)

        val prefs = getSharedPreferences("wapp_automation_prefs", Context.MODE_PRIVATE)
        switchAutoReply.isChecked = prefs.getBoolean("auto_reply_enabled", false)
        etAutoReplyMessage.setText(prefs.getString("auto_reply_message", "Hola, estoy ocupado en este momento. Te responderé pronto."))

        btnSaveAutomation.setOnClickListener {
            val enabled = switchAutoReply.isChecked
            val message = etAutoReplyMessage.text.toString()

            prefs.edit()
                .putBoolean("auto_reply_enabled", enabled)
                .putString("auto_reply_message", message)
                .apply()

            Toast.makeText(this, "Reglas de automatización guardadas", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
