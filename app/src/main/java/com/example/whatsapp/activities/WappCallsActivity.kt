package com.example.whatsapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R

class WappCallsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wapp_calls)

        val toolbar = findViewById<Toolbar>(R.id.toolbarCalls)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<Button>(R.id.btnStartVoiceCall).setOnClickListener {
            Toast.makeText(this, "Iniciando llamada de voz cifrada...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnStartVideoCall).setOnClickListener {
            Toast.makeText(this, "Iniciando videollamada HD...", Toast.LENGTH_SHORT).show()
        }
    }
}
