package com.example.autopia.activities.api

import com.example.autopia.activities.model.Appointments
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

class ReceiverApi {
    interface FetchAppointments {
        @GET("api/appointments")
        fun fetchAppointments(): Call<List<Appointments>>
    }

    var retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .build()

    var service: FetchAppointments = retrofit.create(FetchAppointments::class.java)
}