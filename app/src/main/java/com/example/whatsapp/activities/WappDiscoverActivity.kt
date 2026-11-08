package com.example.whatsapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R

class WappDiscoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wapp_discover)

        val toolbar = findViewById<Toolbar>(R.id.toolbarDiscover)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val lvChannels = findViewById<ListView>(R.id.lvDiscoverChannels)
        val channels = arrayOf(
            "Android Developers Official",
            "Kotlin Community Hub",
            "WAPP Tech & Updates",
            "Open Source Enthusiasts",
            "Global UI/UX Designers"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channels)
        lvChannels.adapter = adapter
        lvChannels.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Te has unido al canal: ${channels[position]}", Toast.LENGTH_SHORT).show()
        }
    }
}
