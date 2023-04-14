package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.model.Notifications
import kotlinx.android.synthetic.main.notification_card_view.view.*

class NotificationsAdapter(var context: Context, var notificationList: List<Notifications>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(notificationsModel: Notifications) {
            itemView.notification_heading.text = notificationsModel.headings
            itemView.notification_subtitle.text = notificationsModel.subtitle
            itemView.notification_time.text = notificationsModel.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_card_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(notificationList[position])
    }
}