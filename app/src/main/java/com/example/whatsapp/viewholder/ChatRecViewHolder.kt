package com.example.whatsapp.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.data.Chats
import kotlinx.android.synthetic.main.reciver_chat_layout.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ChatRecViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    fun setData(chats: Chats) {
        view.apply {
            tvRecMsg.text = chats.message

            // Formatear hora del mensaje de forma 100% segura contra NumberFormatException
            try {
                val timeLong = chats.timeStamp.toLongOrNull() ?: System.currentTimeMillis()
                val date = Date(timeLong)
                val formatter: DateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                tvRecTime.text = formatter.format(date)
            } catch (e: Exception) {
                tvRecTime.text = ""
            }
        }
    }
}
