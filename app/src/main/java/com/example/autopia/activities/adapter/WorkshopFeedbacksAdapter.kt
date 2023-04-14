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
import com.example.autopia.activities.model.Feedbacks
import com.google.android.material.shape.CornerFamily
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.appointment_card_view.view.*
import kotlinx.android.synthetic.main.feedback_card_view.view.*

class WorkshopFeedbacksAdapter(var context: Context, var feedbackListItems: List<Feedbacks>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class FeedbacksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(feedbacksModel: Feedbacks) {
            FirestoreClass().fetchUserByID(feedbacksModel.clientId).addOnSuccessListener {
                itemView.feedback_client.text = String.format(it.data?.get("username").toString())
                itemView.feedback_stars.text = String.format(feedbacksModel.rating.toString())
                itemView.feedback_comments.text = String.format(feedbacksModel.comment)
                val builder = itemView.feedback_image.shapeAppearanceModel.toBuilder()
                    .setTopLeftCorner(CornerFamily.ROUNDED, 40f)
                    .setBottomLeftCorner(CornerFamily.ROUNDED, 40f).build()
                itemView.feedback_image.shapeAppearanceModel = builder
                Glide.with(itemView.context).load(it.data?.get("imageLink"))
                    .into(itemView.feedback_image)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_card_view, parent, false)
        return FeedbacksViewHolder(view)
    }

    override fun getItemCount(): Int {
        return feedbackListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FeedbacksViewHolder).bind(feedbackListItems[position])

        val feedbackListItem = feedbackListItems[position]

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchUserByID(user.uid).addOnSuccessListener { snapshot ->
                if (snapshot.data?.get("userType").toString() == "workshop") {
                    holder.itemView.setOnClickListener {
                        val bundle = bundleOf(
                            "feedback_id" to feedbackListItem.id,
                            "appointment_id" to feedbackListItem.appointmentId
                        )
                        Navigation.findNavController(holder.itemView)
                            .navigate(R.id.feedbackDetailsFragment, bundle)
                    }
                }
            }
        }
    }
}