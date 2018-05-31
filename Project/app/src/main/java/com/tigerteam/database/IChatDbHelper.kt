package com.tigerteam.database

import com.tigerteam.database.DbObjects.*
import java.util.*

/**
 * Zugriffsfunktionen auf die Chat-Datenbank
 */
interface IChatDbHelper {

    // Parameters

    /**
     * Parameter zu anhand des Namens lesen.
     */
    fun getParameter(name : String) : Parameter?

    /**
     * Upsert (Update, sonst Insert) eines Parameters-Objektes.
     */
    fun upsertParameter(param : Parameter)

    /**
     * Lösche einen Parameter anhand seines Namens.
     */
    fun deleteParameter(name: String)




    // Users

    /**
     * Auslesen aller Benutzer.
     */
    fun getAllUsers() : List<User>

    /**
     * User-Objekt anhand der ID selektieren
     */
    fun getUser(id : String) : User?

    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     */
    fun upsertUser(user : User)




    // Chat

    /**
     * Auslesen aller Chats.
     */
    fun getAllChats() : List<Chat>

    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     */
    fun upsertChat(chat: Chat)



    // ChatUsers

    /**
     * Auslesen aller ChatUSer-Einträge.
     */
    fun getAllChatUsers(): List<ChatUser>

    /**
     * Upsert (Update, sonst Insert) eines chatUser-Objektes.
     */
    fun upsertChatUSer(chatUser: ChatUser)



    // Messages

    /**
     * Auslesen aller Messages.
     */
    fun getAllMessages() : List<Message>


    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum
     */
    fun getMessagesBetween(start: Date, end : Date) : List<Message>

    /**
     * Upsert (Update, sonst Insert) eines Message-Objektes.
     */
    fun upsertMessage(message: Message)








    // Hash-Functions
    fun recalculateAllHashes()
    fun recalculateUserHash()
    fun recalculateChatHash()
    fun recalculateChatUsersHash()
    fun recalculateMessageHash()

    fun getUserHash() : String
    fun getChatHash() : String
    fun getChatUsersHash() : String
    fun getMessageHash() : String




    // High-Level Chat-Funktionen.
    // Lesend mit Joins


}