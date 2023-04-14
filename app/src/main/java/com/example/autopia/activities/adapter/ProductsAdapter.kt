package com.example.autopia.activities.adapter

import android.content.Context
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
import com.example.autopia.activities.model.Products
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.news_card_view.view.*
import kotlinx.android.synthetic.main.product_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductsAdapter(var context: Context, var productListItems: List<Products>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ProductsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(productsModel: Products) {
            itemView.product_name.text = String.format(productsModel.name)
            itemView.product_description.text =
                String.format(productsModel.description)
            itemView.product_price.text =
                String.format("RM: " + productsModel.price.toString())
            val builder = itemView.product_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.product_image.shapeAppearanceModel = builder
            if (productsModel.imageLink != null && productsModel.imageLink != "") {
                Glide.with(itemView.context).load(productsModel.imageLink)
                    .into(itemView.product_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.product_card_view, parent, false)
        return ProductsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return productListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProductsViewHolder).bind(productListItems[position])

        val product = productListItems[position]

        holder.itemView.product_card_view.setOnClickListener {
            val bundle = bundleOf("product_id" to product.id)
            Navigation.findNavController(holder.itemView).navigate(R.id.addProductFragment, bundle)
        }

        holder.itemView.product_card_view.delete_product_button.setOnClickListener {
            product.id?.let { it1 ->
                ApiInterface.create().deleteProducts(it1)
            }?.enqueue(object : Callback<Products> {
                override fun onResponse(
                    call: Call<Products>,
                    response: Response<Products>,
                ) {
                    Toast.makeText(context, "Product is deleted!", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onFailure(call: Call<Products>, t: Throwable) {
                    Toast.makeText(
                        context,
                        "Error. Product could not be deleted. " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
}