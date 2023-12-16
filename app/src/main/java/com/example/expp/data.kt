package com.example.expp

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val image:String="",
    val status: String="",
    val typing: Boolean=false,
    val password: String ="",
    val token: String? = "",
    var latestMessage: String="",
    var key:String=""


)


data class Message(
    var text: String = "",
    val timestamp: Long = 0,
    val receiverId: String = "",
    val senderId: String = "",
    val senderName:String="",
    val SenderImage:String="",
    val sendingTime: Long = 0,
    val receivingTime: Long = 0,
    val isViewed:Boolean=false,
    val imagesender:String="",

    )
data class LatestMessage(
    var ID:String="",
    var sender:String="",
    var text: String = "",
    val timestamp: Long = 0
)