package com.example.whatsapp.data

/**
 * Mensaje de señalización WebRTC intercambiado entre dos dispositivos.
 * Se transporta mediante Firebase Realtime Database únicamente para negociar
 * la conexión P2P. Los mensajes de chat NO viajan por este canal una vez
 * establecida la conexión directa.
 */
data class SignalingMessage(
    val type: String, // OFFER, ANSWER, ICE_CANDIDATE, CONNECT_REQUEST, CONNECT_ACCEPT, CONNECT_REJECT, BYE
    val fromId: String,
    val toId: String,
    val sdp: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val messageId: String = java.util.UUID.randomUUID().toString()
) {
    constructor() : this("CONNECT_REQUEST", "", "", null, null, null, null, 0, "")
}
