package com.example.autopia.activities.adapter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.*
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.appointment_card_view.view.appointment_date_time
import kotlinx.android.synthetic.main.appointment_card_view.view.appointment_sp
import kotlinx.android.synthetic.main.appointment_card_view.view.appointment_task
import kotlinx.android.synthetic.main.appointment_card_view.view.appointment_vehicle
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkshopPendingAppointmentsAdapter(
    var context: Context,
    var appointmentsListItems: List<Appointments>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AppointmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(appointmentsModel: Appointments) {
            itemView.appointment_sp.text = String.format(appointmentsModel.clientName)
            itemView.appointment_phone.text = String.format(appointmentsModel.phoneNo)
            itemView.appointment_date_time.text =
                String.format(appointmentsModel.date + " " + appointmentsModel.startTime)
            itemView.appointment_vehicle.text = String.format(appointmentsModel.vehicle)
            itemView.appointment_task.text = String.format(appointmentsModel.services)
            val builder = itemView.workshop_appointment_sp_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.workshop_appointment_sp_image.shapeAppearanceModel = builder
            FirestoreClass().fetchWorkshopInfo(appointmentsModel.clientId).addOnCompleteListener {
                Glide.with(itemView.context).load(it.result.get("imageLink"))
                    .into(itemView.workshop_appointment_sp_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.workshop_appointment_card_view, parent, false)
        return AppointmentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppointmentsViewHolder).bind(appointmentsListItems[position])

        val appointmentListItem = appointmentsListItems[position]

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to "pending"
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }

        holder.itemView.acceptAppointmentButton.setOnClickListener {
            if (appointmentListItem.appointmentStatus == "clashed") {
                val dialog = OverlapDialog()
                dialog.setOnPositiveOption("Yes") { proposeDuration(appointmentListItem) }
                dialog.setOnCancelOption("No") {
                    val appointment = Appointments(
                        appointmentListItem.id,
                        appointmentListItem.services,
                        appointmentListItem.serviceId,
                        appointmentListItem.date,
                        appointmentListItem.startTime,
                        appointmentListItem.duration,
                        appointmentListItem.endTime,
                        appointmentListItem.color,
                        appointmentListItem.vehicle,
                        appointmentListItem.vehicleId,
                        appointmentListItem.phoneNo,
                        appointmentListItem.workshopPhoneNo,
                        appointmentListItem.description,
                        appointmentListItem.workshopId,
                        appointmentListItem.workshopName,
                        appointmentListItem.clientId,
                        appointmentListItem.clientName,
                        "cancelled",
                        appointmentListItem.attachment,
                        appointmentListItem.remarks,
                        appointmentListItem.quotedPrice,
                        appointmentListItem.bookDate
                    )
                    val api =
                        ApiInterface.create()
                            .putAppointments(appointment, appointmentListItem.id!!)
                    api.enqueue(object : Callback<Appointments> {
                        override fun onResponse(
                            call: Call<Appointments>,
                            response: Response<Appointments>,
                        ) {
                            Toast.makeText(
                                context,
                                "The appointment request had been rejected.",
                                Toast.LENGTH_SHORT
                            ).show()
                            OneSignalNotificationService().createAppointmentNotification(
                                appointment.clientId,
                                "${appointment.workshopName} had declined the appointment request (appointment ID: ${appointment.id})",
                                "We're sorry to decline your request due to current high workload, please come to us again in the future. We'll be glad to serve you."
                            )
                        }

                        override fun onFailure(call: Call<Appointments>, t: Throwable) {
                            Toast.makeText(
                                context,
                                "Appointment could not be rejected.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    })
                }
                dialog.show(
                    (context as AppCompatActivity).supportFragmentManager,
                    "OverlapDialog"
                )
            } else {
                proposeDuration(appointmentListItem)
            }
        }

        holder.itemView.rejectAppointmentButton.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "user_type" to "workshop",
                "uid" to appointmentListItem.workshopId,
                "time" to appointmentListItem.startTime,
                "date" to appointmentListItem.date,
                "username" to appointmentListItem.clientName,
                "workshop_id" to appointmentListItem.workshopId
            )
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val formattedAppointmentDate = sdf.parse(appointmentListItem.date)
            val millionSeconds =
                formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
            val difference =
                TimeUnit.MILLISECONDS.toDays(millionSeconds)
            val canReschedule: Boolean = difference <= 2
            val dialog = RejectAppointmentDialog(bundle, canReschedule)
            dialog.setOnPositiveOption("Reschedule") {
//                val sdf = SimpleDateFormat("yyyy-MM-dd")
//                val formattedAppointmentDate = sdf.parse(appointmentListItem.date)
//                val millionSeconds =
//                    formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
//                val difference =
//                    TimeUnit.MILLISECONDS.toDays(millionSeconds)
//                Log.d("babu", "wtf")
//                if (difference < 2) {
//                    Toast.makeText(
//                        context,
//                        "Reschedule request is only allowed at least 2 days before the actual appointment date!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                } else {
//                    Navigation.findNavController(holder.itemView).navigate(
//                        R.id.rescheduleAppointmentsFragment,
//                        bundle
//                    )
//                }
            }
            dialog.setOnCancelOption("Reject") { isReasonFilled: Boolean, reason: String ->
                if (isReasonFilled) {
                    val appointment = Appointments(
                        appointmentListItem.id,
                        appointmentListItem.services,
                        appointmentListItem.serviceId,
                        appointmentListItem.date,
                        appointmentListItem.startTime,
                        appointmentListItem.duration,
                        appointmentListItem.endTime,
                        appointmentListItem.color,
                        appointmentListItem.vehicle,
                        appointmentListItem.vehicleId,
                        appointmentListItem.workshopPhoneNo,
                        appointmentListItem.phoneNo,
                        appointmentListItem.description,
                        appointmentListItem.workshopId,
                        appointmentListItem.workshopName,
                        appointmentListItem.clientId,
                        appointmentListItem.clientName,
                        "rejected",
                        appointmentListItem.attachment,
                        appointmentListItem.remarks,
                        appointmentListItem.quotedPrice,
                        appointmentListItem.bookDate
                    )
                    if (appointmentListItem.id != null) {
                        val apiInterface =
                            ApiInterface.create()
                                .putAppointments(appointment, appointmentListItem.id)
                        apiInterface.enqueue(object : Callback<Appointments> {
                            override fun onResponse(
                                call: Call<Appointments>,
                                response: Response<Appointments>,
                            ) {
                                Toast.makeText(
                                    context,
                                    "You had rejected the appointment!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                OneSignalNotificationService().createAppointmentNotification(
                                    appointment.clientId,
                                    "${appointment.workshopName} had rejected your appointment request (appointment ID: ${appointment.id})",
                                    "Message from ${appointment.workshopName}: $reason"
                                )
                            }

                            override fun onFailure(call: Call<Appointments>, t: Throwable) {
                                Toast.makeText(
                                    context,
                                    "Appointment could not be rejected.",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        })
                    }
                } else {
                    Toast.makeText(
                        context,
                        "Please provide a reason for appointment rejection.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            dialog.show(
                (context as AppCompatActivity).supportFragmentManager,
                "reject_dialog"
            )
        }
    }

    private fun proposeDuration(appointmentListItem: Appointments) {
        val dialog = ProposeDurationDialog()
        dialog.setOnTimeSetOption("Set Time") { hour, minute ->
            val df = SimpleDateFormat("hh:mm aa")
            val duration = hour * 60 + minute
            val start = df.parse(appointmentListItem.startTime)
            val cal = Calendar.getInstance()
            if (start != null) {
                cal.time = start
                cal.add(Calendar.MINUTE, duration)
                var endTime = ""
                endTime = df.format(cal.time)
                val appointment = Appointments(
                    appointmentListItem.id,
                    appointmentListItem.services,
                    appointmentListItem.serviceId,
                    appointmentListItem.date,
                    appointmentListItem.startTime,
                    duration,
                    endTime,
                    appointmentListItem.color,
                    appointmentListItem.vehicle,
                    appointmentListItem.vehicleId,
                    appointmentListItem.workshopPhoneNo,
                    appointmentListItem.phoneNo,
                    appointmentListItem.description,
                    appointmentListItem.workshopId,
                    appointmentListItem.workshopName,
                    appointmentListItem.clientId,
                    appointmentListItem.clientName,
                    "accepted",
                    appointmentListItem.attachment,
                    appointmentListItem.remarks,
                    appointmentListItem.quotedPrice,
                    appointmentListItem.bookDate
                )
                if (appointmentListItem.id != null) {
                    val apiInterface =
                        ApiInterface.create()
                            .putAppointments(
                                appointment,
                                appointmentListItem.id
                            )
                    apiInterface.enqueue(object : Callback<Appointments> {
                        override fun onResponse(
                            call: Call<Appointments>,
                            response: Response<Appointments>,
                        ) {
                            Toast.makeText(
                                context,
                                "You had accepted the appointment!",
                                Toast.LENGTH_SHORT
                            ).show()
                            OneSignalNotificationService().createAppointmentNotification(
                                appointment.clientId,
                                "${appointment.workshopName} had accepted your appointment request",
                                "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                            )
                            val intent = Intent(
                                context,
                                ReminderReceiver::class.java
                            ).putExtra("appointment_id", appointmentListItem.id)

                            val pendingIntent =
                                PendingIntent.getBroadcast(
                                    context,
                                    0,
                                    intent,
                                    0
                                )

                            val alarmManager: AlarmManager =
                                context.getSystemService(ALARM_SERVICE) as AlarmManager

                            val sdf = SimpleDateFormat("hh:mm a")
                            sdf.timeZone = TimeZone.getTimeZone("GMT+8")
                            val sdf2 = SimpleDateFormat("HH:mm")
                            sdf2.timeZone = TimeZone.getTimeZone("GMT+8")
                            val formattedTime = sdf.parse(appointmentListItem.startTime)
                            val format = sdf2.format(formattedTime!!)
                            val time = sdf2.parse(format)
                            val reminderDate = Calendar.getInstance()
                            if (time != null) {
                                reminderDate.set(
                                    appointment.date.dropLast(6).toInt(),
                                    appointment.date.drop(5).dropLast(3)
                                        .toInt() - 1,
                                    appointment.date.drop(8).toInt(),
                                    time.hours,
                                    time.minutes
                                )
                                //remind user two hours earlier
                                reminderDate.add(Calendar.MINUTE, -90)
                            }
                            alarmManager.set(
                                AlarmManager.RTC_WAKEUP,
                                reminderDate.timeInMillis,
                                pendingIntent
                            )
                            Log.d("time", time.time.toString())
                            Log.d("reminder", reminderDate.timeInMillis.toString())
                            Log.d("alarm", "alarm")
                        }

                        override fun onFailure(
                            call: Call<Appointments>,
                            t: Throwable
                        ) {
                            Toast.makeText(
                                context,
                                "Appointment could not be accepted.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    })
                }
            }
        }
        dialog.setOnCancelOption("Cancel") {
            dialog.dismiss()
        }
        dialog.show(
            (context as AppCompatActivity).supportFragmentManager,
            "dialog"
        )
    }
}