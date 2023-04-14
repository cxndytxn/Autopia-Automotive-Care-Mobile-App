package com.example.autopia.activities.model

data class Services(
    val id: Int?,
    val workshopId: String,
    val name: String,
    val description: String,
    val quotation: Double?,
    val imageLink: String?,
)