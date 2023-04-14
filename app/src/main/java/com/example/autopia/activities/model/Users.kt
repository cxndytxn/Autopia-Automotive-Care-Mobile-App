package com.example.autopia.activities.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Users(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val userType: String = "",
    val promotion: String = "true",
) : Parcelable