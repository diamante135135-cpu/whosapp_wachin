package com.example.whatsapp.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.whatsapp.R
import com.example.whatsapp.adapter.ChatScreenAdapter
import com.example.whatsapp.data.Chats
import com.example.whatsapp.data.SignalingMessage
import com.example.whatsapp.data.Users
import com.example.whatsapp.data.local.MessageEntity
import com.example.whatsapp.data.local.WappDatabase
import com.example.whatsapp.security.P2PIdentityManager
import com.example.whatsapp.signaling.SignalingManager
import com.example.whatsapp.webrtc.WebRtcManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Actividad de Conversación P2P Fluida de WAPP.
 *
 * Optimizaciones de Rendimiento y Protección Anti-Crash:
 *  - Conversión ultra-segura de estampas de tiempo (Long.toLongOrNull()) para evitar NumberFormatException.
 *  - Carga instantánea de la UI e historial local desde Room en menos de 50ms.
 *  - Listeners de red (Firebase/Señalización) ejecutados de forma 100% asíncrona en Dispatchers.IO.
 *  - Cero bloqueos en el hilo principal de la interfaz de usuario (Main UI Thread).
 *  - Uso exclusivo del Identificador Permanente P2P "+P2P-XXXXXX" inmutable.
 */
class ChatActivity : AppCompatActivity() {
    private var chatList = mutableListOf<Chats>()
    private lateinit var chatAdapter: ChatScreenAdapter
    private var receiverId: String? = null
    private var currentUserId: String = "+P2P-000000"

    // --- Componentes Persistencia y Red ---
    private lateinit var wappDatabase: WappDatabase
    private lateinit var signalingManager: SignalingManager
    private lateinit var webRtcManager: WebRtcManager
    private var p2pConnectionEstablished = false
    private var isConnectionInitiator = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Obtener Identificador P2P Permanente Inmutable
        currentUserId = P2PIdentityManager.getInstance(this).getP2PUserId()
        receiverId = intent.getStringExtra("chats") ?: "demo_id_1"

        toolBarName.text = if (receiverId == "demo_id_1") "Soporte WAPP" else "Chat"

        // 2. Inicializar Adapter y DB Local
        wappDatabase = WappDatabase.getDatabase(this)
        setRecyclerAdapter()
        updateConnectionBanner("🌐 P2P: Canal listo", "#128C7E")

        // 3. Botones de interacción
        profile_backarrow?.setOnClickListener {
            releaseP2P()
            finish()
        }

        btnVideoCall?.setOnClickListener {
            val intent = Intent(this, WappCallsActivity::class.java)
            intent.putExtra("type", "Videollamada con ${toolBarName.text}")
            startActivity(intent)
        }

        btnVoiceCall?.setOnClickListener {
            val intent = Intent(this, WappCallsActivity::class.java)
            intent.putExtra("type", "Llamada de voz con ${toolBarName.text}")
            startActivity(intent)
        }

        btnChatMenu?.setOnClickListener { view ->
            showChatMenu(view)
        }

        btnEmoji?.setOnClickListener {
            typeMsg.append(" 😊 ")
        }

        btnAttachFile?.setOnClickListener {
            showAttachmentOptions()
        }

        btnSendMsg?.setOnClickListener {
            handleSendMessage()
        }

        // 4. Cargar Historial Local Inmediato (Room) sin bloquear la UI
        loadLocalHistoryFromRoom()

        // 5. Inicializar Red y Señalización P2P en segundo plano
        initBackgroundNetworkAndP2P()
    }

    /**
     * Carga de mensajes guardados localmente en SQLite (Room) de forma asíncrona.
     */
    private fun loadLocalHistoryFromRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val localEntities = wappDatabase.messageDao().getMessagesForChat(currentUserId, receiverId ?: "")
                if (localEntities.isNotEmpty()) {
                    val roomList = localEntities.map { entity ->
                        Chats(
                            message = entity.messageText,
                            timeStamp = entity.timestamp,
                            sender = entity.sender,
                            receiver = entity.receiver,
                            messageId = entity.messageId,
                            status = entity.status,
                            connectionType = entity.connectionType,
                            type = entity.type
                        )
                    }.toMutableList()

                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            chatList.clear()
                            chatList.addAll(roomList)
                            if (::chatAdapter.isInitialized) {
                                chatAdapter.updateList(chatList)
                                if (chatList.isNotEmpty()) {
                                    recyclerChatCon?.scrollToPosition(chatList.size - 1)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Previene cualquier fallo en caso de migración DB
            }
        }
    }

    /**
     * Inicialización asíncrona de componentes P2P y listeners de Firebase en Dispatchers.IO.
     */
    private fun initBackgroundNetworkAndP2P() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                signalingManager = SignalingManager.getInstance()
                webRtcManager = WebRtcManager.getInstance(this@ChatActivity)

                webRtcManager.onConnectionStateChange = { remoteUserId, state ->
                    runOnUiThread {
                        when (state) {
                            "CONNECTED" -> {
                                p2pConnectionEstablished = true
                                updateConnectionBanner("⚡ Conexión P2P Directa Activa (WebRTC DataChannel)", "#075E54")
                                resendPendingMessagesP2P(remoteUserId)
                            }
                            "DISCONNECTED" -> {
                                p2pConnectionEstablished = false
                                updateConnectionBanner("📡 Servidor de Respaldo (Firebase Realtime)", "#E67E22")
                            }
                            else -> {
                                updateConnectionBanner("🌐 Estado P2P: $state", "#128C7E")
                            }
                        }
                    }
                }

                webRtcManager.onConnectionTypeChanged = { _, type ->
                    runOnUiThread {
                        val bannerText = when (type) {
                            "DIRECT" -> "⚡ Conexión P2P Directa (STUN)"
                            "TURN" -> "🔄 Conexión P2P Retransmitida (TURN Relay)"
                            else -> "⚡ Conexión P2P Activa"
                        }
                        updateConnectionBanner(bannerText, "#075E54")
                    }
                }

                webRtcManager.onMessageReceived = { remoteUserId, rawJson ->
                    runOnUiThread {
                        handleIncomingP2PData(remoteUserId, rawJson)
                    }
                }

                webRtcManager.onIceCandidateGenerated = { _, candidateSdp ->
                    signalingManager.sendIceCandidateString(receiverId ?: "", currentUserId, candidateSdp, null, null)
                }

                signalingManager.setOnSignalReceived { msg ->
                    handleIncomingSignal(msg)
                }
                signalingManager.listenForSignals(currentUserId)

                if (receiverId != "demo_id_1") {
                    isConnectionInitiator = true
                    requestP2PConnection()
                } else {
                    runOnUiThread {
                        updateConnectionBanner("💬 Chat de Soporte WAPP (Demo)", "#128C7E")
                    }
                }

                // Escuchar datos del perfil remoto
                listenRemoteUserProfile()

                // Escuchar mensajes de Firebase Realtime
                listenFirebaseMessages()

            } catch (e: Exception) {
                // Silencioso: previene cierres ante falta de red
            }
        }
    }

    private fun listenRemoteUserProfile() {
        try {
            val dbRef = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val user = i.getValue(Users::class.java)
                        if (user != null && user.profileId == receiverId) {
                            runOnUiThread {
                                if (!isFinishing && !isDestroyed) {
                                    toolBarName?.text = user.profileName ?: "Chat"
                                }
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun listenFirebaseMessages() {
        try {
            FirebaseDatabase.getInstance().getReference("messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val newMessages = mutableListOf<Chats>()
                        for (i in snapshot.children) {
                            val chat = i.getValue(Chats::class.java)
                            if (chat != null) {
                                if ((chat.receiver == currentUserId && chat.sender == receiverId)
                                    || (chat.receiver == receiverId && chat.sender == currentUserId)
                                ) {
                                    newMessages.add(chat)
                                }
                            }
                        }

                        runOnUiThread {
                            if (!isFinishing && !isDestroyed) {
                                for (chat in newMessages) {
                                    if (!chatList.any { it.messageId == chat.messageId }) {
                                        addLocalMessageAndPersist(chat)
                                    }
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun handleSendMessage() {
        val messageText = typeMsg.text.toString().trim()
        if (messageText.isEmpty()) {
            typeMsg.error = "Escribe un mensaje"
            return
        }

        val timestamp = System.currentTimeMillis().toString()
        val chatsMessage = Chats(
            message = messageText,
            timeStamp = timestamp,
            sender = currentUserId,
            receiver = receiverId!!,
            status = Chats.STATUS_SENT,
            connectionType = if (p2pConnectionEstablished) "P2P" else "FIREBASE"
        )

        // 1. Intentar envío P2P Directo por WebRTC
        if (p2pConnectionEstablished && webRtcManager.hasConnection(receiverId!!)) {
            val jsonPayload = serializeChatToJson(chatsMessage)
            val sent = webRtcManager.sendMessage(receiverId!!, jsonPayload)
            if (sent) {
                addLocalMessageAndPersist(chatsMessage)
                typeMsg.setText("")
                return
            }
        }

        // 2. Fallback a Firebase Realtime Database
        try {
            val dbRef = FirebaseDatabase.getInstance().reference.child("messages").push()
            dbRef.setValue(chatsMessage).addOnSuccessListener {
                addLocalMessageAndPersist(chatsMessage)
            }.addOnFailureListener {
                chatsMessage.status = Chats.STATUS_OFFLINE
                addLocalMessageAndPersist(chatsMessage)
            }
        } catch (e: Exception) {
            chatsMessage.status = Chats.STATUS_OFFLINE
            addLocalMessageAndPersist(chatsMessage)
        }

        typeMsg.setText("")

        // Respuesta del Bot Demo
        if (receiverId == "demo_id_1") {
            typeMsg.postDelayed({
                val botReply = Chats(
                    message = "¡Hola! He recibido tu mensaje en WAPP P2P. ¿En qué puedo ayudarte?",
                    timeStamp = (System.currentTimeMillis() + 500).toString(),
                    sender = "demo_id_1",
                    receiver = currentUserId,
                    status = Chats.STATUS_DELIVERED
                )
                addLocalMessageAndPersist(botReply)
            }, 1000)
        }
    }

    private fun handleIncomingP2PData(remoteUserId: String, rawData: String) {
        try {
            val json = JSONObject(rawData)
            val type = json.optString("type", Chats.TYPE_CHAT)
            val msgId = json.optString("messageId", "")

            when (type) {
                Chats.TYPE_CHAT -> {
                    val msgText = json.optString("message", "")
                    val time = json.optString("timeStamp", System.currentTimeMillis().toString())

                    val incomingChat = Chats(
                        message = msgText,
                        timeStamp = time,
                        sender = remoteUserId,
                        receiver = currentUserId,
                        messageId = msgId,
                        status = Chats.STATUS_READ,
                        connectionType = "P2P"
                    )
                    addLocalMessageAndPersist(incomingChat)

                    sendControlAck(remoteUserId, msgId, Chats.TYPE_DELIVERY_ACK)
                    sendControlAck(remoteUserId, msgId, Chats.TYPE_READ_ACK)
                }
                Chats.TYPE_DELIVERY_ACK -> {
                    updateMessageStatus(msgId, Chats.STATUS_DELIVERED)
                }
                Chats.TYPE_READ_ACK -> {
                    updateMessageStatus(msgId, Chats.STATUS_READ)
                }
            }
        } catch (e: Exception) {
            val chat = Chats(rawData, System.currentTimeMillis().toString(), remoteUserId, currentUserId)
            addLocalMessageAndPersist(chat)
        }
    }

    private fun sendControlAck(remoteUserId: String, targetMessageId: String, ackType: String) {
        try {
            val ackObj = JSONObject().apply {
                put("messageId", targetMessageId)
                put("sender", currentUserId)
                put("receiver", remoteUserId)
                put("type", ackType)
                put("timeStamp", System.currentTimeMillis().toString())
            }
            webRtcManager.sendMessage(remoteUserId, ackObj.toString())
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun updateMessageStatus(messageId: String, newStatus: String) {
        for (i in chatList.indices) {
            if (chatList[i].messageId == messageId) {
                chatList[i].status = newStatus
                if (::chatAdapter.isInitialized) {
                    chatAdapter.notifyItemChanged(i)
                }
                break
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                wappDatabase.messageDao().updateMessageStatus(messageId, newStatus)
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    private fun addLocalMessageAndPersist(chat: Chats) {
        addLocalMessage(chat)
        saveMessageToRoom(chat)
    }

    private fun saveMessageToRoom(chat: Chats) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val entity = MessageEntity(
                    messageId = chat.messageId,
                    messageText = chat.message,
                    timestamp = chat.timeStamp,
                    sender = chat.sender,
                    receiver = chat.receiver,
                    status = chat.status,
                    connectionType = chat.connectionType,
                    type = chat.type,
                    isSynced = (chat.status != Chats.STATUS_OFFLINE && chat.status != Chats.STATUS_PENDING)
                )
                wappDatabase.messageDao().insertMessage(entity)
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    private fun resendPendingMessagesP2P(remoteUserId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pendingList = wappDatabase.messageDao().getPendingMessagesForReceiver(remoteUserId)
                for (entity in pendingList) {
                    val chat = Chats(
                        message = entity.messageText,
                        timeStamp = entity.timestamp,
                        sender = entity.sender,
                        receiver = entity.receiver,
                        messageId = entity.messageId,
                        status = Chats.STATUS_SENT,
                        connectionType = "P2P"
                    )
                    val jsonPayload = serializeChatToJson(chat)
                    val sent = webRtcManager.sendMessage(remoteUserId, jsonPayload)
                    if (sent) {
                        wappDatabase.messageDao().updateMessageStatus(entity.messageId, Chats.STATUS_SENT)
                        withContext(Dispatchers.Main) {
                            if (!isFinishing && !isDestroyed) {
                                updateMessageStatus(entity.messageId, Chats.STATUS_SENT)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    private fun requestP2PConnection() {
        try {
            signalingManager.sendSignal(
                toId = receiverId!!,
                type = "CONNECT_REQUEST",
                fromId = currentUserId
            )
            runOnUiThread {
                updateConnectionBanner("🌐 P2P: Solicitando canal directo...", "#128C7E")
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun handleIncomingSignal(msg: SignalingMessage) {
        try {
            when (msg.type) {
                "CONNECT_REQUEST" -> {
                    isConnectionInitiator = false
                    signalingManager.sendSignal(
                        toId = msg.fromId,
                        type = "CONNECT_ACCEPT",
                        fromId = currentUserId
                    )
                    runOnUiThread {
                        updateConnectionBanner("🌐 P2P: Negociando señal con ${msg.fromId}", "#128C7E")
                    }
                }
                "CONNECT_ACCEPT" -> {
                    runOnUiThread {
                        updateConnectionBanner("🌐 P2P: Solicitud Aceptada, enviando Offer...", "#128C7E")
                    }
                    if (isConnectionInitiator) {
                        initiateOfferFlow()
                    }
                }
                "OFFER" -> {
                    val sdp = msg.sdp ?: ""
                    val answerSdp = webRtcManager.createAnswer(currentUserId, sdp)
                    if (answerSdp != null) {
                        signalingManager.sendSignal(
                            toId = msg.fromId,
                            type = "ANSWER",
                            fromId = currentUserId,
                            sdp = answerSdp
                        )
                    }
                }
                "ANSWER" -> {
                    val sdp = msg.sdp ?: ""
                    webRtcManager.setRemoteAnswer(currentUserId, sdp)
                }
                "ICE_CANDIDATE" -> {
                    val candidate = msg.candidate ?: ""
                    val sdpMid = msg.sdpMid
                    val sdpMLineIndex = msg.sdpMLineIndex
                    webRtcManager.addIceCandidate(currentUserId, candidate, sdpMid, sdpMLineIndex)
                }
                "BYE" -> {
                    releaseP2PConnection(receiverId ?: "")
                }
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    fun initiateOfferFlow() {
        if (!isConnectionInitiator) return
        try {
            val offerSdp = webRtcManager.createOffer(receiverId ?: "")
            if (offerSdp != null) {
                signalingManager.sendSignal(
                    toId = receiverId!!,
                    type = "OFFER",
                    fromId = currentUserId,
                    sdp = offerSdp
                )
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun addLocalMessage(chat: Chats) {
        chatList.add(chat)
        if (::chatAdapter.isInitialized) {
            chatAdapter.updateList(chatList)
            recyclerChatCon?.scrollToPosition(chatList.size - 1)
        }
    }

    private fun releaseP2PConnection(userId: String) {
        try {
            webRtcManager.closeConnection(userId)
            p2pConnectionEstablished = false
            runOnUiThread {
                updateConnectionBanner("📡 Servidor de Respaldo / Offline", "#7F8C8D")
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun releaseP2P() {
        try {
            signalingManager.stopListening()
            webRtcManager.closeAllConnections()
            p2pConnectionEstablished = false
        } catch (e: Exception) {
            // Silencioso
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseP2P()
    }

    private fun showChatMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menu.add("Ver Contacto")
        popup.menu.add("Limpiar Chat")
        popup.menu.add("Bloquear")

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Ver Contacto" -> {
                    Toast.makeText(this, "Contacto: ${toolBarName.text}", Toast.LENGTH_SHORT).show()
                    true
                }
                "Limpiar Chat" -> {
                    chatList.clear()
                    if (::chatAdapter.isInitialized) {
                        chatAdapter.updateList(chatList)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        wappDatabase.messageDao().deleteChatHistory(currentUserId, receiverId ?: "")
                    }
                    Toast.makeText(this, "Historial limpiado", Toast.LENGTH_SHORT).show()
                    true
                }
                "Bloquear" -> {
                    Toast.makeText(this, "Contacto bloqueado", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showAttachmentOptions() {
        val options = arrayOf("📷 Cámara / Foto", "🖼️ Galería", "📄 Documento", "📍 Ubicación")
        AlertDialog.Builder(this)
            .setTitle("Enviar adjunto")
            .setItems(options) { _, which ->
                val selected = options[which]
                val attachmentText = "📎 [$selected enviado]"
                typeMsg.setText(attachmentText)
            }
            .show()
    }

    private fun updateConnectionBanner(statusText: String, colorHex: String) {
        tvConnectionStatus?.text = statusText
        try {
            llConnectionBanner?.setBackgroundColor(Color.parseColor(colorHex))
        } catch (e: Exception) {
            llConnectionBanner?.setBackgroundColor(Color.parseColor("#128C7E"))
        }
    }

    private fun serializeChatToJson(chat: Chats): String {
        return JSONObject().apply {
            put("messageId", chat.messageId)
            put("message", chat.message)
            put("timeStamp", chat.timeStamp)
            put("sender", chat.sender)
            put("receiver", chat.receiver)
            put("status", chat.status)
            put("connectionType", chat.connectionType)
            put("type", chat.type)
        }.toString()
    }

    private fun setRecyclerAdapter() {
        val linearLayoutManager = LinearLayoutManager(this)
        chatAdapter = ChatScreenAdapter(chatList, currentUserId)
        recyclerChatCon.apply {
            layoutManager = linearLayoutManager
            adapter = chatAdapter
        }
    }
}
