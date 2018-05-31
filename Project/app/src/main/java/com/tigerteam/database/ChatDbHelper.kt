package com.tigerteam.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.tigerteam.database.DbObjects.*
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
                "${MESSAGES_C_SENDTIMESTAMP} DATETIME, " +
                "${MESSAGES_C_DATATYPE} TEXT, " +
                "${MESSAGES_C_DATA} TEXT, " +
                "${MESSAGES_C_SENDERID} TEXT, " +
                "${MESSAGES_C_CHATID} TEXT " +
                ")"

        val createHashesStatement = "CREATE TABLE ${HASHES_T} (" +
                "${HASHES_C_KEY} TEXT, " +
                "${HASHES_C_FROMTIME} DATETIME, " +
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
     */
    override fun upsertParameter(param : Parameter){
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

        val query = "SELECT * FROM $USERS_T"

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
     */
    override fun upsertUser(user: User) {
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
    }

    /// END User


    /// Chat


    /**
     * Auslesen aller Chats.
     */
    override fun getAllChats(): List<Chat> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    /**
     * Upsert (Update, sonst Insert) eines User-Objektes.
     */
    override fun upsertChat(chat: Chat) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /// END Chat



    /// ChatUsers

    /**
     * Auslesen aller ChatUSer-Einträge.
     */
    override fun getAllChatUsers(): List<ChatUser> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    /**
     * Upsert (Update, sonst Insert) eines chatUser-Objektes.
     */
    override fun upsertChatUSer(chatUser: ChatUser) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /// END ChatUsers


    /// Messages


    /**
     * Auslesen aller Messages.
     */
    override fun getAllMessages(): List<Message> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Liefert alle Nachrichten für einen bestimmten Zeitraum
     */
    override fun getMessagesBetween(start: Date, end : Date) : List<Message>{
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    /**
     * Upsert (Update, sonst Insert) eines Message-Objektes.
     */
    override fun upsertMessage(message: Message) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /// END Messages













    /// Hash

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


    // TODO: Wieder entfernen
    fun Test() {
        var h = Hash("T1", Date(1527776078417), "1")
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(HASHES_C_KEY, h.key)
        values.put(HASHES_C_FROMTIME, h.fromTime.time)
        values.put(HASHES_C_VALUE, h.value)

        // erst versuch Update, wenn nichts betroffen, dann insert
        val affectedRows = db.update(HASHES_T, values, HASHES_C_KEY + " = ? AND " + HASHES_C_FROMTIME +" = ?", arrayOf(h.key, h.fromTime.time.toString() ))

        if(affectedRows == 0) {
            // nun Insert notwendig
            var a = db.insert(HASHES_T, null, values)
        }
        db.close()

        //getHashesForKey(h.key)

    }


    private fun getHashesForKey(key : String): List<Hash> {
        val ret = mutableListOf<Hash>()

        val query = "SELECT * FROM $HASHES_T WHERE $HASHES_C_KEY = \"$key\""

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


    override fun recalculateAllHashes() {
        recalculateChatHash()
        recalculateChatHash()
        recalculateChatUsersHash()
        recalculateMessageHash()
    }

    override fun recalculateUserHash() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun recalculateChatHash() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun recalculateChatUsersHash() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun recalculateMessageHash() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    override fun getUserHash() : String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatHash() : String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChatUsersHash() : String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMessageHash() : String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }



    /// END Hash





}