package com.example.autopia.activities.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.upcoming_workshop_appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkshopUpcomingAppointmentsAdapter(
    var context: Context,
    var appointmentsListItems: List<Appointments>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class AppointmentsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(appointmentsModel: Appointments) {
            itemView.upcoming_appointment_workshop.text =
                String.format(appointmentsModel.clientName)
            itemView.workshop_upcoming_appointment_phone.text = appointmentsModel.phoneNo
            itemView.workshop_upcoming_appointment_date_time.text =
                String.format(appointmentsModel.date + " " + appointmentsModel.startTime)
            itemView.workshop_upcoming_appointment_vehicle.text = appointmentsModel.vehicle
            itemView.workshop_upcoming_appointment_task.text = appointmentsModel.services
            val builder = itemView.upcoming_appointment_sp_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.upcoming_appointment_sp_image.shapeAppearanceModel = builder
            FirestoreClass().fetchWorkshopInfo(appointmentsModel.clientId).addOnCompleteListener {
                Glide.with(itemView.context).load(it.result.get("imageLink"))
                    .into(itemView.upcoming_appointment_sp_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.upcoming_workshop_appointment_card_view, parent, false)
        return AppointmentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointmentsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as AppointmentsViewHolder).bind(appointmentsListItems[position])

        val appointmentListItem = appointmentsListItems[position]

        holder.itemView.doneAppointmentButton.setOnClickListener {
            val progressDialog = ProgressDialog(context as Activity)
            progressDialog.startLoading()
            val appointment = Appointments(
                appointmentListItem.id,
                appointmentListItem.services,
                appointmentListItem.serviceId,
                appointmentListItem.date,
                appointmentListItem.startTime,
                appointmentListItem.duration,
                appointmentListItem.endTime,
                "#70e000",
                appointmentListItem.vehicle,
                appointmentListItem.vehicleId,
                appointmentListItem.phoneNo,
                appointmentListItem.workshopPhoneNo,
                appointmentListItem.description,
                appointmentListItem.workshopId,
                appointmentListItem.workshopName,
                appointmentListItem.clientId,
                appointmentListItem.clientName,
                "done",
                appointmentListItem.attachment,
                appointmentListItem.remarks,
                appointmentListItem.quotedPrice,
                appointmentListItem.bookDate
            )
            if (appointmentListItem.id != null) {
                val apiInterface =
                    ApiInterface.create().putAppointments(appointment, appointmentListItem.id)
                apiInterface.enqueue(object : Callback<Appointments> {
                    override fun onResponse(
                        call: Call<Appointments>,
                        response: Response<Appointments>,
                    ) {
                        Toast.makeText(
                            context,
                            "Appointment had been marked as done.",
                            Toast.LENGTH_SHORT
                        ).show()
                        OneSignalNotificationService().createAppointmentNotification(
                            appointment.clientId,
                            "${appointment.workshopName} marked your appointment as completed",
                            "Date: ${appointment.date}\nStart Time: ${appointment.startTime}\nEstimated Duration: ${appointment.duration} minutes\nEstimated End Time: ${appointment.endTime}"
                        )
                        progressDialog.dismissLoading()
                        val bundle = bundleOf(
                            "appointment_id" to appointment.id,
                            "client_id" to appointment.clientId
                        )
                        Navigation.findNavController(holder.itemView)
                            .navigate(R.id.appointmentInfoFragment, bundle)
                    }

                    override fun onFailure(call: Call<Appointments>, t: Throwable) {
                        Toast.makeText(
                            context,
                            "Appointment could not be marked as done.",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                })
            }
        }

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to "accepted"
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }
    }
}