package com.example.whatsapp.automation

import android.content.Context
import java.util.Calendar

class AutomationEngine(private val context: Context) {

    fun shouldAutoReply(sender: String, messageText: String): String? {
        val prefs = context.getSharedPreferences("wapp_automation_prefs", Context.MODE_PRIVATE)
        val isAutoReplyEnabled = prefs.getBoolean("auto_reply_enabled", false)
        val customMessage = prefs.getString("auto_reply_message", "Hola, estoy ocupado en este momento. Te responderé pronto.")

        if (!isAutoReplyEnabled) return null

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Regla determinista: Horario laboral o fuera de horario
        if (hour < 8 || hour > 20) {
            return customMessage
        }

        if (messageText.contains("urgente", ignoreCase = true)) {
            return "He recibido tu mensaje urgente. Me pondré en contacto contigo en breve."
        }

        return null
    }
}
