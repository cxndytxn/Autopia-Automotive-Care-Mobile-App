package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Customers
import kotlinx.android.synthetic.main.customers_card_view.view.*

class CustomersAdapter(var context: Context, var customerListItems: List<Customers>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ServicesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(customersModel: Customers) {
            FirestoreClass().fetchUserByID(customersModel.clientId).addOnSuccessListener {
                itemView.client_name.text = String.format(it.get("username").toString())
                itemView.client_number.text =
                    String.format(it.get("contactNumber").toString())
                Glide.with(itemView.context).load(it.get("imageLink").toString())
                    .into(itemView.customer_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.customers_card_view, parent, false)
        return ServicesViewHolder(view)
    }

    override fun getItemCount(): Int {
        return customerListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ServicesViewHolder).bind(customerListItems[position])
    }
}