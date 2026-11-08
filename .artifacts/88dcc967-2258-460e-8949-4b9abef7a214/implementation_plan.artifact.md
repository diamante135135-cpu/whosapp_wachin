# Plan Completo de Auditoría Profunda por Fases y Descifrado de Errores

Plan de revisión de ingeniería para auditar los **52 archivos Kotlin y recursos XML** del proyecto WAPP, analizando exhaustivamente cada componente para garantizar fluidez absoluta y cero cierres.

---

## 🎯 Fases del Plan de Auditoría Detallada

### **FASE 1: Auditoría de Declaraciones, Manifest y Transición de Actividades**
- **Archivos a inspeccionar:** `AndroidManifest.xml`, `SplashScreenActivity.kt`, `LogInActivity.kt`, `IdentityReadyActivity.kt`, `HomeActivity.kt`, `ChatActivity.kt`.
- **Análisis de errores:**
  - Comprobación de todas las entradas `<activity>` en `AndroidManifest.xml`.
  - Inspección de la transmisión de `Intent.putExtra` (`"chats"`, `"phone"`, `"p2pId"`).
  - Verificación de que ninguna actividad intente interactuar con componentes de interfaz antes de ejecutar `setContentView()`.
  - Garantizar que los métodos `finish()` y de navegación no produzcan fugas de memoria o cierres en la pila de actividades.

### **FASE 2: Auditoría de Vistas, Layouts XML, Adaptadores y RecyclerView**
- **Archivos a inspeccionar:** `ChatScreenAdapter.kt`, `ChatSendViewHolder.kt`, `ChatRecViewHolder.kt`, `ChatsAdapter.kt`, `ChatsViewHolder.kt`, `activity_chat.xml`, `sender_chat_layout.xml`, `reciver_chat_layout.xml`.
- **Análisis de errores:**
  - Verificación de compatibilidad de todos los IDs y drawables utilizados en los layouts XML.
  - Comprobación de límites en las colecciones de datos (`position in chatsList.indices`).
  - Verificación de seguridad en clics de listas (`adapterPosition != RecyclerView.NO_POSITION`).
  - Conversión de estampas de tiempo mediante `toLongOrNull()` con resguardo contra `NumberFormatException`.

### **FASE 3: Auditoría de Persistencia Local (SQLite Room, KeyStore y SharedPreferences)**
- **Archivos a inspeccionar:** `WappDatabase.kt`, `MessageDao.kt`, `MessageEntity.kt`, `SecureStorageManager.kt`, `P2PIdentityManager.kt`.
- **Análisis de errores:**
  - Inspección de consultas SQL en `MessageDao.kt` para asegurar que soporten valores nulos sin quebrarse.
  - Verificación de que la versión 2 de `WappDatabase` aplique `fallbackToDestructiveMigration()` de forma limpia.
  - Garantía de que `SecureStorageManager` capture fallos de Android KeyStore (`KeyStoreException`) y conmute a almacenamiento de respaldo sin lanzar excepciones.

### **FASE 4: Auditoría de Hilos, Corrutinas, Red, Señalización P2P y Compilación Final**
- **Archivos a inspeccionar:** `SignalingManager.kt`, `WebRtcManager.kt`, `WappMessagingService.kt`.
- **Análisis de errores:**
  - Verificación de que los `ValueEventListener` de Firebase y callbacks P2P se ejecuten fuera del hilo de interfaz gráfica (`Main UI Thread`).
  - Garantía de que la UI se actualice dentro de `runOnUiThread` protegiéndose con `!isFinishing && !isDestroyed`.
  - Ejecución de la compilación completa `./gradlew.bat assembleDebug` y generación del informe final con el APK verificado.

---

## 🧪 Plan de Verificación

1. Comprobar uno por uno todos los puntos de la auditoría en los 52 archivos.
2. Compilar el APK de depuración y comprobar que no existan errores de síntesis ni de ejecución.
3. Entregar el informe definitivo con las conclusiones y el APK verificado.
