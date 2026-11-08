package com.example.whatsapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.whatsapp.R
import com.example.whatsapp.security.P2PIdentityManager
import kotlinx.android.synthetic.main.activity_identity_ready.*

class IdentityReadyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identity_ready)

        val identityManager = P2PIdentityManager.getInstance(this)
        val identity = identityManager.getIdentity()

        val name = intent.getStringExtra("name") ?: identity?.username ?: "Usuario"
        val p2pId = intent.getStringExtra("p2pId") ?: identity?.p2pId ?: "+P2P-829451"

        tvUserNameDisplay.text = "Nombre: $name"
        tvP2pIdDisplay.text = "Identificador: $p2pId"

        btnCopyId.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("WAPP P2P ID", p2pId)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Identificador $p2pId copiado al portapapeles", Toast.LENGTH_SHORT).show()
        }

        btnShareId.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Mi Identificador WAPP P2P")
                putExtra(Intent.EXTRA_TEXT, "¡Agregame en WAPP! Mi identificador P2P personal es: $p2pId")
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir mi Identificador P2P"))
        }

        btnEnterApp.setOnClickListener {
            val homeIntent = Intent(this@IdentityReadyActivity, HomeActivity::class.java)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(homeIntent)
            finish()
        }
    }
}
