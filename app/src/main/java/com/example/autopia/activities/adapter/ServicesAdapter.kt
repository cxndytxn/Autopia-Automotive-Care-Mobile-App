package com.example.autopia.activities.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.model.Services
import com.google.android.material.button.MaterialButton
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.news_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*
import kotlinx.android.synthetic.main.workshop_services_card.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ServicesAdapter(var context: Context, var serviceListItems: List<Services>) :
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
            val bundle = bundleOf("service_id" to serviceListItem.id)
            Navigation.findNavController(holder.itemView).navigate(R.id.addServiceFragment, bundle)
        }

        holder.itemView.service_card_view.delete_service_button.setOnClickListener {
            serviceListItem.id?.let { it1 ->
                ApiInterface.create().deleteServices(
                    it1
                )
            }?.enqueue(object : Callback<Services> {
                override fun onResponse(
                    call: Call<Services>,
                    response: Response<Services>,
                ) {
                    Log.d("Response", response.code().toString())
                    Toast.makeText(context, "Service is deleted!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(call: Call<Services>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error. Service could not be deleted. " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}