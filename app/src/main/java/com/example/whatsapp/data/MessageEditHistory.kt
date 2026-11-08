package com.example.whatsapp.data

data class MessageEditHistory(
    val messageId: String,
    val oldText: String,
    val newText: String,
    val editedTimestamp: String
)
