package com.example.autopia.activities.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.ServiceReminders
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.service_reminder_card.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServiceRemindersAdapter(
    var context: Context,
    var serviceRemindersList: List<ServiceReminders>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ServiceRemindersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(serviceRemindersModel: ServiceReminders) {
            itemView.service_reminder_service.text = "Service needed: " + serviceRemindersModel.services
            itemView.service_reminder_date.text = "Date: " + serviceRemindersModel.date
            itemView.service_reminder_mileage.text = "Mileage: " + serviceRemindersModel.mileage.toString()
            FirestoreClass().fetchWorkshopInfo(serviceRemindersModel.workshopId)
                .addOnSuccessListener { snapshot ->
                    itemView.service_reminder_workshop_name.text =
                        snapshot.data?.get("workshopName").toString()
                    val builder = itemView.service_reminder_workshop_image.shapeAppearanceModel.toBuilder()
                        .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                        .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                        .setBottomRightCorner(CornerFamily.ROUNDED, 40f)
                        .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
                    itemView.service_reminder_workshop_image.shapeAppearanceModel = builder
                    Glide.with(itemView.context).load(snapshot.data?.get("imageLink"))
                        .into(itemView.service_reminder_workshop_image)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.service_reminder_card, parent, false)
        return ServiceRemindersViewHolder(view)
    }

    override fun getItemCount(): Int {
        return serviceRemindersList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ServiceRemindersViewHolder).bind(serviceRemindersList[position])

        val item = serviceRemindersList[position]

        if (item.status == "accepted") {
            holder.itemView.constraintLayout.visibility = View.GONE
        }

        holder.itemView.accept_btn.setOnClickListener {
            val bundle = bundleOf(
                "service_reminder_id" to item.id,
                "service" to item.services,
                "workshop_id" to item.workshopId,
                "appointment_id" to item.appointmentId,
                "date" to item.date,
                "duration" to item.duration,
                "service_id" to item.serviceId,
                "client_id" to item.clientId,
                "mileage" to item.mileage,
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.serviceReminderReviewFragment, bundle)
        }

        holder.itemView.reject_btn.setOnClickListener {
            item.id?.let { it1 ->
                ApiInterface.create().deleteServiceReminders(
                    it1
                )
            }?.enqueue(object : Callback<ServiceReminders> {
                override fun onResponse(
                    call: Call<ServiceReminders>,
                    response: Response<ServiceReminders>,
                ) {
                    Log.d("Response", response.code().toString())
                    Toast.makeText(context, "Service reminder is rejected!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(call: Call<ServiceReminders>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error. Service reminder could not be rejected. " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}