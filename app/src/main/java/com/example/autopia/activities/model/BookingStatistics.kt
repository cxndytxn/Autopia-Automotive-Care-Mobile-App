package com.example.autopia.activities.model

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class BookingStatistics(
    var workshopId: String = "",
    var millis: Long = 0,
    @ServerTimestamp val date: Date? = null,
    var bookingCount: Int = 0,
) : Serializable