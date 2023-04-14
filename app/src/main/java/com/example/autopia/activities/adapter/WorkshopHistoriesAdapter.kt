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
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_date_time
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_sp
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_task
import kotlinx.android.synthetic.main.workshop_appointment_card_view.view.appointment_vehicle

class WorkshopHistoriesAdapter(var context: Context, var historyListItems: List<Appointments>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class HistoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workshop_appointment_card_view, parent, false)
        return HistoriesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return historyListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HistoriesViewHolder).bind(historyListItems[position])

        val appointmentListItem = historyListItems[position]

        holder.itemView.constraintLayoutWAppointment.isVisible = false

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "appointment_id" to appointmentListItem.id,
                "appointment_status" to appointmentListItem.appointmentStatus
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.appointmentDetailsFragment, bundle)
        }
    }
}