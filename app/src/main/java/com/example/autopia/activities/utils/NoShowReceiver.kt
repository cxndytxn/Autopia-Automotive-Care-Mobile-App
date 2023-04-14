package com.example.autopia.activities.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.model.Appointments
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NoShowReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val apiInterface = ApiInterface.create().getAppointments()
        apiInterface.enqueue(object : Callback<List<Appointments>> {
            override fun onResponse(
                call: Call<List<Appointments>>,
                response: Response<List<Appointments>>
            ) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val appointments = response.body()
                    appointments?.forEach { appointment ->
                        if (appointment.endTime != null && appointment.endTime != "") {
                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
                            val formattedDateTime = LocalDateTime.parse(
                                appointment.date + " " + appointment.endTime,
                                formatter
                            )
                            if (LocalDateTime.now() > formattedDateTime && appointment.appointmentStatus == "accepted") {
                                val appointmentObject = Appointments(
                                    appointment.id,
                                    appointment.services,
                                    appointment.serviceId,
                                    appointment.date,
                                    appointment.startTime,
                                    appointment.duration,
                                    appointment.endTime,
                                    "#ff3224",
                                    appointment.vehicle,
                                    appointment.vehicleId,
                                    appointment.phoneNo,
                                    appointment.workshopPhoneNo,
                                    appointment.description,
                                    appointment.workshopId,
                                    appointment.workshopName,
                                    appointment.clientId,
                                    appointment.clientName,
                                    "no show",
                                    appointment.attachment,
                                    appointment.remarks,
                                    appointment.quotedPrice,
                                    appointment.bookDate
                                )
                                updateNoShow(appointment.id!!, appointmentObject)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Appointments>>, t: Throwable) {

            }
        })
    }

    private fun updateNoShow(appointmentId: Int, appointment: Appointments) {
        val apiInterface = ApiInterface.create().putAppointments(appointment, appointmentId)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                Log.d("marked", "no show")
            }

            override fun onFailure(call: Call<Appointments>, t: Throwable) {
            }
        })
    }
}