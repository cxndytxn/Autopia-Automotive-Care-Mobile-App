package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProposeDurationDialog
import com.example.autopia.activities.utils.RejectAppointmentDialog
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_date_time
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_sp
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_task
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkshopRescheduleAppointmentsAdapter(
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
                "appointment_status" to appointmentListItem.appointmentStatus
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }

        if (appointmentListItem.appointmentStatus[11] == 'u') {
            holder.itemView.acceptAppointmentButton.setOnClickListener {
                if (appointmentListItem.duration == 0 && appointmentListItem.endTime == "") {
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
                            endTime = if (cal.get(Calendar.AM_PM) == Calendar.AM) {
                                if (cal.time.hours > 12) {
                                    (cal.time.hours - 12).toString() + ":" + cal.time.minutes.toString() + " AM"
                                } else {
                                    cal.time.hours.toString() + ":" + cal.time.minutes.toString() + " AM"
                                }
                            } else {
                                if (cal.time.hours > 12) {
                                    (cal.time.hours - 12).toString() + ":" + cal.time.minutes.toString() + " PM"
                                } else {
                                    cal.time.hours.toString() + ":" + cal.time.minutes.toString() + " AM"
                                }
                            }
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
                                        .putAppointments(appointment, appointmentListItem.id)
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
                                    }

                                    override fun onFailure(call: Call<Appointments>, t: Throwable) {
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
                } else {
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
                        "accepted",
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
                                    "You had accepted the appointment!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                OneSignalNotificationService().createAppointmentNotification(
                                    appointment.clientId,
                                    "${appointment.workshopName} had accepted your appointment request",
                                    "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                                )
                            }

                            override fun onFailure(call: Call<Appointments>, t: Throwable) {
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

            holder.itemView.rejectAppointmentButton.setOnClickListener {
                val bundle = bundleOf(
                    "appointment_id" to appointmentListItem.id,
                    "user_type" to "user",
                    "uid" to appointmentListItem.workshopId,
                    "time" to appointmentListItem.startTime,
                    "date" to appointmentListItem.date,
                    "username" to appointmentListItem.clientName
                )
                val sdf = SimpleDateFormat("yyyy-MM-dd")
                val formattedAppointmentDate = sdf.parse(appointmentListItem.date)
                val millionSeconds =
                    formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
                val difference =
                    TimeUnit.MILLISECONDS.toDays(millionSeconds)
                val canReschedule: Boolean = difference <= 2
                val dialog = RejectAppointmentDialog(bundle, canReschedule)
                dialog.setOnPositiveOption("") {}
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
                                        "${appointment.workshopName} had rejected your appointment rescheduling request (appointment ID: ${appointment.id})",
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
                            "Please provide a reason for appointment rescheduling rejection.",
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
        } else {
            holder.itemView.constraintLayoutWAppointment.visibility = View.GONE
        }
    }
}
