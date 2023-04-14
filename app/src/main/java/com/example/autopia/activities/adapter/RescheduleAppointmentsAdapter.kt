package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RescheduleAppointmentsAdapter(
    var context: Context,
    var appointmentsListItems: List<Appointments>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AppointmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(appointmentsModel: Appointments) {
            itemView.appointment_sp.text = appointmentsModel.workshopName
            itemView.appointment_date_time.text =
                String.format(appointmentsModel.date + " " + appointmentsModel.startTime)
            itemView.appointment_vehicle.text = appointmentsModel.vehicle
            itemView.appointment_task.text = appointmentsModel.services
            val builder = itemView.appointment_sp_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.appointment_sp_image.shapeAppearanceModel = builder
            FirestoreClass().fetchWorkshopInfo(appointmentsModel.workshopId).addOnCompleteListener {
                Glide.with(itemView.context).load(it.result.get("imageLink"))
                    .into(itemView.appointment_sp_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.appointment_card_view, parent, false)
        return AppointmentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppointmentsViewHolder).bind(appointmentsListItems[position])

        val appointmentListItem = appointmentsListItems[position]

        holder.itemView.navigate_button.isVisible = false

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to appointmentListItem.appointmentStatus
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }

        if (appointmentListItem.appointmentStatus[11] == 'w') {
            holder.itemView.constraintLayoutReschedule.visibility = View.VISIBLE
            holder.itemView.acceptRescheduleButton.setOnClickListener {
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
                uploadAppointment(appointment, "accept")
            }

            holder.itemView.rejectRescheduleButton.setOnClickListener {
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
                uploadAppointment(appointment, "reject")
            }
        }
    }

    private fun uploadAppointment(appointment: Appointments, status: String) {
        val apiInterface =
            ApiInterface.create()
                .putAppointments(appointment, appointment.id!!)
        apiInterface.enqueue(object : Callback<Appointments> {
            override fun onResponse(
                call: Call<Appointments>,
                response: Response<Appointments>,
            ) {
                val textMessage = if (status == "accept") {
                    "You had accepted the appointment!"
                } else {
                    "You had rejected the appointment!"
                }
                val headings = if (status == "accept") {
                    "${appointment.clientName} had accepted your appointment rescheduling request"
                } else {
                    "${appointment.clientName} had rejected your appointment rescheduling request (appointment ID: ${appointment.id})"
                }
                Toast.makeText(
                    context,
                    textMessage,
                    Toast.LENGTH_SHORT
                ).show()
                OneSignalNotificationService().createAppointmentNotification(
                    appointment.workshopId,
                    headings,
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