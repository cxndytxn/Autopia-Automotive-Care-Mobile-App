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
import com.example.autopia.activities.model.Vehicles
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.vehicle_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VehiclesAdapter(var context: Context, var vehiclesListItems: List<Vehicles>) :
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
            val bundle = bundleOf("vehicle_id" to vehicleListItem.id)
            Navigation.findNavController(holder.itemView).navigate(R.id.addVehicleFragment, bundle)
        }

        holder.itemView.vehicle_card_view.delete_vehicle_button.setOnClickListener {
            vehicleListItem.id?.let { it1 ->
                ApiInterface.create().deleteVehicles(
                    it1
                )
            }?.enqueue(object : Callback<Vehicles> {
                override fun onResponse(
                    call: Call<Vehicles>,
                    response: Response<Vehicles>,
                ) {
                    Log.d("Response", response.code().toString())
                    Toast.makeText(context, "Vehicle is deleted!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(call: Call<Vehicles>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error. Vehicle could not be deleted. " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}

