package com.example.autopia.activities.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.model.Messages
import com.google.firebase.auth.FirebaseAuth

class ChatBotAdapter(var context: Context, var messagesListItems: List<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        val sentMessage: TextView = itemView.findViewById(R.id.send_msg)
        val sentDate: TextView = itemView.findViewById(R.id.send_date_time)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.receive_msg)
        val receivedDate: TextView = itemView.findViewById(R.id.receive_date_time)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messagesListItems[position]

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        return if (uid.equals(currentMessage.senderId)) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 1) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.receiver_chat_bubble, parent, false)
            ReceiveViewHolder(view)
        } else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.sender_chat_bubble, parent, false)
            SentViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messagesListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messagesListItems[position]

        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
            holder.sentDate.text = currentMessage.dateTime
        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.receivedMessage.text = currentMessage.message
            holder.receivedDate.text = currentMessage.dateTime
        }
    }
}
