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
import com.example.autopia.activities.model.News
import com.google.android.material.shape.CornerFamily
import kotlinx.android.synthetic.main.news_card_view.view.*

class NewsAdapter(var context: Context, var newsListItems: List<News>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(newsModel: News) {
            itemView.newsTitle.text = newsModel.title
            itemView.newsDate.text = newsModel.postedDate
            val builder = itemView.newsImage.shapeAppearanceModel.toBuilder()
                .setTopRightCorner(CornerFamily.ROUNDED, 40f)
                .setBottomRightCorner(CornerFamily.ROUNDED, 40f).build()
            itemView.newsImage.shapeAppearanceModel = builder
            Glide.with(itemView.context).load(newsModel.imageLink)
                .into(itemView.newsImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.news_card_view, parent, false)
        return NewsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return newsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NewsViewHolder).bind(newsListItems[position])

        val newsListItem = newsListItems[position]

        holder.itemView.setOnClickListener {
            val bundle = bundleOf(
                "image" to newsListItem.imageLink,
                "title" to newsListItem.title,
                "desc" to newsListItem.description,
                "date" to newsListItem.postedDate
            )
            Navigation.findNavController(holder.itemView)
                .navigate(R.id.newsDetailsFragment, bundle)
        }
    }
}