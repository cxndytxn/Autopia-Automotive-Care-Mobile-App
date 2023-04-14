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

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val appointmentId = intent?.extras?.getInt("appointment_id")
        Log.d("receiver", "receiver")
        if (appointmentId != null) {
            val apiInterface = ApiInterface.create().getAppointmentById(appointmentId)
            apiInterface.enqueue(object : Callback<Appointments> {
                override fun onResponse(
                    call: Call<Appointments>,
                    response: Response<Appointments>
                ) {
                    if (response.isSuccessful) {
                        val appointment = response.body()
                        if (appointment != null) {
                            OneSignalNotificationService().createAppointmentNotification(
                                appointment.clientId,
                                "Reminder: Appointment with ${appointment.workshopName}",
                                "Date: ${appointment.date}\n" +
                                        "Start Time: ${appointment.startTime}\n" +
                                        "Estimated Duration: ${appointment.duration} minutes\n" +
                                        "Estimated End Time: ${appointment.endTime}"
                            )
                            OneSignalNotificationService().createAppointmentNotification(
                                appointment.workshopId,
                                "Reminder: Appointment with ${appointment.clientName}",
                                "Date: ${appointment.date}\n" +
                                        "Start Time: ${appointment.startTime}\n" +
                                        "Estimated Duration: ${appointment.duration} minutes\n" +
                                        "Estimated End Time: ${appointment.endTime}"
                            )
                        }
                    }
                }

                override fun onFailure(call: Call<Appointments>, t: Throwable) {
                }
            })
        }
    }
}