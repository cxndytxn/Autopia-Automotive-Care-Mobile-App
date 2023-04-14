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
import com.example.autopia.activities.model.Workshops
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.workshop_card_view.view.*

class WorkshopsListAdapter(var context: Context, var workshopsListItems: List<Workshops>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class WorkshopsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(workshopsModel: Workshops) {
            itemView.workshop_list_name.text = workshopsModel.workshopName
            if (workshopsModel.address != "") {
                itemView.workshop_list_address.text = workshopsModel.address
            } else {
                itemView.workshop_list_address.text = "-"
            }
            if (workshopsModel.description != "") {
                itemView.workshop_list_description.text = workshopsModel.description
            } else {
                itemView.workshop_list_description.text = "-"
            }
            val builder = itemView.workshop_list_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.workshop_list_image.shapeAppearanceModel = builder
            Glide.with(itemView.context).load(workshopsModel.imageLink)
                .into(itemView.workshop_list_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.workshop_card_view, parent, false)
        return WorkshopsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return workshopsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WorkshopsViewHolder).bind(workshopsListItems[position])

        val workshopListItem = workshopsListItems[position]

        holder.itemView.workshop_card_view.setOnClickListener {
            val bundle = bundleOf("workshop_id" to workshopListItem.id)
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.workshopInfoFragment, bundle)
        }
    }
}