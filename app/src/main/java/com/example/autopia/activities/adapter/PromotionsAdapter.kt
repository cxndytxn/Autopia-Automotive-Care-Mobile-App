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
import com.example.autopia.activities.model.Promotions
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.product_card_view.view.*
import kotlinx.android.synthetic.main.promotion_card_view.view.*
import kotlinx.android.synthetic.main.service_card_view.view.*

class PromotionsAdapter(var context: Context, var promotionListItems: List<Promotions>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class PromotionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(promotionsModel: Promotions) {
            itemView.promotion_title.text = String.format(promotionsModel.title)
            itemView.promotion_content.text = String.format(promotionsModel.description)
            itemView.promotion_date_time.text =
                String.format(promotionsModel.dateTime)
            val builder = itemView.promotion_image.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.promotion_image.shapeAppearanceModel = builder
            if (promotionsModel.imageLink != null && promotionsModel.imageLink != "")
                Glide.with(itemView.context).load(promotionsModel.imageLink)
                    .into(itemView.promotion_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.promotion_card_view, parent, false)
        return PromotionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return promotionListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PromotionsViewHolder).bind(promotionListItems[position])

        val promotionListItem = promotionListItems[position]

        holder.itemView.setOnClickListener {
            val bundle = bundleOf("promotion_id" to promotionListItem.id)
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.promotionDetailsFragment, bundle)
        }
    }
}