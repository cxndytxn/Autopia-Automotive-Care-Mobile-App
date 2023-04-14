package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Services
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.news_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*
import kotlinx.android.synthetic.main.workshop_services_card.view.*

class WorkshopServicesAdapter(var context: Context, var serviceListItems: List<Services>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ServicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(servicesModel: Services) {
            itemView.workshop_service_name.text = String.format(servicesModel.name)
            itemView.workshop_service_description.text =
                String.format(servicesModel.description)
            if (servicesModel.quotation == 0.0) {
                "Free".also { itemView.workshop_service_quotation.text = it }
            } else {
                itemView.workshop_service_quotation.text =
                    String.format("RM" + servicesModel.quotation)
            }
            val builder = itemView.workshop_service_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.workshop_service_image.shapeAppearanceModel = builder
            Glide.with(itemView.context).load(servicesModel.imageLink)
                .into(itemView.workshop_service_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workshop_services_card, parent, false)
        return ServicesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return serviceListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ServicesViewHolder).bind(serviceListItems[position])

        val serviceListItem = serviceListItems[position]

//        holder.itemView.setOnClickListener {
//            val bundle = bundleOf(
//                "title" to serviceListItem.name,
//                "description" to serviceListItem.description,
//                "image" to serviceListItem.imageLink
//            )
//            Navigation.findNavController(holder.itemView)
//                .navigate(R.id.servicesDetailsFragment, bundle)
//        }
    }

}