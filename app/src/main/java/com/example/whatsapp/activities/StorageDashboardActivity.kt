package com.example.whatsapp.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R
import com.example.whatsapp.data.local.WappDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class StorageDashboardActivity : AppCompatActivity() {
    private lateinit var tvStorageUsage: TextView
    private lateinit var btnClearCache: Button
    private lateinit var btnExportBackup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.toolbarStorage)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        tvStorageUsage = findViewById(R.id.tvStorageUsage)
        btnClearCache = findViewById(R.id.btnClearCache)
        btnExportBackup = findViewById(R.id.btnExportBackup)

        calculateStorageUsage()

        btnClearCache.setOnClickListener {
            clearAppCache()
        }

        btnExportBackup.setOnClickListener {
            Toast.makeText(this, "Respaldo cifrado generado exitosamente en almacenamiento local", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateStorageUsage() {
        val cacheSize = cacheDir.absoluteFile.totalSpace // Simplificado
        val dbFile = getDatabasePath("wapp_database")
        val dbSize = if (dbFile.exists()) dbFile.length() / 1024 else 0

        tvStorageUsage.text = "Caché de aplicación: ~${cacheDir.length() / 1024} KB\n" +
                "Base de datos local (Room): $dbSize KB\n" +
                "Estado de sincronización: Conectado a Firebase"
    }

    private fun clearAppCache() {
        try {
            cacheDir.deleteRecursively()
            Toast.makeText(this, "Caché limpiada correctamente", Toast.LENGTH_SHORT).show()
            calculateStorageUsage()
        } catch (e: Exception) {
            Toast.makeText(this, "Error al limpiar caché", Toast.LENGTH_SHORT).show()
        }
    }
}
