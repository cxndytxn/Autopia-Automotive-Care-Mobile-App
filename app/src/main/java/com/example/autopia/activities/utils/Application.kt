package com.example.autopia.activities.utils

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.model.Appointments
import com.onesignal.OneSignal
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class Application : Application() {
    private val ONESIGNAL_APP_ID = "5db400c8-98a6-4b9e-b816-20a435dba7e5"

    override fun onCreate() {
        super.onCreate()
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)

//        val apiInterface =
//            ApiInterface.create().getAppointments()
//        apiInterface.enqueue(object : Callback<List<Appointments>> {
//            override fun onResponse(
//                call: Call<List<Appointments>>,
//                response: Response<List<Appointments>>
//            ) {
//                Log.d("bello", "connecting to REST")
//            }
//
//            override fun onFailure(call: Call<List<Appointments>>, t: Throwable) {
//                Log.d("bello", "connection to REST failed")
//            }
//        })

        checkNoShows()
        checkNoResponds()
//        OneSignal.setNotificationOpenedHandler { result ->
//            val type: String = result.action.type.toString()
//            if (type == "Opened") {
//                val receiverId = result.notification.additionalData.getString("receiver_id")
//                val receiverName = result.notification.additionalData.getString("receiver_name")
//                val receiverType = result.notification.additionalData.getString("receiver_type")
//                val bundle = bundleOf(
//                    "receiver_id" to receiverId,
//                    "receiver_name" to receiverName
//                )
//                Log.d("receiverType", receiverType)
//                if (receiverType == "user") {
//                    pendingIntent = NavDeepLinkBuilder(applicationContext)
//                        .setComponentName(NavigationDrawerActivity::class.java)
//                        .setGraph(R.navigation.mobile_navigation)
//                        .setDestination(R.id.homeFragment)
//                        .setDestination(R.id.action_homeFragment_to_chatGraph)
//                        .setArguments(bundle)
//                        .createPendingIntent()
//                    pendingIntent.send()
//                } else {
//                    pendingIntent = NavDeepLinkBuilder(applicationContext)
//                        .setComponentName(WorkshopNavigationDrawerActivity::class.java)
//                        .setGraph(R.navigation.workshop_mobile_navigation)
//                        .setDestination(R.id.chatRoomFragment)
//                        .setArguments(bundle)
//                        .createPendingIntent()
//                    pendingIntent.send()
//                }
//            }
//        }

//        val mIntent = Intent(this, NavigationDrawerActivity::class.java)
//
//        val mPendingIntent = getBroadcast(this, 0, mIntent, FLAG_UPDATE_CURRENT)
//        val mAlarmManager = this
//            .getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        mAlarmManager.setRepeating(
//            AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
//            Calendar., mPendingIntent
//        )
    }

    private fun checkNoResponds() {
        val apiInterface = ApiInterface.create().getAppointments()
        apiInterface.enqueue(object : Callback<List<Appointments>> {
            override fun onResponse(
                call: Call<List<Appointments>>,
                response: Response<List<Appointments>>,
            ) {
                response.body()?.forEach { appointment ->
                    val sdf = SimpleDateFormat("yyyy-MM-dd")
                    val cal = Calendar.getInstance()
                    if (appointment.bookDate != null) {
                        cal.time = sdf.parse(appointment.bookDate) as Date
                        cal.add(Calendar.DATE, 2)
                        val date = sdf.format(cal.time)
                        val twoDays = sdf.parse(date)
                        val now = Date()
                        if (twoDays != null && appointment.id != null) {
                            if (appointment.appointmentStatus == "pending" && twoDays < now) {
                                val update = Appointments(
                                    appointment.id,
                                    appointment.services,
                                    appointment.serviceId,
                                    appointment.date,
                                    appointment.startTime,
                                    appointment.duration,
                                    appointment.endTime,
                                    appointment.color,
                                    appointment.vehicle,
                                    appointment.vehicleId,
                                    appointment.phoneNo,
                                    appointment.workshopPhoneNo,
                                    appointment.description,
                                    appointment.workshopId,
                                    appointment.workshopName,
                                    appointment.clientId,
                                    appointment.clientName,
                                    "no response",
                                    appointment.attachment,
                                    appointment.remarks,
                                    appointment.quotedPrice,
                                    appointment.bookDate
                                )
                                val api =
                                    ApiInterface.create().putAppointments(update, appointment.id)
                                api.enqueue(object : Callback<Appointments> {
                                    override fun onResponse(
                                        call: Call<Appointments>,
                                        response: Response<Appointments>
                                    ) {
                                        OneSignalNotificationService().createAppointmentNotification(
                                            appointment.clientId,
                                            "Your appointment request had not been responded by workshop.",
                                            "We're sorry about that, please give our other workshops a try."
                                        )
                                    }

                                    override fun onFailure(call: Call<Appointments>, t: Throwable) {

                                    }
                                }
                                )
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<Appointments>>, t: Throwable) {

            }
        })
    }

    private fun checkNoShows() {
        val alarmManager = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, NoShowReceiver::class.java)
        val alarmPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val hour = 23
        val minute = 55

        val calendar = GregorianCalendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            alarmPendingIntent
        )
    }
}