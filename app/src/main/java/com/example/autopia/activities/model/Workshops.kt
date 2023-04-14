package com.example.autopia.activities.model

import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue
import java.util.ArrayList

@Parcelize
data class Workshops(
    val id: String = "",
    val email: String = "",
    val workshopName: String = "",
    val imageLink: String? = "",
    val userType: String = "",
    val address: String? = "",
    val description: String? = "",
    val lowerName: String = "",
    val latitude: String? = "",
    val longitude: String? = "",
    val contactNumber: String? = "",
    val location: @RawValue GeoPoint? = GeoPoint(0.0, 0.0),
    val openHours: String? = "",
    val closeHours: String? = "",
    val vehicleBrands: ArrayList<String>? = arrayListOf(),
) : Parcelable