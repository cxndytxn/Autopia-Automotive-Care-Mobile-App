package com.example.autopia.activities.model

import java.time.LocalDateTime

data class CalendarEntities(
    val id: Long,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String,
    val color: Int,
    val isAllDay: Boolean,
    val isCanceled: Boolean,
    val dayOfMonth: Int,
    val workshopId: String,
    val duration: Int
)