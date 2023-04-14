package com.example.autopia.activities.model

data class Appointments(
    val id: Int? = 0,
    val services: String = "",
    val serviceId: Int = 0,
    val date: String = "",
    val startTime: String = "",
    val duration: Int? = 0,
    val endTime: String? = "",
    val color: String? = "",
    val vehicle: String = "",
    val vehicleId: Int = 0,
    val phoneNo: String = "",
    val workshopPhoneNo: String  = "",
    val description: String = "",
    val workshopId: String = "",
    val workshopName: String = "",
    val clientId: String = "",
    val clientName: String = "",
    var appointmentStatus: String = "",
    val attachment: String? = "",
    val remarks: String? = "",
    val quotedPrice: Double? = 0.0,
    val bookDate: String? = ""
)