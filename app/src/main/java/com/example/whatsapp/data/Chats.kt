package com.example.whatsapp.data

import java.util.UUID

/**
 * Modelo de datos unificado para mensajes de conversación en WAPP.
 *
 * Soporta transporte P2P (WebRTC DataChannel), señalización y fallback vía Firebase.
 * Incluye identificador único, tipo de mensaje (CHAT, DELIVERY_ACK, READ_ACK),
 * estado de entrega (PENDING, SENT, DELIVERED, READ, ERROR, OFFLINE)
 * y tipo de conexión utilizada (P2P_DIRECT, P2P_TURN, FIREBASE, LOCAL).
 */
data class Chats(
    val message: String = "",
    val timeStamp: String = System.currentTimeMillis().toString(),
    val sender: String = "",
    val receiver: String = "",
    val messageId: String = UUID.randomUUID().toString(),
    var status: String = "SENT", // PENDING, SENT, DELIVERED, READ, ERROR, OFFLINE
    var connectionType: String = "LOCAL", // P2P_DIRECT, P2P_TURN, FIREBASE, LOCAL
    val type: String = "CHAT" // CHAT, DELIVERY_ACK, READ_ACK
) {
    // Constructor secundario de compatibilidad para Firebase Realtime Database
    constructor() : this("", "", "", "", "", "SENT", "LOCAL", "CHAT")

    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_DELIVERED = "DELIVERED"
        const val STATUS_READ = "READ"
        const val STATUS_ERROR = "ERROR"
        const val STATUS_OFFLINE = "OFFLINE"

        const val TYPE_CHAT = "CHAT"
        const val TYPE_DELIVERY_ACK = "DELIVERY_ACK"
        const val TYPE_READ_ACK = "READ_ACK"
    }
}
