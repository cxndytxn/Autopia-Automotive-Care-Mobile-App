package com.example.autopia.activities.model

data class Products(
    val id: Int? = 0,
    val workshopId: String = "",
    val name: String = "",
    val description: String ="",
    val price: Double? = 0.0,
    val imageLink: String? = "",
)