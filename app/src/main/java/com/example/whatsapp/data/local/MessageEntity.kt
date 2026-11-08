package com.example.whatsapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para persistencia local de mensajes P2P y conversaciones en WAPP.
 * Guarda identificador único de mensaje, contenido, remitente, destinatario, marcas de tiempo,
 * estados de entrega (PENDING, SENT, DELIVERED, READ, ERROR, OFFLINE) y tipo de conexión usada.
 */
@Entity(tableName = "offline_messages")
data class MessageEntity(
    @PrimaryKey
    val messageId: String,
    val messageText: String,
    val timestamp: String,
    val sender: String,
    val receiver: String,
    val status: String = "SENT", // PENDING, SENT, DELIVERED, READ, ERROR, OFFLINE
    val connectionType: String = "LOCAL", // P2P_DIRECT, P2P_TURN, FIREBASE, LOCAL
    val type: String = "CHAT", // CHAT, DELIVERY_ACK, READ_ACK
    val isSynced: Boolean = true
)
