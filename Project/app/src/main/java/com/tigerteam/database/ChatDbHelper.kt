package com.tigerteam.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.tigerteam.database.DbObjects.*
import com.tigerteam.mischat.Constants
import com.tigerteam.ui.Objects.ChatItem
import com.tigerteam.ui.Objects.ChatOverviewItem
import com.tigerteam.ui.Objects.ChatUserItem
import java.util.*


class ChatDbHelper(context: Context)
    : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION),
        IChatDbHelper {


    // Für Log nutzen
    private val TAG = "ChatDbHelper"

    /// Überschriebene Methoden SQLiteOpenHelper

    /**
     * Anlegen der Datenbankstruktur
     */
    override fun onCreate(db: SQLiteDatabase?) {
        val method = "onCreate";
        WriteLog(Log.INFO, method,"start")

        val createParametersStatement = "CREATE TABLE ${PARAMETERS_T} (" +
                "${PARAMETERS_C_NAME} TEXT PRIMARY KEY, " +
                "${PARAMETERS_C_TYPE} TEXT, " +
                "${PARAMETERS_C_VALUE} TEXT " +
                ")"

        val createUsersStatement = "CREATE TABLE ${USERS_T} (" +
                "${USERS_C_ID} TEXT PRIMARY KEY, " +
                "${USERS_C_NAME} TEXT " +
                ")"

        val createChatsStatement = "CREATE TABLE ${CHATS_T} (" +
                "${CHATS_C_ID} TEXT PRIMARY KEY, " +
                "${CHATS_C_NAME} TEXT " +
                ")"

        val createChatUsersStatement = "CREATE TABLE ${CHAT_USERS_T} (" +
                "${CHAT_USERS_C_CHATID} TEXT, " +
                "${CHAT_USERS_C_USERID} TEXT, " +
                "${CHAT_USERS_C_ISOWNER} BOOLEAN, " +
                "PRIMARY KEY (${CHAT_USERS_C_CHATID}, ${CHAT_USERS_C_USERID})" +
                ")"

        val createMessagesStatement = "CREATE TABLE ${MESSAGES_T} (" +
                "${MESSAGES_C_ID} TEXT PRIMARY KEY, " +
                "${MESSAGES_C_SENDTIMESTAMP} LONG, " +
                "${MESSAGES_C_DATATYPE} TEXT, " +
                "${MESSAGES_C_DATA} TEXT, " +
                "${MESSAGES_C_SENDERID} TEXT, " +
                "${MESSAGES_C_CHATID} TEXT " +
                ")"

        val createHashesStatement = "CREATE TABLE ${HASHES_T} (" +
                "${HASHES_C_KEY} TEXT, " +
                "${HASHES_C_FROMTIME} LONG, " +
                "${HASHES_C_VALUE} TEXT, " +
                "PRIMARY KEY (${HASHES_C_KEY}, ${HASHES_C_FROMTIME})" +
                ")"


        try {
            WriteLog(Log.INFO, method,"start create parameters")
            db?.execSQL(createParametersStatement)

            WriteLog(Log.INFO, method,"start create users")
            db?.execSQL(createUsersStatement)

            WriteLog(Log.INFO, method,"start create chats")
            db?.execSQL(createChatsStatement)

            WriteLog(Log.INFO, method,"start create chatusers")
            db?.execSQL(createChatUsersStatement)

            WriteLog(Log.INFO, method,"start create messages")
            db?.execSQL(createMessagesStatement)

            WriteLog(Log.INFO, method,"start create hashes")
            db?.execSQL(createHashesStatement)


            WriteLog(Log.INFO, method,"Create Tables done")
        }
        catch (e: Exception){
            WriteLog(Log.ERROR, method, e.toString())
        }


        WriteLog(Log.INFO, method,"end")
    }

    /**
     * Funktion, wenn die Version angehoben wurde
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val method = "onUpgrade";
        WriteLog(Log.INFO, method,"start")

        try {
            db?.execSQL("DROP TABLE IF EXISTS " + PARAMETERS_T)
            db?.execSQL("DROP TABLE IF EXISTS " + USERS_T)
            db?.execSQL("DROP TABLE IF EXISTS " + CHATS_T)
            db?.execSQL("DROP TABLE IF EXISTS " + CHAT_USERS_T)
            db?.execSQL("DROP TABLE IF EXISTS " + MESSAGES_T)
            db?.execSQL("DROP TABLE IF EXISTS " + HASHES_T)

            WriteLog(Log.INFO, method, "Drop done")
        }
        catch (e: Exception){
            WriteLog(Log.ERROR, method, e.toString())
        }

        onCreate(db)

        WriteLog(Log.INFO, method,"end")
    }


    /// END Überschriebene Methoden SQLiteOpenHelper


    /**
     * Konstanten für die Datenbank
     */
    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MISChatApp.db"

        // Parameters-Tabelle
        const val PARAMETERS_T = "parameters"

        const val PARAMETERS_C_NAME = "name"
        const val PARAMETERS_C_TYPE = "type"
        const val PARAMETERS_C_VALUE = "value"


        // Users-Tabelle
        const val USERS_T = "users"

        const val USERS_C_ID = "id"
        const val USERS_C_NAME = "name"


        // Chats-Tabelle
        const val CHATS_T = "chats"

        const val CHATS_C_ID = "id"
        const val CHATS_C_NAME = "name"


        // CHAT_USERS-Tabelle
        const val CHAT_USERS_T = "chat_users"

        const val CHAT_USERS_C_CHATID = "chatId"
        const val CHAT_USERS_C_USERID = "userId"
        const val CHAT_USERS_C_ISOWNER = "isOwner"


        // Messages-Table
        const val MESSAGES_T = "messages"

        const val MESSAGES_C_ID = "id"
        const val MESSAGES_C_SENDTIMESTAMP = "sendTimeStamp"
        const val MESSAGES_C_DATATYPE = "dataType"
        const val MESSAGES_C_DATA = "data"
        const val MESSAGES_C_SENDERID = "senderId"
        const val MESSAGES_C_CHATID = "chatId"

        // Hash-Table
        const val HASHES_T = "hashes"

        const val HASHES_C_KEY = "key"
        const val HASHES_C_FROMTIME = "fromTime"
        const val HASHES_C_VALUE = "value"
    }


    /// END Companion Objekte


    fun WriteLog(prio : Int, method: String, msg : String){
        Log.println(prio, TAG, method + ": " +msg)
    }




    /// Parameter

    /**
     * Aus Cursor Werte auslesen und ein Parameter-Objekt zurückliefern.
     */
    private fun cursorToParameter(cursor: Cursor) : Parameter {
        val name = cursor.getString(0)
        val type = cursor.getString(1)
        val value = cursor.getString(2)

        var param = Parameter(name, type, value)
        return param
    }

    /**
     * Parameter zu anhand des Namens lesen.
     */
    override fun getParameter(name : String) : Parameter?{
        var param : Parameter? = null

        val query = "SELECT * FROM $PARAMETERS_T WHERE $PARAMETERS_C_NAME = \"$name\""

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            param = cursorToParameter(cursor)
            cursor.close()
        }
        db.close()

        return param
    }

    /**
     * Upsert (Update, sonst Insert) eines Parameters-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    override fun upsertParameter(param : Parameter) : Boolean{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(PARAMETERS_C_NAME, param.name)
        values.put(PARAMETERS_C_TYPE, param.type)
        values.put(PARAMETERS_C_VALUE, param.value)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(PARAMETERS_T, values, PARAMETERS_C_NAME + " = ?", arrayOf(param.name))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(PARAMETERS_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }


    /**
     * Lösche einen Parameter anhand seines Namens.
     */
    override fun deleteParameter(name: String) {
        val db = this.writableDatabase
        var affectedRows = db.delete(PARAMETERS_T, PARAMETERS_C_NAME + " = ?", arrayOf(name))
        db.close()
    }


    /// END Parameter


    /// User

    /**
     * Aus Cursor Werte auslesen und ein User-Objekt zurückliefern.
     */
    private fun cursorToUser(cursor: Cursor) : User {
        val id = cursor.getString(0)
        val name = cursor.getString(1)

        var user = User(id, name)
        return user
    }


    /**
     * Auslesen aller Benutzer.
     */
    override fun getAllUsers(): List<User> {
        val ret = mutableListOf<User>()

        val query = "SELECT * FROM $USERS_T ORDER BY $USERS_C_ID"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            do {
                val user = cursorToUser(cursor)
                ret.add(user)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }

    /**
     * User-Objekt anhand der ID selektieren
     */
    override fun getUser(id: String): User? {
        var user : User? = null

        val query = "SELECT * FROM $USERS_T WHERE $USERS_C_ID = \"$id\""

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            user = cursorToUser(cursor)
            cursor.close()
        }
        db.close()

        return user
    }


    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    override fun upsertUser(user: User) : Boolean {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(USERS_C_ID, user.id)
        values.put(USERS_C_NAME, user.name)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(USERS_T, values, USERS_C_ID + " = ?", arrayOf(user.id ))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(USERS_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }

    /// END User


    /// Chat

    /**
     * Aus Cursor Werte auslesen und ein User-Objekt zurückliefern.
     */
    private fun cursorToChat(cursor: Cursor) : Chat {
        val id = cursor.getString(0)
        val name = cursor.getString(1)

        var chat = Chat(id, name)
        return chat
    }


    /**
     * Auslesen aller Chat-Objekte.
     */
    override fun getAllChats(): List<Chat> {
        val ret = mutableListOf<Chat>()

        val query = "SELECT * FROM $CHATS_T ORDER BY $CHATS_C_ID"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            do {
                val chat = cursorToChat(cursor)
                ret.add(chat)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }


    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    override fun upsertChat(chat: Chat) : Boolean {

        val db = this.writableDatabase

        val values = ContentValues()
        values.put(CHATS_C_ID, chat.id)
        values.put(CHATS_C_NAME, chat.name)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(CHATS_T, values, CHATS_C_ID + " = ?", arrayOf(chat.id ))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(CHATS_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }

    /// END Chat



    /// ChatUsers


    /**
     * Aus Cursor Werte auslesen und ein ChatUser-Objekt zurückliefern.
     */
    private fun cursorToChatUser(cursor: Cursor) : ChatUser {
        val chatId = cursor.getString(0)
        val userId = cursor.getString(1)
        val isOwner = cursor.getInt(2) > 0

        var chatUser = ChatUser(chatId, userId, isOwner)
        return chatUser
    }


    /**
     * Auslesen aller ChatUSer-Einträge.
     */
    override fun getAllChatUsers(): List<ChatUser> {
        val ret = mutableListOf<ChatUser>()

        val query = "SELECT * FROM $CHAT_USERS_T ORDER BY $CHAT_USERS_C_CHATID, $CHAT_USERS_C_USERID"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            do {
                val chatUser = cursorToChatUser(cursor)
                ret.add(chatUser)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }


    /**
     * Upsert (Update, sonst Insert) eines chatUser-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    override fun upsertChatUser(chatUser: ChatUser) : Boolean {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(CHAT_USERS_C_CHATID, chatUser.chatId)
        values.put(CHAT_USERS_C_USERID, chatUser.userId)
        values.put(CHAT_USERS_C_ISOWNER, chatUser.isOwner)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(CHAT_USERS_T, values, CHAT_USERS_C_CHATID + " = ? AND " + CHAT_USERS_C_USERID + " = ?" , arrayOf(chatUser.chatId, chatUser.userId ))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(CHAT_USERS_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }

    /// END ChatUsers


    /// Messages

    /**
     * Aus Cursor Werte auslesen und ein Message-Objekt zurückliefern.
     */
    private fun cursorToMessage(cursor: Cursor): ChatMessage  {
        val id = cursor.getString(0)
        val timeInMilliseconds =  cursor.getLong(1)
        val dataType = cursor.getString(2)
        val data = cursor.getString(3)
        val senderId = cursor.getString(4)
        val chatId = cursor.getString(5)

        var msg = ChatMessage(id, Date(timeInMilliseconds) ,dataType, data, senderId, chatId)
        return msg
    }


    /**
     * Auslesen aller Messages.
     */
    override fun getAllMessages(): List<ChatMessage> {
        val ret = mutableListOf<ChatMessage>()

        val query = "SELECT * FROM $MESSAGES_T ORDER BY $MESSAGES_C_ID"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            do {
                val msg = cursorToMessage(cursor)
                ret.add(msg)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }

    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum
     */
    override fun getMessagesBetween(start: Date, end : Date) : List<ChatMessage>{
        val ret = mutableListOf<ChatMessage>()

        val query = "SELECT * FROM $MESSAGES_T WHERE $MESSAGES_C_SENDTIMESTAMP BETWEEN ? AND ? ORDER BY $MESSAGES_C_ID"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, arrayOf(start.time.toString(), end.time.toString()))
        if(cursor.moveToFirst())
        {
            do {
                val msg = cursorToMessage(cursor)
                ret.add(msg)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }

    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum nach Zeit sortiert. (Neuste zuletzt in Liste)
     */
    override fun getMessagesBetweenOrderedByTime(start: Date, end: Date): List<ChatMessage> {
        var ret = listOf<ChatMessage>()
        val unOrdered = getMessagesBetween(start, end)

        if(unOrdered.count() > 0) {
            ret = unOrdered.sortedBy { x -> x.timeStamp.time }
        }

        return ret
    }


    /**
     * Upsert (Update, sonst Insert) eines Message-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    override fun upsertMessage(msg: ChatMessage) : Boolean{
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(MESSAGES_C_ID, msg.id)
        values.put(MESSAGES_C_SENDTIMESTAMP, msg.timeStamp.time)
        values.put(MESSAGES_C_DATATYPE, msg.dataType)
        values.put(MESSAGES_C_DATA, msg.data)
        values.put(MESSAGES_C_SENDERID, msg.senderId)
        values.put(MESSAGES_C_CHATID, msg.chatId)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(MESSAGES_T, values, MESSAGES_C_ID + " = ?", arrayOf(msg.id ))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(MESSAGES_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }

    /// END Messages













    /// Hash
    /// Zeistempel als Millisekunden (siehe Date)

    /**
     * Aus Cursor Werte auslesen und ein Hash-Objekt zurückliefern.
     */
    private fun cursorToHash(cursor: Cursor): Hash  {
        val key = cursor.getString(0)
        val timeInMilliseconds =  cursor.getLong(1)
        val value = cursor.getString(2)

        var hash = Hash(key, Date(timeInMilliseconds) ,value)
        return hash
    }


    /**
     * Upsert (Update, sonst Insert) eines Hash-Objektes.
     * Liefert True bei Insert, False bei Update.
     */
    private fun UpsertHash(hash : Hash) : Boolean {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(HASHES_C_KEY, hash.key)
        values.put(HASHES_C_FROMTIME, hash.fromTime.time)
        values.put(HASHES_C_VALUE, hash.value)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(HASHES_T, values, HASHES_C_KEY + " = ? AND "+ HASHES_C_FROMTIME + " = ?", arrayOf(hash.key, hash.fromTime.time.toString()))

        if(affectedRows == 0) {
            // nun Insert notwendig
            db.insert(HASHES_T, null, values)
        }
        db.close()

        return affectedRows == 0;
    }


    /**
     * Einträge in der Hash-Tabelle für einen Key lesen.
     */
    private fun getHashesForKey(key : String): List<Hash> {
        val ret = mutableListOf<Hash>()

        val query = "SELECT * FROM $HASHES_T WHERE $HASHES_C_KEY = \"$key\" ORDER BY $HASHES_C_FROMTIME"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, null)
        if(cursor.moveToFirst())
        {
            do {
                val hash = cursorToHash(cursor)
                ret.add(hash)
            }
            while (cursor.moveToNext())
            cursor.close()
        }
        db.close()
        return ret
    }

    /**
     * Alle Hashes für die Tabellen neu berechnen
     */
    override fun recalculateAllHashes() {
        recalculateChatHash()
        recalculateChatHash()
        recalculateChatUsersHash()
        recalculateMessageHash()
    }

    /**
     * Hash für die User-Tabelle aktualisieren
     */
    override fun recalculateUserHash() {
        val allUsers = getAllUsers()

        //Hash berechnen
        val hashValue = HashingFunctions.byteArrayToHexString(HashingFunctions.getMD5Hash(allUsers));

        val hash = Hash(USERS_T, Date(0), hashValue)
        UpsertHash(hash)
    }

    /**
     * Hash für die Chat-Tabelle aktualisieren
     */
    override fun recalculateChatHash() {
        val allChats = getAllChats()

        //Hash berechnen
        val hashValue = HashingFunctions.byteArrayToHexString(HashingFunctions.getMD5Hash(allChats));

        val hash = Hash(CHATS_T, Date(0), hashValue)
        UpsertHash(hash)
    }

    /**
     * Hash für die ChatUsers-Tabelle aktualisieren
     */
    override fun recalculateChatUsersHash() {
        val allChatUsers = getAllChatUsers()

        //Hash berechnen
        val hashValue = HashingFunctions.byteArrayToHexString(HashingFunctions.getMD5Hash(allChatUsers));

        val hash = Hash(CHAT_USERS_T, Date(0), hashValue)
        UpsertHash(hash)
    }

    /**
     * Hash für die Message-Tabelle aktualisieren
     */
    override fun recalculateMessageHash() {
        val allMessages = getAllMessages()

        val groupedByHour = allMessages.groupBy { (it.timeStamp.time /(1000 * 60*60)) * (1000 * 60*60)} // auf Stunden gehen

        for(group in groupedByHour) {
            //Hash berechnen
            val hashValue = HashingFunctions.byteArrayToHexString(HashingFunctions.getMD5Hash(group.value));

            val hash = Hash(MESSAGES_T, Date(group.key), hashValue)
            UpsertHash(hash)
        }
    }



    /**
     * User-Hash lesen
     */
    override fun getUserHash() : String? {
        var ret : String? = null

        val hashes = getHashesForKey(USERS_T)
        if(hashes.count() > 0)
        {
            ret = hashes[0].value
        }
        return ret
    }

    /**
     * Chat-Hash lesen
     */
    override fun getChatHash() : String? {
        var ret : String? = null

        val hashes = getHashesForKey(CHATS_T)
        if(hashes.count() > 0)
        {
            ret = hashes[0].value
        }
        return ret
    }

    /**
     * ChatUsers-Hash lesen
     */
    override fun getChatUsersHash() : String? {
        var ret : String? = null

        val hashes = getHashesForKey(CHAT_USERS_T)
        if(hashes.count() > 0)
        {
            ret = hashes[0].value
        }
        return ret
    }

    /**
     * Message-Hash lesen
     */
    override fun getMessageHashes() : HashMap<Date, String> {
        var ret = HashMap<Date, String>()

        val hashes = getHashesForKey(MESSAGES_T)
        for(h : Hash in hashes)
        {
            ret.put(h.fromTime, h.value)
        }
        return ret
    }



    /// END Hash






    /// High-Level Chat-Funktionen für die Visualisierung


    /**
     * Liefert die Chat-Übersicht für den eigenen User
     * Null, wenn eigene User-Id nicht gefunden werden konnte.
     * Chat-Übersicht ist so sortiert, dass erstes Element der Chat mit der jüngsten Nachricht ist.
     * Achtung: Es muss in einem Chat keine letzte Nachricht geben!
     */
    override fun getChatOverviewItems(): List<ChatOverviewItem>? {
        var ret : List<ChatOverviewItem>?  = null

        val ownUserIdParam = getParameter(Constants.PARAM_OWN_USER_ID)

        if(ownUserIdParam != null) {
            ret = mutableListOf<ChatOverviewItem>()

            // hole die Chats, wo ich drin bin, und dann die neuste Message dazu noch mit rein
            val query = "SELECT $CHATS_T.$CHATS_C_NAME ,$CHATS_T.$CHATS_C_ID , $USERS_T.$USERS_C_NAME , $USERS_T.$USERS_C_ID, " +
                    "$MESSAGES_C_DATATYPE, $MESSAGES_C_DATA, $MESSAGES_C_SENDTIMESTAMP " +
                    "FROM $CHAT_USERS_T " +
                    "JOIN $CHATS_T on ($CHAT_USERS_T.$CHAT_USERS_C_CHATID = $CHATS_T.$CHATS_C_ID) " +
                    "LEFT JOIN $MESSAGES_T on ($MESSAGES_T.$MESSAGES_C_ID = " +
                    "(SELECT $MESSAGES_C_ID FROM $MESSAGES_T " +
                    "WHERE $MESSAGES_T.$MESSAGES_C_CHATID = $CHAT_USERS_T.$CHAT_USERS_C_CHATID " +
                    "ORDER BY $MESSAGES_C_SENDTIMESTAMP DESC " +
                    "LIMIT 1)" + // get only the Last message
                    ") " +
                    "LEFT JOIN $USERS_T on ($MESSAGES_C_SENDERID = $USERS_T.$USERS_C_ID) " +
                    "WHERE $CHAT_USERS_C_USERID = ? " +
                    "ORDER BY $MESSAGES_C_SENDTIMESTAMP DESC"

            val db = this.writableDatabase
            val cursor = db.rawQuery(query, arrayOf(ownUserIdParam.value))
            if (cursor.moveToFirst()) {
                do {
                    val chatName = cursor.getString(0)
                    val chatId = cursor.getString(1)
                    val lastUserName = cursor.getString(2)
                    val lastUserId = cursor.getString(3)
                    val lastMessageDataType = cursor.getString(4)
                    val lastMessageData = cursor.getString(5)
                    val lastMessageTimeStamp = Date(cursor.getLong(6))
                    val chatOverviewItem = ChatOverviewItem(chatName, chatId, lastUserName, lastUserId, lastMessageDataType, lastMessageData, lastMessageTimeStamp)

                    ret.add(chatOverviewItem)
                } while (cursor.moveToNext())

                cursor.close()
            }
            db.close()
        }

        val retString = if(ret == null)  "NULL" else "${ret.size} Elements"
        WriteLog(Log.INFO, "getChatOverviewItems", "returning ${retString}")

        return ret;
    }



    /**
     * Liefert die Nachrichten eines Chats in der Sortierung, dass die neuste Nachricht an letzter Stelle ist.
     */
    override fun getMessagesForChat(chatId : String) : List<ChatItem> {
        var ret = mutableListOf<ChatItem>()

        // hole die Nachrichten einer ChatID
        val query = "SELECT $USERS_T.$USERS_C_ID, $USERS_T.$USERS_C_NAME, $MESSAGES_C_DATATYPE, $MESSAGES_C_DATA, $MESSAGES_C_SENDTIMESTAMP " +
                "FROM $MESSAGES_T " +
                "JOIN $USERS_T ON ($MESSAGES_C_SENDERID = $USERS_T.$USERS_C_ID) " +
                "WHERE $MESSAGES_T.$MESSAGES_C_CHATID = ? " +
                "ORDER BY $MESSAGES_C_SENDTIMESTAMP ASC"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, arrayOf(chatId))
        if(cursor.moveToFirst())
        {
            do
            {
                val userId = cursor.getString(0)
                val userName = cursor.getString(1)
                val messageDataType = cursor.getString(2)
                val messageData = cursor.getString(3)
                val messageTimeStamp = Date(cursor.getLong(4))
                val chatItem = ChatItem(userId, userName, messageDataType, messageData, messageTimeStamp)

                ret.add(chatItem)
            }
            while (cursor.moveToNext())

            cursor.close()
        }
        db.close()

        val retString = if(ret == null)  "NULL" else "${ret.size} Elements"
        WriteLog(Log.INFO, "getChatOverviewItems", "returning ${retString}")

        return ret;
    }


    /**
     * Liefert die Benutzer eines Chats (Owner immer als erstes selektiert)
     */
    override fun getUsersForChat(chatId : String) : List<ChatUserItem>  {
        var ret = mutableListOf<ChatUserItem>()

        // hole alle Mitglieder eines Chats
        val query = "SELECT  $USERS_T.$USERS_C_ID, $USERS_T.$USERS_C_NAME, $CHAT_USERS_T.$CHAT_USERS_C_ISOWNER " +
                "FROM $CHAT_USERS_T " +
                "JOIN $USERS_T ON ($CHAT_USERS_T.$CHAT_USERS_C_USERID = $USERS_T.$USERS_C_ID) " +
                "WHERE $CHAT_USERS_T.$CHAT_USERS_C_CHATID = ? " +
                "ORDER BY $CHAT_USERS_T.$CHAT_USERS_C_ISOWNER DESC"

        val db = this.writableDatabase
        val cursor =  db.rawQuery(query, arrayOf(chatId))
        if(cursor.moveToFirst())
        {
            do
            {
                val userId = cursor.getString(0)
                val userName = cursor.getString(1)
                val isOwner = cursor.getInt(2) > 0
                val chatUserItem = ChatUserItem(userId, userName, isOwner)

                ret.add(chatUserItem)
            }
            while (cursor.moveToNext())

            cursor.close()
        }
        db.close()

        val retString = if(ret == null)  "NULL" else "${ret.size} Elements"
        WriteLog(Log.INFO, "getChatOverviewItems", "returning ${retString}")

        return ret;
    }


















    ///// CREATE Test-DAta


    /**
    var me = User("1111", "Nils")
    var other = User("2222", "jemand anderes")
    db.upsertUser(me)
    db.upsertUser(other)

    val ownUserId = Parameter(Constants.PARAM_OWN_USER_ID, "String", me.id)
    db.upsertParameter(ownUserId)

    var chat = Chat("0000", "Cooler Chat")
    db.upsertChat(chat)
    var chatUser1 = ChatUser(chat.id, me.id, true)
    var chatUser2= ChatUser(chat.id, other.id, false)
    db.upsertChatUser(chatUser1)
    db.upsertChatUser(chatUser2)

    var msgMe = ChatMessage("1", Date(Date().time -100), "text", "Hallo", me.id, chat.id)
    var msgOther = ChatMessage("2", Date(Date().time -50), "text", "Welt", other.id, chat.id)

    db.upsertMessage(msgMe)
    db.upsertMessage(msgOther)




    chat = Chat("0001", "Cooler Chat 2")
    db.upsertChat(chat)
    chatUser1 = ChatUser(chat.id, me.id, true)
    chatUser2= ChatUser(chat.id, other.id, false)
    db.upsertChatUser(chatUser1)
    db.upsertChatUser(chatUser2)

    msgMe = ChatMessage("10", Date(Date().time -100), "text", "Hallo", me.id, chat.id)
    msgOther = ChatMessage("11", Date(), "text", "Welt 2", other.id, chat.id)

    db.upsertMessage(msgMe)
    db.upsertMessage(msgOther)





    chat = Chat("0002", "Cooler Chat 3 ohne Message")
    db.upsertChat(chat)
    chatUser1 = ChatUser(chat.id, me.id, true)
    chatUser2= ChatUser(chat.id, other.id, false)
    db.upsertChatUser(chatUser1)
    db.upsertChatUser(chatUser2)
     */



    ///// END CREATE Test-DAta
}