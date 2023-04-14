package com.example.autopia.activities.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class News(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val postedDate: String = "",
    val imageLink: String = "",
) : Parcelable