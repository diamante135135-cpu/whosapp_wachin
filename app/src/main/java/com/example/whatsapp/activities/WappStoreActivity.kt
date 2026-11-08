package com.example.whatsapp.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R

class WappStoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wapp_store)

        val toolbar = findViewById<Toolbar>(R.id.toolbarStore)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val lvStoreItems = findViewById<ListView>(R.id.lvStoreItems)
        val items = arrayOf(
            "Tema Oscuro Esmeralda (Instalado)",
            "Tema Neón Cyberpunk",
            "Widget de Conversaciones Rápidas",
            "Extensión de Notas Seguras",
            "Pack de Sonidos Personalizados"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        lvStoreItems.adapter = adapter
        lvStoreItems.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Elemento seleccionado: ${items[position]}", Toast.LENGTH_SHORT).show()
        }
    }
}
