package com.example.whatsapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp.R
import com.example.whatsapp.data.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_user_data.*

class UserDataActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_data)

        auth = Firebase.auth
        val userPhone = intent.getStringExtra("phone") ?: auth.currentUser?.phoneNumber ?: "+54 11 1234 5678"
        profileNumber.text = userPhone

        nextHomeBtn.setOnClickListener {
            val name = profileNameUser.text.toString().trim()
            val about = profileAbout.text.toString().trim()

            if (name.isEmpty()) {
                profileNameUser.error = "Ingresa tu nombre"
                return@setOnClickListener
            }

            val finalAbout = if (about.isEmpty()) "¡Disponible en WAPP!" else about

            saveUserProfileAndProceed(name, userPhone, finalAbout)
        }
    }

    private fun saveUserProfileAndProceed(name: String, phone: String, about: String) {
        val uid = auth.currentUser?.uid ?: "user_${System.currentTimeMillis()}"

        // 1. Guardar en SharedPreferences
        val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("username", name)
            .putString("user_phone", phone)
            .putString("user_about", about)
            .putString("device_user_id", uid)
            .putBoolean("is_logged_in", true)
            .apply()

        // 2. Guardar en Firebase Realtime Database
        try {
            val user = Users(name, phone, about, uid)
            FirebaseDatabase.getInstance().getReference("Users").child(uid).setValue(user)
        } catch (e: Exception) {
            // Guardado local de respaldo
        }

        Toast.makeText(this, "Perfil creado con éxito", Toast.LENGTH_SHORT).show()

        // 3. Ir a HomeActivity
        val intent = Intent(this@UserDataActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
