package com.example.autopia.activities.model

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class ViewStatistics(
    var workshopId: String = "",
    var millis: Long = 0,
    @ServerTimestamp val date: Date? = null,
    var viewCount: Int = 0,
) : Serializable