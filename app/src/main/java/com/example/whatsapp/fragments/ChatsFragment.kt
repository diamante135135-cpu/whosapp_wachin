package com.example.whatsapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp.R
import com.example.whatsapp.RecyclerViewItemClicked
import com.example.whatsapp.activities.ChatActivity
import com.example.whatsapp.adapter.ChatsAdapter
import com.example.whatsapp.data.Users
import com.example.whatsapp.security.P2PIdentityManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_chats.*

class ChatsFragment : Fragment(), RecyclerViewItemClicked {
    private lateinit var databaseReference: DatabaseReference
    private var userList = mutableListOf<Users>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fabNewChat?.setOnClickListener {
            showAddContactDialog()
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                val currentUid = try {
                    P2PIdentityManager.getInstance(requireContext()).getP2PUserId()
                } catch (e: Exception) {
                    "local_user"
                }

                for (i in snapshot.children) {
                    val user = i.getValue(Users::class.java)
                    if (user != null && user.profileId != currentUid) {
                        userList.add(user)
                    }
                }

                ensureDefaultContacts()
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                ensureDefaultContacts()
                setAdapter()
            }
        })

        ensureDefaultContacts()
        setAdapter()
    }

    private fun ensureDefaultContacts() {
        if (!userList.any { it.profileId == "demo_id_1" }) {
            userList.add(0, Users("Soporte WAPP", "11000000", "Disponible en WAPP", "demo_id_1"))
        }
        if (!userList.any { it.profileId == "demo_id_2" }) {
            userList.add(Users("Equipo WAPP P2P", "11888888", "Conectado vía WebRTC", "demo_id_2"))
        }
    }

    private fun showAddContactDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }

        val etName = EditText(context).apply {
            hint = "Nombre para agendar (ej: Tiziano)"
            inputType = InputType.TYPE_CLASS_TEXT
        }
        val etP2pId = EditText(context).apply {
            hint = "Identificador (+P2P-XXXXXX)"
            inputType = InputType.TYPE_CLASS_TEXT
            setText("+P2P-")
        }

        layout.addView(etName)
        layout.addView(etP2pId)

        AlertDialog.Builder(context)
            .setTitle("Agregar y Agendar Contacto P2P")
            .setView(layout)
            .setPositiveButton("Agendar y Conectar") { _, _ ->
                val name = etName.text.toString().trim()
                val p2pId = etP2pId.text.toString().trim()

                if (name.isNotEmpty() && p2pId.startsWith("+P2P-")) {
                    val newUser = Users(
                        profileName = name,
                        profilePhone = p2pId,
                        profileAbout = "Contacto P2P Agendado",
                        profileId = p2pId
                    )

                    try {
                        FirebaseDatabase.getInstance().getReference("Users").child(p2pId).setValue(newUser)
                    } catch (e: Exception) {
                        // Silencioso
                    }

                    if (!userList.any { it.profileId == p2pId }) {
                        userList.add(newUser)
                        setAdapter()
                    }

                    Toast.makeText(context, "Contacto '$name' agendado con éxito", Toast.LENGTH_SHORT).show()

                    val intent = Intent(activity, ChatActivity::class.java)
                    intent.putExtra("chats", p2pId)
                    startActivity(intent)
                } else {
                    Toast.makeText(context, "Verifica que el nombre no esté vacío y el ID comience con +P2P-", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setAdapter() {
        if (isAdded && view != null) {
            val linearLayoutManager = LinearLayoutManager(activity)
            val userAdapter = ChatsAdapter(userList, this)
            recyclerChats?.apply {
                layoutManager = linearLayoutManager
                adapter = userAdapter
            }
        }
    }

    override fun onItemClicked(position: Int, data: Any) {
        if (data is Users) {
            val intent = Intent(activity, ChatActivity::class.java)
            intent.putExtra("chats", data.profileId)
            startActivity(intent)
        }
    }
}
