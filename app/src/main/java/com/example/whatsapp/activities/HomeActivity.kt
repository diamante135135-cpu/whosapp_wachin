package com.example.whatsapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.whatsapp.AccountActivity
import com.example.whatsapp.R
import com.example.whatsapp.adapter.ViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        val tabLayout = findViewById<TabLayout>(R.id.tabs)

        if (viewPager != null) {
            val adapter = ViewPagerAdapter(supportFragmentManager)
            viewPager.adapter = adapter
            tabLayout?.setupWithViewPager(viewPager)
        }

        btnSearchHome?.setOnClickListener {
            startActivity(Intent(this, UniversalSearchActivity::class.java))
        }

        three_dot?.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add(0, 1, 0, "Mi Perfil")
        popup.menu.add(0, 2, 1, "Buscar")
        popup.menu.add(0, 3, 2, "Ajustes")
        popup.menu.add(0, 4, 3, "Almacenamiento")
        popup.menu.add(0, 5, 4, "Automatizaciones")
        popup.menu.add(0, 6, 5, "WAPP Store")
        popup.menu.add(0, 7, 6, "WAPP Calls")
        popup.menu.add(0, 8, 7, "WAPP Discover")
        popup.menu.add(0, 9, 8, "WAPP Studio")
        popup.menu.add(0, 10, 9, "Cerrar Sesión")

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                1 -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                2 -> {
                    startActivity(Intent(this, UniversalSearchActivity::class.java))
                    true
                }
                3 -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                4 -> {
                    startActivity(Intent(this, StorageDashboardActivity::class.java))
                    true
                }
                5 -> {
                    startActivity(Intent(this, AutomationsActivity::class.java))
                    true
                }
                6 -> {
                    startActivity(Intent(this, WappStoreActivity::class.java))
                    true
                }
                7 -> {
                    startActivity(Intent(this, WappCallsActivity::class.java))
                    true
                }
                8 -> {
                    startActivity(Intent(this, WappDiscoverActivity::class.java))
                    true
                }
                9 -> {
                    startActivity(Intent(this, WappStudioActivity::class.java))
                    true
                }
                10 -> {
                    val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()
                    val intent = Intent(this, LogInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
