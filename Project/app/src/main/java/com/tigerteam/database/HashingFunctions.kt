package com.tigerteam.database

import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.security.MessageDigest
import android.R.attr.data
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.io.Serializable


class HashingFunctions {

    companion object {

        /**
         * Serialisieren eines Objektes in ein Byte-Array
         */
        fun <T : Serializable> serialize(obj: T) : ByteArray{
            val out = ByteArrayOutputStream()
            val os = ObjectOutputStream(out)
            os.writeObject(obj)
            return out.toByteArray()
        }

        /**
         * Deserialisieren eines Objektes von einem Byte-Array
         */
        fun <T : Serializable> deserialize(byteArray: ByteArray) : T{
            val inpStr = ByteArrayInputStream(byteArray)
            val isStr = ObjectInputStream(inpStr)
            val obj = isStr.readObject()

            return obj as T
        }


        /**
         * Bildet über eine ArrayList von serialisierbaren Objekten einen Hash
         */
        fun <T : Serializable> getMD5Hash(list : ArrayList<T>) : ByteArray
        {
            var md = MessageDigest.getInstance("MD5")

            for(item : T in list)
            {
                val serialized = serialize(item)
                md.update(serialized)
            }

            val mdbytes = md.digest()
            return mdbytes
        }
    }
}