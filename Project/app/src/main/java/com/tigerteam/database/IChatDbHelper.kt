package com.tigerteam.database

import com.tigerteam.database.DbObjects.*
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.Objects.ChatOverviewItem
import com.tigerteam.ui.Objects.ChatUserItem
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
     * Liefert True bei Insert, False bei Update.
     */
    fun upsertParameter(param : Parameter) : Boolean

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
     * Liefert True bei Insert, False bei Update.
     */
    fun upsertUser(user : User) : Boolean




    // Chat

    /**
     * Auslesen aller Chats.
     */
    fun getAllChats() : List<Chat>

    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    fun upsertChat(chat: Chat):Boolean



    // ChatUsers

    /**
     * Auslesen aller ChatUSer-Einträge.
     */
    fun getAllChatUsers(): List<ChatUser>

    /**
     * Upsert (Update, sonst Insert) eines chatUser-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    fun upsertChatUser(chatUser: ChatUser) : Boolean



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
     * Liefert True bei Insert, False bei Update.
     */
    fun upsertMessage(message: ChatMessage) : Boolean








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

    /**
     * Liefert die Chat-Übersicht für den eigenen User
     * Null, wenn eigene User-Id nicht gefunden werden konnte.
     * Chat-Übersicht ist so sortiert, dass erstes Element der Chat mit der jüngsten Nachricht ist.
     * Achtung: Es muss in einem Chat keine letzte Nachricht geben!
     */
    fun getChatOverviewItems() : List<ChatOverviewItem>?


    /**
     * Liefert die Nachrichten eines Chats in der Sortierung, dass die neuste Nachricht an letzter Stelle ist.
     */
    fun getMessagesForChat(chatId : String) : List<ChatItem>

    /**
     * Liefert die Benutzer eines Chats (Owner immer als erstes selektiert)
     */
    fun getUsersForChat(chatId : String) : List<ChatUserItem>


    /**
     * Liefert alle Benutzer zu den übergebenen Telefonnummern
     */
    fun getUsersWithPhoneNumberIn(numbers : List<String>) : List<User>

}