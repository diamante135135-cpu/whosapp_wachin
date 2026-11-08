package com.example.whatsapp.signaling

import com.example.whatsapp.data.SignalingMessage
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject

/**
 * Gestor de señalización WebRTC.
 *
 * Responsabilidades:
 *  - Enviar ofertas SDP (OFFER), respuestas (ANSWER) y candidatos ICE.
 *  - Escuchar señales entrantes dirigidas al usuario actual.
 *  - Utilizar Firebase Realtime Database exclusivamente como canal de señalización.
 *  - Nunca transportar mensajes de chat por este nodo.
 *
 * Serialización: se utiliza org.json (incluido en Android) para evitar
 * dependencias externas como Gson.
 */
class SignalingManager private constructor() {

    private val database = FirebaseDatabase.getInstance()
    private val signalingRef = database.reference.child("signaling")

    private var onSignalReceived: ((SignalingMessage) -> Unit)? = null
    private var currentListener: ValueEventListener? = null

    companion object {
        @Volatile
        private var instance: SignalingManager? = null

        fun getInstance(): SignalingManager {
            return instance ?: synchronized(this) {
                instance ?: SignalingManager().also { instance = it }
            }
        }
    }

    fun setOnSignalReceived(callback: (SignalingMessage) -> Unit) {
        onSignalReceived = callback
    }

    fun listenForSignals(userId: String) {
        stopListening()
        currentListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (convSnapshot in snapshot.children) {
                    for (msgSnapshot in convSnapshot.children) {
                        val json = msgSnapshot.getValue(String::class.java) ?: continue
                        try {
                            val msg = parseSignalingMessage(json)
                            if (msg != null && msg.toId == userId) {
                                onSignalReceived?.invoke(msg)
                            }
                        } catch (e: Exception) {
                            // Ignorar mensajes malformados
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        signalingRef.child(userId).addValueEventListener(currentListener!!)
    }

    fun stopListening() {
        currentListener?.let {
            signalingRef.removeEventListener(it)
        }
        currentListener = null
    }

    fun sendSignal(
        toId: String,
        type: String,
        fromId: String,
        sdp: String? = null,
        candidate: String? = null,
        sdpMid: String? = null,
        sdpMLineIndex: Int? = null
    ) {
        val msg = SignalingMessage(type, fromId, toId, sdp, candidate, sdpMid, sdpMLineIndex)
        val json = toJson(msg)

        signalingRef.child(fromId).child(toId).push().setValue(json)
        signalingRef.child(toId).child(fromId).push().setValue(json)
    }

    /**
     * Envía un candidato ICE local al destinatario (representado como String).
     */
    fun sendIceCandidateString(
        toId: String,
        fromId: String,
        candidateSdp: String,
        sdpMid: String?,
        sdpMLineIndex: Int?
    ) {
        sendSignal(
            toId = toId,
            type = "ICE_CANDIDATE",
            fromId = fromId,
            candidate = candidateSdp,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        )
    }

    fun release() {
        stopListening()
    }

    // -----------------------------------------------------------------------
    // Serialización JSON manual (sin Gson)
    // -----------------------------------------------------------------------

    private fun toJson(msg: SignalingMessage): String {
        return JSONObject().apply {
            put("type", msg.type)
            put("fromId", msg.fromId)
            put("toId", msg.toId)
            put("sdp", msg.sdp)
            put("candidate", msg.candidate)
            put("sdpMid", msg.sdpMid)
            put("sdpMLineIndex", msg.sdpMLineIndex)
            put("timestamp", msg.timestamp)
            put("messageId", msg.messageId)
        }.toString()
    }

    private fun parseSignalingMessage(json: String): SignalingMessage? {
        return try {
            val obj = JSONObject(json)
            SignalingMessage(
                type = obj.optString("type", ""),
                fromId = obj.optString("fromId", ""),
                toId = obj.optString("toId", ""),
                sdp = if (obj.has("sdp") && !obj.isNull("sdp")) obj.optString("sdp") else null,
                candidate = if (obj.has("candidate") && !obj.isNull("candidate")) obj.optString("candidate") else null,
                sdpMid = if (obj.has("sdpMid") && !obj.isNull("sdpMid")) obj.optString("sdpMid") else null,
                sdpMLineIndex = if (obj.has("sdpMLineIndex") && !obj.isNull("sdpMLineIndex")) obj.optInt("sdpMLineIndex") else null,
                timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                messageId = obj.optString("messageId", java.util.UUID.randomUUID().toString())
            )
        } catch (e: JSONException) {
            null
        }
    }
}
