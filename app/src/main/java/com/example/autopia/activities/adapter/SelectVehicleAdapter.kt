package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.model.Vehicles
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.vehicle_card_view.view.*

class SelectVehicleAdapter(var context: Context, var vehiclesListItems: List<Vehicles>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class VehiclesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(vehiclesModel: Vehicles) {
            itemView.vehicle_plate.text = String.format("Plate No: " + vehiclesModel.plateNo)
            itemView.vehicle_model.text = String.format("Model: " + vehiclesModel.model)
            itemView.vehicle_mileage.text =
                String.format("Mileage: " + vehiclesModel.currentMileage)
            itemView.vehicle_manufacturer.text =
                String.format("Manufacturer: " + vehiclesModel.manufacturer)
            itemView.vehicle_year.text = String.format("Year: " + vehiclesModel.purchaseYear)
            val builder = itemView.vehicle_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.vehicle_image.shapeAppearanceModel = builder
            if (vehiclesModel.imageLink != null && vehiclesModel.imageLink != "") {
                Glide.with(itemView.context).load(vehiclesModel.imageLink)
                    .into(itemView.vehicle_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.vehicle_card_view, parent, false)
        return VehiclesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return vehiclesListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VehiclesViewHolder).bind(vehiclesListItems[position])

        val vehicleListItem = vehiclesListItems[position]

        holder.itemView.vehicle_card_view.setOnClickListener {
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "vehicle_id",
                vehicleListItem.id
            )
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "vehicle_plate",
                vehicleListItem.plateNo
            )
            Navigation.findNavController(holder.itemView).previousBackStackEntry?.savedStateHandle?.set(
                "vehicle_image_link",
                vehicleListItem.imageLink
            )
            Navigation.findNavController(holder.itemView).popBackStack()
        }

        holder.itemView.vehicle_card_view.delete_vehicle_button.visibility = View.GONE
    }
}