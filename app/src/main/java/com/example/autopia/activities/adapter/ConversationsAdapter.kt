package com.example.autopia.activities.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.constants.Constants
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Conversations
import com.example.autopia.activities.ui.NavigationDrawerActivity
import com.example.autopia.activities.ui.WorkshopNavigationDrawerActivity
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.chat_card_view.view.*

class ConversationsAdapter(var context: Context, var conversationListItems: List<Conversations>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class ConversationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context

        fun bind(conversationsModel: Conversations) {
            itemView.chat_date_time.text = conversationsModel.dateTime
            when (conversationsModel.messageType) {
                "text" -> itemView.chat_message.text = conversationsModel.latestMsg
                "image" -> "Sent an image".also { itemView.chat_message.text = it }
                "document" -> "Sent a PDF document".also { itemView.chat_message.text = it }
                "video" -> "Sent a video".also { itemView.chat_message.text = it }
            }
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            FirestoreClass().fetchUserByID(userId!!).addOnSuccessListener {
                if (it.data?.get("userType")?.equals("workshop") == true) {
                    itemView.chat_sender.text = conversationsModel.username
                    Glide.with(itemView.context).load(conversationsModel.userImage)
                        .into(itemView.chat_image)
                    val bottomNavigationView: BottomNavigationView =
                        (context as WorkshopNavigationDrawerActivity).findViewById(R.id.workshop_bottom_navigation_view)
                    val badge: BadgeDrawable =
                        bottomNavigationView.getOrCreateBadge(R.id.chatFragment)
                    if (conversationsModel.workshopReadStatus == "unread") {
                        itemView.chat_background.setBackgroundColor(context.getColor(R.color.indigo_100))
                        badge.isVisible = true
                    } else {
                        badge.isVisible = false
                    }
                } else {
                    itemView.chat_sender.text = conversationsModel.workshopName
                    Glide.with(itemView.context).load(conversationsModel.workshopImage)
                        .into(itemView.chat_image)
                    val bottomNavigationView: BottomNavigationView =
                        (context as NavigationDrawerActivity).findViewById(R.id.bottom_navigation_view)
                    val badge: BadgeDrawable =
                        bottomNavigationView.getOrCreateBadge(R.id.chatFragment)
                    if (conversationsModel.userReadStatus == "unread") {
                        itemView.chat_background.setBackgroundColor(context.getColor(R.color.indigo_100))
                        badge.isVisible = true
                    } else {
                        badge.isVisible = false
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.chat_card_view, parent, false)
        return ConversationsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return conversationListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ConversationsViewHolder).bind(conversationListItems[position])
        val dbRef = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL).reference

        val conListItem = conversationListItems[position]
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        holder.itemView.setOnClickListener {
            if (uid != null) {
                FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
                    val bundle: Bundle
                    if (it.isSuccessful) {
                        if (it.result.data?.get("userType")?.equals("user") == true) {
                            //sender room
                            val messageRef = dbRef.child("conversations")
                                .child(conListItem.workshopId + conListItem.userId)
                                .child("messages")
                            val infoRef = dbRef.child("conversations")
                                .child(conListItem.workshopId + conListItem.userId)
                                .child("info")
                            val oppositeInfoRef = dbRef.child("conversations")
                                .child(conListItem.userId + conListItem.workshopId)
                                .child("info")
                            messageRef.addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (snap in snapshot.children) {
                                        val readStatus = snap.child("readStatus").value as String?
                                        Log.d("readStatus", readStatus.toString())
                                        val messageMap: MutableMap<String, Any> = HashMap()
                                        messageMap["readStatus"] = "read"
                                        messageRef.child(snap.key!!).updateChildren(messageMap)
                                        val infoMap: MutableMap<String, Any> = HashMap()
                                        infoMap["userReadStatus"] = "read"
                                        infoRef.updateChildren(infoMap)
                                        val oppositeInfoMap: MutableMap<String, Any> = HashMap()
                                        oppositeInfoMap["userReadStatus"] = "read"
                                        oppositeInfoRef.updateChildren(oppositeInfoMap)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                            bundle = bundleOf(
                                "receiver_name" to conListItem.workshopName,
                                "receiver_id" to conListItem.workshopId
                            )
                            Navigation.findNavController(holder.itemView)
                                .navigate(R.id.chatRoomFragment, bundle)
                        } else {
                            val messageRef = dbRef.child("conversations")
                                .child(conListItem.userId + conListItem.workshopId)
                                .child("messages")
                            val infoRef = dbRef.child("conversations")
                                .child(conListItem.userId + conListItem.workshopId)
                                .child("info")
                            val oppositeInfoRef = dbRef.child("conversations")
                                .child(conListItem.workshopId + conListItem.userId)
                                .child("info")
                            messageRef.addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (snap in snapshot.children) {
                                        val readStatus = snap.child("readStatus").value as String?
                                        Log.d("readStatus", readStatus.toString())
                                        val messageMap: MutableMap<String, Any> = HashMap()
                                        messageMap["readStatus"] = "read"
                                        messageRef.child(snap.key!!).updateChildren(messageMap)
                                        val infoMap: MutableMap<String, Any> = HashMap()
                                        infoMap["workshopReadStatus"] = "read"
                                        infoRef.updateChildren(infoMap)
                                        val oppositeInfoMap: MutableMap<String, Any> = HashMap()
                                        oppositeInfoMap["workshopReadStatus"] = "read"
                                        oppositeInfoRef.updateChildren(oppositeInfoMap)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                            bundle = bundleOf(
                                "receiver_name" to conListItem.username,
                                "receiver_id" to conListItem.userId
                            )
                            Navigation.findNavController(holder.itemView)
                                .navigate(R.id.chatRoomFragment, bundle)
                        }
                    }
                }
            }
        }
    }
}