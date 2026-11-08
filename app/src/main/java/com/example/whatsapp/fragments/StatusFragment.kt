package com.example.whatsapp.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.whatsapp.R

class StatusFragment : Fragment() {

    private var myStatusText: String = "Toca para añadir una actualización"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_status, container, false)

        view.setOnClickListener {
            showUpdateStatusDialog()
        }

        return view
    }

    private fun showUpdateStatusDialog() {
        val context = requireContext()
        val etStatus = EditText(context).apply {
            hint = "Escribe tu estado..."
            inputType = InputType.TYPE_CLASS_TEXT
            setText(if (myStatusText.contains("Toca")) "" else myStatusText)
        }

        AlertDialog.Builder(context)
            .setTitle("Actualizar mi estado")
            .setView(etStatus)
            .setPositiveButton("Publicar") { _, _ ->
                val newStatus = etStatus.text.toString().trim()
                if (newStatus.isNotEmpty()) {
                    myStatusText = newStatus
                    Toast.makeText(context, "Estado publicado: \"$newStatus\"", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Ver historias") { _, _ ->
                Toast.makeText(context, "Viendo estados recientes de contactos...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
