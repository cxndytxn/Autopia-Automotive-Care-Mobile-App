package com.example.autopia.activities.model

data class Feedbacks(
    val id: Int? = 0,
    val rating: Double = 0.0,
    val comment: String = "",
    val timeliness: String = "",
    val politeness: String = "",
    val speed: String = "",
    val payment: String = "",
    val explanation: String = "",
    val service: String = "",
    val appointmentId: Int = 0,
    val workshopId: String = "",
    val clientId: String = ""
)