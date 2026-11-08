package com.example.whatsapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM offline_messages WHERE (sender = :user1 AND receiver = :user2) OR (sender = :user2 AND receiver = :user1) ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(user1: String, user2: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE offline_messages SET status = :status WHERE messageId = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("SELECT * FROM offline_messages WHERE receiver = :receiverId AND (status = 'PENDING' OR status = 'OFFLINE') ORDER BY timestamp ASC")
    suspend fun getPendingMessagesForReceiver(receiverId: String): List<MessageEntity>

    @Query("SELECT * FROM offline_messages WHERE messageText LIKE '%' || :query || '%'")
    suspend fun searchMessages(query: String): List<MessageEntity>

    @Query("DELETE FROM offline_messages WHERE (sender = :user1 AND receiver = :user2) OR (sender = :user2 AND receiver = :user1)")
    suspend fun deleteChatHistory(user1: String, user2: String)
}
