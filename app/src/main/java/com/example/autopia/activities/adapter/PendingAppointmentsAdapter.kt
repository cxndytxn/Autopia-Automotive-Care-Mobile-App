package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
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
import com.example.autopia.activities.utils.RejectAppointmentDialog
import com.google.android.material.shape.CornerFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PendingAppointmentsAdapter(
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

        holder.itemView.constraintLayoutReschedule.visibility = View.VISIBLE
        "Reschedule".also { holder.itemView.acceptRescheduleButton.text = it }
        "Cancel".also { holder.itemView.rejectRescheduleButton.text = it }
        holder.itemView.acceptRescheduleButton.setOnClickListener {
            val sdf = SimpleDateFormat("yyyy-MM-dd")
            val formattedAppointmentDate = sdf.parse(appointmentListItem.date)
            val millionSeconds =
                formattedAppointmentDate!!.time - Calendar.getInstance().timeInMillis
            val difference =
                TimeUnit.MILLISECONDS.toDays(millionSeconds)
            if (difference < 2) {
                Toast.makeText(
                    context,
                    "Reschedule request is only allowed at least 2 days before the actual appointment date!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val bundle = bundleOf(
                    "appointment_id" to appointmentListItem.id,
                    "user_type" to "user",
                    "uid" to appointmentListItem.clientId,
                    "time" to appointmentListItem.startTime,
                    "date" to appointmentListItem.date,
                    "username" to appointmentListItem.clientName,
                    "workshop_id" to appointmentListItem.workshopId
                )
                Navigation.findNavController(holder.itemView).navigate(
                    R.id.rescheduleAppointmentsFragment,
                    bundle
                )
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        holder.itemView.rejectRescheduleButton.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "user_type" to "user",
                "uid" to user?.uid!!,
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
                Navigation.findNavController(holder.itemView).navigate(
                    R.id.rescheduleAppointmentsFragment,
                    bundle
                )
            }

            dialog.setOnCancelOption("Cancel Appointment") { isReasonFilled, reason ->
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
                    val apiInterface =
                        ApiInterface.create().putAppointments(appointment, appointmentListItem.id!!)
                    apiInterface.enqueue(object : Callback<Appointments> {
                        override fun onResponse(
                            call: Call<Appointments>,
                            response: Response<Appointments>,
                        ) {
                            Toast.makeText(
                                context,
                                "You had cancelled the appointment!",
                                Toast.LENGTH_SHORT
                            ).show()
                            OneSignalNotificationService().createAppointmentNotification(
                                appointment.workshopId,
                                "${appointment.clientName} had cancelled appointment (appointment ID: ${appointment.id})",
                                "Message from ${appointment.clientName}: $reason"
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
                } else {
                    Toast.makeText(
                        context,
                        "Please provide a reason for appointment cancellation.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            dialog.show(
                (context as AppCompatActivity).supportFragmentManager,
                "cancel_dialog"
            )
        }

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to "pending"
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }
    }
}