package com.example.whatsapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp.R
import com.example.whatsapp.security.P2PIdentityManager

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(mainLooper).postDelayed({
            val identityManager = P2PIdentityManager.getInstance(this)
            val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)

            val hasP2PIdentity = identityManager.hasIdentity()
            val isLoggedIn = prefs.getBoolean("is_logged_in", false)

            if (hasP2PIdentity || isLoggedIn) {
                // Abrir directamente la pantalla principal si ya tiene identidad guardada
                val intent = Intent(this@SplashScreenActivity, HomeActivity::class.java)
                startActivity(intent)
            } else {
                // Primera apertura: ir al registro simplificado
                val intent = Intent(this@SplashScreenActivity, LogInActivity::class.java)
                startActivity(intent)
            }
            finish()
        }, 1200)
    }
}
