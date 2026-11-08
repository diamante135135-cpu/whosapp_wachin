package com.example.whatsapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.R
import com.example.whatsapp.activities.WappCallsActivity
import com.example.whatsapp.adapter.CallsAdapter
import com.example.whatsapp.data.CallsModel

class CallsFragment : Fragment() {
    private val modelList = ArrayList<CallsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calls, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerCalls = view.findViewById<RecyclerView>(R.id.recyclerCalls)
        val tvNoCalls = view.findViewById<TextView>(R.id.tvNoCalls)

        // Cargar registros de llamadas de demostración con recurso de imagen válido
        if (modelList.isEmpty()) {
            modelList.add(CallsModel(R.drawable.chat_call, "Soporte WAPP (Hoy, 14:30)"))
            modelList.add(CallsModel(R.drawable.video, "Equipo WAPP P2P (Ayer, 18:15)"))
        }

        if (modelList.isEmpty()) {
            recyclerCalls?.visibility = View.GONE
            tvNoCalls?.visibility = View.VISIBLE
        } else {
            recyclerCalls?.visibility = View.VISIBLE
            tvNoCalls?.visibility = View.GONE

            val itemAdapter = CallsAdapter(modelList)
            recyclerCalls?.layoutManager = LinearLayoutManager(this.context)
            recyclerCalls?.adapter = itemAdapter
        }

        view.setOnClickListener {
            val intent = Intent(requireContext(), WappCallsActivity::class.java)
            startActivity(intent)
        }
    }
}
