package com.example.whatsapp.viewholder

import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.data.Chats
import kotlinx.android.synthetic.main.sender_chat_layout.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChatSendViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    fun setData(chats: Chats) {
        view.apply {
            tvChatMessage.text = chats.message

            // Formatear hora del mensaje con guardagujas contra NumberFormatException
            try {
                val timeLong = chats.timeStamp.toLongOrNull() ?: System.currentTimeMillis()
                val date = Date(timeLong)
                val formatter: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvTime.text = formatter.format(date)
            } catch (e: Exception) {
                tvTime.text = ""
            }

            // Formatear estado del mensaje (Regla: no mostrar entregado si no hay ACK real)
            when (chats.status) {
                Chats.STATUS_PENDING, Chats.STATUS_OFFLINE -> {
                    tvMsgStatus.text = "🕒"
                    tvMsgStatus.setTextColor(Color.GRAY)
                }
                Chats.STATUS_SENT -> {
                    tvMsgStatus.text = "✓"
                    tvMsgStatus.setTextColor(Color.GRAY)
                }
                Chats.STATUS_DELIVERED -> {
                    tvMsgStatus.text = "✓✓"
                    tvMsgStatus.setTextColor(Color.GRAY)
                }
                Chats.STATUS_READ -> {
                    tvMsgStatus.text = "✓✓"
                    tvMsgStatus.setTextColor(Color.parseColor("#34B7F1")) // Azul WhatsApp
                }
                Chats.STATUS_ERROR -> {
                    tvMsgStatus.text = "⚠️"
                    tvMsgStatus.setTextColor(Color.RED)
                }
                else -> {
                    tvMsgStatus.text = "✓"
                    tvMsgStatus.setTextColor(Color.GRAY)
                }
            }
        }
    }
}
