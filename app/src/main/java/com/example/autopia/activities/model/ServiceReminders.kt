package com.example.autopia.activities.model

data class ServiceReminders(
    val id: Int? = 0,
    val services: String = "",
    val serviceId: Int = 0,
    val date: String = "",
    val duration: Int? = 0,
    val appointmentId: Int = 0,
    val workshopId: String = "",
    val clientId: String = "",
    val remarks: String = "",
    val attachment: String = "",
    val mileage: Int = 0,
    val status: String = ""
)