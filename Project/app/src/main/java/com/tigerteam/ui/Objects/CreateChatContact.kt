package com.tigerteam.ui.Objects

import java.io.Serializable

// Kontakt, der beim erstellen eines Chats zur Verfügung steht
data class CreateChatContact(
        val userId : String
        , val userName : String
        , val nameInContacts : String
        , val phoneNumber : String?
        , var isSelected : Boolean = false
)  : Serializable {
}