package com.example.autopia.activities.adapter

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.model.Messages
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.stfalcon.imageviewer.StfalconImageViewer

class MessagesAdapter(var context: Context, var messagesListItems: List<Messages>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        val sentMessage: TextView = itemView.findViewById(R.id.send_msg)
        val sentDate: TextView = itemView.findViewById(R.id.send_date_time)
        val sentImg: ImageView = itemView.findViewById(R.id.send_img)
        val sentImgDate: TextView = itemView.findViewById(R.id.send_img_date_time)
        val sentImgCard: CardView = itemView.findViewById(R.id.send_img_card)
        val sentVideoCard: CardView = itemView.findViewById(R.id.send_video_card)
        var sentVideo: PlayerView = itemView.findViewById(R.id.send_video)
        val sentVideoDate: TextView = itemView.findViewById(R.id.send_video_date_time)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.receive_msg)
        val receivedDate: TextView = itemView.findViewById(R.id.receive_date_time)
        val receivedImg: ImageView = itemView.findViewById(R.id.receive_img)
        val receivedImgDate: TextView = itemView.findViewById(R.id.receive_img_date_time)
        val receivedImgCard: CardView = itemView.findViewById(R.id.receive_img_card)
        val receivedVideoCard: CardView = itemView.findViewById(R.id.receive_video_card)
        var receivedVideo: PlayerView = itemView.findViewById(R.id.receive_video)
        val receivedVideoDate: TextView = itemView.findViewById(R.id.receive_video_date_time)
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
            when (currentMessage.messageType) {
                "text" -> {
                    holder.sentMessage.text = currentMessage.message
                    holder.sentDate.text = currentMessage.dateTime
                }
                "image" -> {
                    holder.sentMessage.visibility = View.GONE
                    holder.sentDate.visibility = View.GONE
                    holder.sentVideo.visibility = View.GONE
                    viewHolder.sentImgDate.visibility = View.VISIBLE
                    viewHolder.sentImgCard.visibility = View.VISIBLE
                    viewHolder.sentImg.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(currentMessage.message)
                        .into(viewHolder.sentImg)
                    holder.sentImgDate.text = currentMessage.dateTime
                    viewHolder.sentImg.setOnClickListener {
                        val images = listOf(currentMessage.message)
                        StfalconImageViewer.Builder(context, images) { imageView, image ->
                            Picasso.get().load(image.toString().toUri()).into(imageView)
                        }.withBackgroundColor(context.resources.getColor(R.color.black70)).show()
                    }
                }
                "document" -> {
                    holder.sentMessage.isClickable = true
                    holder.sentMessage.setTypeface(null, Typeface.BOLD)
                    holder.sentMessage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    holder.sentMessage.setTextColor(context.resources.getColor(R.color.indigo_300))
                    holder.sentMessage.text = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(currentMessage.message).name
                    holder.sentDate.text = currentMessage.dateTime
                    holder.sentMessage.setOnClickListener {
                        val pdfIntent = Intent(Intent.ACTION_VIEW)
                        pdfIntent.setDataAndType(
                            currentMessage.message.toUri(),
                            "application/pdf"
                        )
                        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        try {
                            context.startActivity(pdfIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "No Application available to view PDF",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                "video" -> {
                    holder.sentMessage.visibility = View.GONE
                    holder.sentDate.visibility = View.GONE
                    holder.sentImg.visibility = View.GONE
                    viewHolder.sentVideo.visibility = View.VISIBLE
                    viewHolder.sentVideoDate.visibility = View.VISIBLE
                    viewHolder.sentVideoCard.visibility = View.VISIBLE
                    viewHolder.sentVideoDate.text = currentMessage.dateTime
                    val player = ExoPlayer.Builder(context).build()
                    viewHolder.sentVideo.player = player
                    val mediaItem = MediaItem.fromUri(currentMessage.message)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                }
            }
        } else {
            val viewHolder = holder as MessagesAdapter.ReceiveViewHolder
            when (currentMessage.messageType) {
                "text" -> {
                    holder.receivedMessage.text = currentMessage.message
                    holder.receivedDate.text = currentMessage.dateTime
                }
                "image" -> {
                    holder.receivedMessage.visibility = View.GONE
                    holder.receivedDate.visibility = View.GONE
                    holder.receivedVideo.visibility = View.GONE
                    viewHolder.receivedImgDate.visibility = View.VISIBLE
                    viewHolder.receivedImgCard.visibility = View.VISIBLE
                    viewHolder.receivedImg.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context).load(currentMessage.message)
                        .into(viewHolder.receivedImg)
                    holder.receivedImgDate.text = currentMessage.dateTime
                    viewHolder.receivedImg.setOnClickListener {
                        val images = listOf(currentMessage.message)
                        StfalconImageViewer.Builder(context, images) { imageView, image ->
                            Picasso.get().load(image.toString().toUri()).into(imageView)
                        }.withBackgroundColor(context.resources.getColor(R.color.black70)).show()
                    }
                }
                "document" -> {
                    holder.receivedMessage.isClickable = true
                    holder.receivedMessage.setTypeface(null, Typeface.BOLD)
                    holder.receivedMessage.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    holder.receivedMessage.setTextColor(context.resources.getColor(R.color.indigo_300))
                    holder.receivedMessage.text = FirebaseStorage.getInstance()
                        .getReferenceFromUrl(currentMessage.message).name
                    holder.receivedDate.text = currentMessage.dateTime
                    holder.receivedMessage.setOnClickListener {
                        val pdfIntent = Intent(Intent.ACTION_VIEW)
                        pdfIntent.setDataAndType(
                            currentMessage.message.toUri(),
                            "application/pdf"
                        )
                        pdfIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        try {
                            context.startActivity(pdfIntent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                "No Application available to view PDF",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                "video" -> {
                    holder.receivedMessage.visibility = View.GONE
                    holder.receivedDate.visibility = View.GONE
                    holder.receivedImg.visibility = View.GONE
                    viewHolder.receivedVideo.visibility = View.VISIBLE
                    viewHolder.receivedVideoDate.visibility = View.VISIBLE
                    viewHolder.receivedVideoCard.visibility = View.VISIBLE
                    viewHolder.receivedVideoDate.text = currentMessage.dateTime
                    val player = ExoPlayer.Builder(context).build()
                    viewHolder.receivedVideo.player = player
                    val mediaItem = MediaItem.fromUri(currentMessage.message)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                }
            }
        }
    }
}