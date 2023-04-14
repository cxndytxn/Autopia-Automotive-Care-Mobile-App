package com.example.autopia.activities.model

data class Vehicles(
    val id: Int? = 0,
    val plateNo: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val purchaseYear: String = "",
    val currentMileage: String? = "",
    val imageLink: String? = "",
    val clientId: String = "",
)