package com.example.whatsapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp.R
import com.example.whatsapp.data.Users
import com.example.whatsapp.security.P2PIdentityManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        nextPhnBtn.setOnClickListener {
            val username = tvPhoneNumber.text.toString().trim()
            if (username.length < 2) {
                tvPhoneNumber.error = "Por favor ingresa tu nombre"
                return@setOnClickListener
            }

            flProgressBarLogIn.visibility = View.VISIBLE
            nextPhnBtn.isEnabled = false

            // 1. Generación automática de identidad criptográfica P2P
            val identityManager = P2PIdentityManager.getInstance(this)
            val identity = identityManager.getOrCreateIdentity(username)

            // 2. Registrar la identidad P2P en el sistema de descubrimiento
            try {
                val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(identity.p2pId)
                val user = Users(
                    profileName = identity.username,
                    profilePhone = identity.p2pId,
                    profileAbout = "Disponible en WAPP P2P",
                    profileId = identity.p2pId
                )
                dbRef.setValue(user)
            } catch (e: Exception) {
                // Fallback local silencioso si no hay conexión a internet
            }

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                flProgressBarLogIn.visibility = View.GONE
                nextPhnBtn.isEnabled = true

                // 3. Abrir la pantalla del Identificador listo
                val intent = Intent(this@LogInActivity, IdentityReadyActivity::class.java)
                intent.putExtra("name", identity.username)
                intent.putExtra("p2pId", identity.p2pId)
                startActivity(intent)
                finish()
            }, 600)
        }
    }
}
