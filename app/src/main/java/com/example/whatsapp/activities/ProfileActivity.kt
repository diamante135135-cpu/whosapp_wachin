package com.example.whatsapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp.R
import com.example.whatsapp.data.Users
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity(), View.OnClickListener {
    private val CAMERA_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        loadProfileData()

        camera?.setOnClickListener(this)
        profile_name_edit?.setOnClickListener(this)
        profile_aboutedit?.setOnClickListener(this)
        profilr_phoneEdit?.setOnClickListener(this)
        btn_profile_backarrow?.setOnClickListener(this)
        image_profile?.setOnClickListener(this)
    }

    private fun loadProfileData() {
        val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("username", "Usuario WAPP") ?: "Usuario WAPP"
        val about = prefs.getString("user_about", "Disponible en WAPP") ?: "Disponible en WAPP"
        val phone = prefs.getString("user_phone", "+54 11 1234 5678") ?: "+54 11 1234 5678"

        profile_name?.setText(name)
        profilr_about?.setText(about)
        profilr_phonenumber?.setText(phone)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.camera -> {
                val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
                } catch (e: Exception) {
                    Toast.makeText(this, "Abre la galería o foto de perfil", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.profile_name_edit -> {
                showEditDialog("Editar Nombre", profile_name?.text.toString()) { newName ->
                    profile_name?.setText(newName)
                    saveProfileToPrefs("username", newName)
                }
            }
            R.id.profile_aboutedit -> {
                showEditDialog("Editar Info", profilr_about?.text.toString()) { newAbout ->
                    profilr_about?.setText(newAbout)
                    saveProfileToPrefs("user_about", newAbout)
                }
            }
            R.id.profilr_phoneEdit -> {
                showEditDialog("Editar Teléfono", profilr_phonenumber?.text.toString()) { newPhone ->
                    profilr_phonenumber?.setText(newPhone)
                    saveProfileToPrefs("user_phone", newPhone)
                }
            }
            R.id.btn_profile_backarrow -> {
                finish()
            }
        }
    }

    private fun showEditDialog(title: String, currentValue: String, onSave: (String) -> Unit) {
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(currentValue)
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(et)
            .setPositiveButton("Guardar") { _, _ ->
                val text = et.text.toString().trim()
                if (text.isNotEmpty()) {
                    onSave(text)
                    Toast.makeText(this, "Guardado correctamente", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveProfileToPrefs(key: String, value: String) {
        val prefs = getSharedPreferences("whatsapp_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()

        val uid = Firebase.auth.currentUser?.uid ?: prefs.getString("device_user_id", "local_user") ?: "local_user"
        val name = prefs.getString("username", "Usuario") ?: "Usuario"
        val about = prefs.getString("user_about", "Disponible") ?: "Disponible"
        val phone = prefs.getString("user_phone", "+54110000") ?: "+54110000"

        try {
            val user = Users(name, phone, about, uid)
            FirebaseDatabase.getInstance().getReference("Users").child(uid).setValue(user)
        } catch (e: Exception) {
            // Silencioso
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val bitmap = data.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                image_profile?.setImageBitmap(bitmap)
                Toast.makeText(this, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
