package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.model.Services
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*

class SelectServiceAdapter(var context: Context, var serviceListItems: List<Services>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ServicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(servicesModel: Services) {
            itemView.service_name.text = String.format(servicesModel.name)
            itemView.service_description.text =
                String.format(servicesModel.description)
            itemView.service_quotation.text =
                String.format("RM: " + servicesModel.quotation.toString())
            val builder = itemView.service_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.service_image.shapeAppearanceModel = builder
            if (servicesModel.imageLink != null && servicesModel.imageLink != "") {
                Glide.with(itemView.context).load(servicesModel.imageLink)
                    .into(itemView.service_image)
            }
            if (servicesModel.name == "Inspection") {
                val deleteButton: MaterialButton = itemView.findViewById(R.id.delete_service_button)
                deleteButton.isVisible = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.service_card_view, parent, false)
        return ServicesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return serviceListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ServicesViewHolder).bind(serviceListItems[position])

        val serviceListItem = serviceListItems[position]

        holder.itemView.service_card_view.setOnClickListener {
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "service_id",
                serviceListItem.id
            )
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "service_name",
                serviceListItem.name
            )
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "service_quotation",
                serviceListItem.quotation
            )
            Navigation.findNavController(holder.itemView).popBackStack()
        }

        holder.itemView.service_card_view.delete_service_button.visibility = View.GONE
    }
}