package com.example.autopia.activities.model

data class Conversations(
    val userId: String? = "",
    val workshopId: String? = "",
    val username: String? = "",
    val workshopName: String? = "",
    val userImage: String? = "",
    val workshopImage: String? = "",
    val latestMsg: String? = "",
    val dateTime: String? = "",
    val userReadStatus: String? = "",
    val workshopReadStatus: String? = "",
    val timestamp: MutableMap<String, String>? = mutableMapOf(),
    val returnedTimestamp: Long? = 0,
    val messageType: String? = ""
)