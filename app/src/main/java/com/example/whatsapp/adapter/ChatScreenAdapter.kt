package com.example.whatsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsapp.R
import com.example.whatsapp.data.Chats
import com.example.whatsapp.security.P2PIdentityManager
import com.example.whatsapp.viewholder.ChatRecViewHolder
import com.example.whatsapp.viewholder.ChatSendViewHolder

class ChatScreenAdapter(
    private var chatsList: List<Chats>,
    private var currentUserId: String = ""
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_TYPE_SENT = 1
    private val ITEM_TYPE_RECEIVED = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (currentUserId.isEmpty()) {
            currentUserId = P2PIdentityManager.getInstance(parent.context).getP2PUserId()
        }

        return when (viewType) {
            ITEM_TYPE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.sender_chat_layout, parent, false)
                ChatSendViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.reciver_chat_layout, parent, false)
                ChatRecViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position in chatsList.indices) {
            val chats = chatsList[position]
            if (holder is ChatSendViewHolder) {
                holder.setData(chats)
            } else if (holder is ChatRecViewHolder) {
                holder.setData(chats)
            }
        }
    }

    override fun getItemCount(): Int {
        return chatsList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position !in chatsList.indices) return ITEM_TYPE_RECEIVED

        val senderId = chatsList[position].sender
        return if (senderId == currentUserId || (currentUserId.isEmpty() && senderId != "demo_id_1" && senderId != "demo_id_2")) {
            ITEM_TYPE_SENT
        } else {
            ITEM_TYPE_RECEIVED
        }
    }

    fun updateList(newChatsList: List<Chats>) {
        this.chatsList = newChatsList
        notifyDataSetChanged()
    }
}
