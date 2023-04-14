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
import com.example.autopia.activities.model.Products
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*
import kotlinx.android.synthetic.main.workshop_products_card.view.*
import kotlinx.android.synthetic.main.workshop_services_card.view.*

class WorkshopProductsAdapter(var context: Context, var productListItems: List<Products>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(productsModel: Products) {
            itemView.workshop_product_name.text = String.format(productsModel.name)
            itemView.workshop_product_description.text =
                String.format(productsModel.description)
            if (productsModel.price == 0.0) {
                "Free".also { itemView.workshop_product_price.text = it }
            } else {
                itemView.workshop_product_price.text =
                    String.format("RM" + productsModel.price)
            }
            val builder = itemView.workshop_product_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.workshop_product_image.shapeAppearanceModel = builder
            Glide.with(itemView.context).load(productsModel.imageLink)
                .into(itemView.workshop_product_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.workshop_products_card, parent, false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProductsViewHolder).bind(productListItems[position])

        val product = productListItems[position]

//        holder.itemView.setOnClickListener {
//            val bundle = bundleOf(
//                "title" to product.name,
//                "description" to product.description,
//                "image" to product.imageLink
//            )
//            Navigation.findNavController(holder.itemView)
//                .navigate(R.id.productDetailsFragment, bundle)
//        }
    }
}