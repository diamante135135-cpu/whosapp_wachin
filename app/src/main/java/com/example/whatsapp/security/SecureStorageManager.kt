package com.example.whatsapp.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de Almacenamiento Seguro Resiliente.
 *
 * Captura cualquier excepción producida por Android KeyStore (KeyStoreException,
 * AEADBadTagException, GeneralSecurityException) en dispositivos con chips de seguridad
 * incompletos o corruptos, recurriendo a SharedPreferences locales de respaldo de forma
 * completamente transparente sin provocar jamás cierres inesperados (crashes).
 */
class SecureStorageManager(context: Context) {
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                "wapp_secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback seguro si Android KeyStore falla en el dispositivo
            context.applicationContext.getSharedPreferences("wapp_fallback_secure_prefs", Context.MODE_PRIVATE)
        }
    }

    fun saveString(key: String, value: String) {
        try {
            sharedPreferences.edit().putString(key, value).apply()
        } catch (e: Exception) {
            // Silencioso
        }
    }

    fun getString(key: String, defaultValue: String?): String? {
        return try {
            sharedPreferences.getString(key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }

    fun saveBoolean(key: String, value: Boolean) {
        try {
            sharedPreferences.edit().putBoolean(key, value).apply()
        } catch (e: Exception) {
            // Silencioso
        }
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return try {
            sharedPreferences.getBoolean(key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }
}
