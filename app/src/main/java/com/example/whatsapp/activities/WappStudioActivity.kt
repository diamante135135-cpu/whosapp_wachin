package com.example.whatsapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R

class WappStudioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wapp_studio)

        val toolbar = findViewById<Toolbar>(R.id.toolbarStudio)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        findViewById<Button>(R.id.btnApplyEmeraldTheme).setOnClickListener {
            Toast.makeText(this, "Tema Esmeralda Pro aplicado con éxito", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnApplyMidnightTheme).setOnClickListener {
            Toast.makeText(this, "Tema Medianoche Oscura aplicado con éxito", Toast.LENGTH_SHORT).show()
        }
    }
}
