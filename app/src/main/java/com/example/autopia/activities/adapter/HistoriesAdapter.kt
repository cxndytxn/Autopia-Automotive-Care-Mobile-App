package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.model.Feedbacks
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_sp
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_task
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoriesAdapter(var context: Context, var historyListItems: List<Appointments>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class HistoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(appointmentsModel: Appointments) {
            itemView.appointment_task.text =
                String.format(appointmentsModel.services)
            itemView.appointment_vehicle.text =
                String.format(appointmentsModel.description)
            itemView.appointment_sp.text =
                String.format(appointmentsModel.workshopName)
            itemView.appointment_date_time.text =
                String.format(appointmentsModel.date + " " + appointmentsModel.startTime)
            val builder = itemView.appointment_sp_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f)
                .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.appointment_sp_image.shapeAppearanceModel = builder
            FirestoreClass().fetchWorkshopInfo(appointmentsModel.workshopId).addOnCompleteListener {
                Glide.with(itemView.context).load(it.result.get("imageLink"))
                    .into(itemView.appointment_sp_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.appointment_card_view, parent, false)
        return HistoriesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoriesViewHolder).bind(historyListItems[position])

        val appointmentListItem = historyListItems[position]

        holder.itemView.navigate_button.isVisible = false

        val apiInterface =
            ApiInterface.create().getFeedbackByAppointmentId(appointmentListItem.id!!)
        apiInterface.enqueue(object : Callback<List<Feedbacks>> {
            override fun onResponse(
                call: Call<List<Feedbacks>>,
                response: Response<List<Feedbacks>>
            ) {
                if (response.body().isNullOrEmpty()) {
                    holder.itemView.feedback_button.visibility = View.VISIBLE
                    holder.itemView.feedback_button.setOnClickListener {
                        val bundle = bundleOf(
                            "appointment_id" to appointmentListItem.id,
                            "workshop_id" to appointmentListItem.workshopId,
                            "workshop_name" to appointmentListItem.workshopName,
                            "vehicle" to appointmentListItem.vehicle,
                            "date" to appointmentListItem.date,
                            "time" to appointmentListItem.startTime,
                            "service" to appointmentListItem.services
                        )
                        Navigation.findNavController(holder.itemView)
                            .navigate(R.id.feedbackFragment, bundle)
                    }
                } else {
                    holder.itemView.feedback_button.setBackgroundColor(context.resources.getColor(R.color.theme_grey))
                    holder.itemView.feedback_button.isClickable = false
                }
            }

            override fun onFailure(call: Call<List<Feedbacks>>, t: Throwable) {
            }
        })

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to appointmentListItem.appointmentStatus,
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }
    }
}