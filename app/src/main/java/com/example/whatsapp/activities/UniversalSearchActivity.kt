package com.example.whatsapp.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.whatsapp.R
import com.example.whatsapp.data.local.WappDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UniversalSearchActivity : AppCompatActivity() {
    private lateinit var etSearchQuery: EditText
    private lateinit var lvSearchResults: ListView
    private lateinit var database: WappDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_universal_search)

        val toolbar = findViewById<Toolbar>(R.id.toolbarSearch)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        etSearchQuery = findViewById(R.id.etSearchQuery)
        lvSearchResults = findViewById(R.id.lvSearchResults)
        database = WappDatabase.getDatabase(this)

        etSearchQuery.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                performSearch(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performSearch(query: String) {
        if (query.trim().isEmpty()) {
            lvSearchResults.adapter = null
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val results = database.messageDao().searchMessages(query)
            val resultTexts = results.map { "De: ${it.sender} - ${it.messageText} (${it.timestamp})" }

            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(this@UniversalSearchActivity, android.R.layout.simple_list_item_1, resultTexts)
                lvSearchResults.adapter = adapter
            }
        }
    }
}
