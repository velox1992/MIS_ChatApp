package com.tigerteam.database

import com.tigerteam.database.DbObjects.*
import java.util.*
import kotlin.collections.HashMap

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
    fun upsertChatUser(chatUser: ChatUser)



    // Messages

    /**
     * Auslesen aller Messages.
     */
    fun getAllMessages() : List<ChatMessage>


    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum (NICHT nach Zeit sortiert)
     */
    fun getMessagesBetween(start: Date, end : Date) : List<ChatMessage>

    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum nach Zeit sortiert. (Neuste zuletzt in Liste)
     */
    fun getMessagesBetweenOrderedByTime(start: Date, end : Date) : List<ChatMessage>

    /**
     * Upsert (Update, sonst Insert) eines Message-Objektes.
     */
    fun upsertMessage(message: ChatMessage)








    // Hash-Functions

    /**
     * Alle Hashes für die Tabellen neu berechnen
     */
    fun recalculateAllHashes()

    /**
     * Hash für die User-Tabelle aktualisieren
     */
    fun recalculateUserHash()
    /**
     * Hash für die Chat-Tabelle aktualisieren
     */
    fun recalculateChatHash()
    /**
     * Hash für die ChatUsers-Tabelle aktualisieren
     */
    fun recalculateChatUsersHash()
    /**
     * Hash für die Message-Tabelle aktualisieren
     */
    fun recalculateMessageHash()


    /**
     * User-Hash lesen
     */
    fun getUserHash() : String?

    /**
     * Chat-Hash lesen
     */
    fun getChatHash() : String?

    /**
     * ChatUser-Hash lesen
     */
    fun getChatUsersHash() : String?

    /**
     * Message-Hash lesen
     */
    fun getMessageHashes() : HashMap<Date, String>




    // High-Level Chat-Funktionen für die Visualisierung.
    // Lesend mit Joins


}