package com.example.autopia.activities.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toUri
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Conversations
import com.example.autopia.activities.model.Messages
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import com.google.firebase.storage.FirebaseStorage
import java.util.*


class ChatRoomUtils(
    val context: Context,
    private val dbRef: DatabaseReference,
    private val senderRoom: String,
    private val receiverRoom: String
) {

    fun uploadImageToFirebase(
        fileUri: Uri,
        mime: String
    ) {
        if (mime != "video/mp4") {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(
                    "chat_image",
                    Context.MODE_PRIVATE
                )
            val imageUri: String? = sharedPreferences.getString("chat_image", "")
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val refStorage = FirebaseStorage.getInstance().reference.child("chat/$fileName")
            if (imageUri != null) {
                refStorage.putFile(fileUri)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val url = it.toString()
                            val preferences: SharedPreferences =
                                context.getSharedPreferences(
                                    "uploaded_image",
                                    Context.MODE_PRIVATE
                                )
                            val editor = preferences.edit()
                            editor.putString("uploaded_image", url)
                            editor.apply()
                            val sharePreferences: SharedPreferences =
                                context.getSharedPreferences(
                                    "message_info",
                                    Context.MODE_PRIVATE
                                )
                            val senderId: String? = sharePreferences.getString("sender_id", "")
                            val receiverId: String? = sharePreferences.getString("receiver_id", "")
                            val formattedDateTime: String? =
                                sharePreferences.getString("formatted_date_time", "")
                            if (senderId != null && receiverId != null && formattedDateTime != null) {
                                uploadImage(senderId, formattedDateTime, receiverId)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Image could not be sent", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        } else {
            val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(
                    "chat_video",
                    Context.MODE_PRIVATE
                )
            val videoUri: String? = sharedPreferences.getString("chat_video", "")
            val fileName = UUID.randomUUID().toString() + ".mp4"
            val refStorage = FirebaseStorage.getInstance().reference.child("chat/$fileName")
            if (videoUri != null) {
                refStorage.putFile(fileUri)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                            val url = it.toString()
                            val preferences: SharedPreferences =
                                context.getSharedPreferences(
                                    "uploaded_video",
                                    Context.MODE_PRIVATE
                                )
                            val editor = preferences.edit()
                            editor.putString("uploaded_video", url)
                            editor.apply()
                            val sharePreferences: SharedPreferences =
                                context.getSharedPreferences(
                                    "message_info",
                                    Context.MODE_PRIVATE
                                )
                            val senderId: String? = sharePreferences.getString("sender_id", "")
                            val receiverId: String? = sharePreferences.getString("receiver_id", "")
                            val formattedDateTime: String? =
                                sharePreferences.getString("formatted_date_time", "")
                            if (senderId != null && receiverId != null && formattedDateTime != null) {
                                uploadVideo(senderId, formattedDateTime, receiverId)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Video could not be sent", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
        }
    }

    private fun uploadVideo(senderId: String, formattedDateTime: String, receiverId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(
                "uploaded_video",
                Context.MODE_PRIVATE
            )
        val videoUrl: String? = sharedPreferences.getString("uploaded_video", "")
        val senderMessageObject =
            Messages(senderId, videoUrl!!, formattedDateTime, "read", "video")
        val receiverMessageObject =
            Messages(senderId, videoUrl, formattedDateTime, "unread", "video")

        FirestoreClass().fetchUserByID(senderId)
            .addOnSuccessListener { senderSnapshot ->
                FirestoreClass().fetchUserByID(receiverId)
                    .addOnSuccessListener { receiverSnapshot ->
                        val conversationObject: Conversations =
                            if (senderSnapshot.data?.get("userType")
                                    ?.equals("workshop") == true
                            ) {
                                Conversations(
                                    receiverId,
                                    senderId,
                                    receiverSnapshot.data?.get("username")
                                        .toString(),
                                    senderSnapshot.data?.get("workshopName")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    videoUrl,
                                    formattedDateTime,
                                    "unread",
                                    "read",
                                    ServerValue.TIMESTAMP,
                                    messageType = "video"
                                )
                            } else {
                                Conversations(
                                    senderId,
                                    receiverId,
                                    senderSnapshot.data?.get("username")
                                        .toString(),
                                    receiverSnapshot.data?.get("workshopName")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    videoUrl,
                                    formattedDateTime,
                                    "read",
                                    "unread",
                                    ServerValue.TIMESTAMP,
                                    messageType = "video"
                                )
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("messages")
                            .push()
                            .setValue(senderMessageObject)
                            .addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(receiverMessageObject)
                                    .addOnSuccessListener {
                                        if (senderSnapshot.data?.get("userType")
                                                ?.equals("workshop") == true
                                        ) {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("workshopName")
                                                    .toString(),
                                                "Sent a video",
                                                receiverRoom,
                                                "workshop",
                                                receiverSnapshot.data?.get("username").toString()
                                            )
                                        } else {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("username")
                                                    .toString(),
                                                "Sent an video",
                                                receiverRoom,
                                                "user",
                                                receiverSnapshot.data?.get("workshopName")
                                                    .toString()
                                            )
                                        }
                                    }
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("info")
                            .removeValue().addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("info")
                                    .removeValue().addOnSuccessListener {
                                        dbRef.child("conversations")
                                            .child(senderRoom)
                                            .child("info")
                                            .setValue(conversationObject)
                                            .addOnSuccessListener {
                                                dbRef.child("conversations")
                                                    .child(receiverRoom)
                                                    .child("info")
                                                    .setValue(conversationObject)


                                            }
                                    }
                            }
                    }
            }
        val preferences: SharedPreferences =
            context.getSharedPreferences(
                "chat_video",
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        val editor2 = sharedPreferences.edit()
        editor2.clear()
        editor2.apply()
    }

    private fun uploadImage(senderId: String, formattedDateTime: String, receiverId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(
                "uploaded_image",
                Context.MODE_PRIVATE
            )
        val imageUrl: String? = sharedPreferences.getString("uploaded_image", "")
        val senderMessageObject =
            Messages(senderId, imageUrl!!, formattedDateTime, "read", "image")
        val receiverMessageObject =
            Messages(senderId, imageUrl, formattedDateTime, "unread", "image")

        FirestoreClass().fetchUserByID(senderId)
            .addOnSuccessListener { senderSnapshot ->
                FirestoreClass().fetchUserByID(receiverId)
                    .addOnSuccessListener { receiverSnapshot ->
                        val conversationObject: Conversations =
                            if (senderSnapshot.data?.get("userType")
                                    ?.equals("workshop") == true
                            ) {
                                Conversations(
                                    receiverId,
                                    senderId,
                                    receiverSnapshot.data?.get("username")
                                        .toString(),
                                    senderSnapshot.data?.get("workshopName")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    imageUrl,
                                    formattedDateTime,
                                    "unread",
                                    "read",
                                    ServerValue.TIMESTAMP,
                                    messageType = "image"
                                )
                            } else {
                                Conversations(
                                    senderId,
                                    receiverId,
                                    senderSnapshot.data?.get("username")
                                        .toString(),
                                    receiverSnapshot.data?.get("workshopName")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    imageUrl,
                                    formattedDateTime,
                                    "read",
                                    "unread",
                                    ServerValue.TIMESTAMP,
                                    messageType = "image"
                                )
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("messages")
                            .push()
                            .setValue(senderMessageObject)
                            .addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(receiverMessageObject)
                                    .addOnSuccessListener {
                                        if (senderSnapshot.data?.get("userType")
                                                ?.equals("workshop") == true
                                        ) {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("workshopName")
                                                    .toString(),
                                                "Sent a image",
                                                receiverRoom,
                                                "user",
                                                receiverSnapshot.data?.get("username").toString()
                                            )
                                        } else {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("username")
                                                    .toString(),
                                                "Sent an image",
                                                receiverRoom,
                                                "workshop",
                                                receiverSnapshot.data?.get("workshopName")
                                                    .toString()
                                            )
                                        }
                                    }
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("info")
                            .removeValue().addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("info")
                                    .removeValue().addOnSuccessListener {
                                        dbRef.child("conversations")
                                            .child(senderRoom)
                                            .child("info")
                                            .setValue(conversationObject)
                                            .addOnSuccessListener {
                                                dbRef.child("conversations")
                                                    .child(receiverRoom)
                                                    .child("info")
                                                    .setValue(conversationObject)


                                            }
                                    }
                            }
                    }
            }
        val preferences: SharedPreferences =
            context.getSharedPreferences(
                "chat_image",
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        val editor2 = sharedPreferences.edit()
        editor2.clear()
        editor2.apply()
    }

    fun uploadDocumentToFirebase(filename: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(
                "chat_document",
                Context.MODE_PRIVATE
            )
        val imageUri: String? = sharedPreferences.getString("chat_document", "")
        val refStorage = FirebaseStorage.getInstance().reference.child("chat/$filename")

        if (imageUri != null) {
            refStorage.putFile(imageUri.toUri())
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener {
                        val url = it.toString()
                        val preferences: SharedPreferences =
                            context.getSharedPreferences(
                                "uploaded_document",
                                Context.MODE_PRIVATE
                            )
                        val editor = preferences.edit()
                        editor.putString("uploaded_document", url)
                        editor.apply()
                        val sharePreferences: SharedPreferences =
                            context.getSharedPreferences(
                                "message_info",
                                Context.MODE_PRIVATE
                            )
                        val senderId: String? = sharePreferences.getString("sender_id", "")
                        val receiverId: String? = sharePreferences.getString("receiver_id", "")
                        val formattedDateTime: String? =
                            sharePreferences.getString("formatted_date_time", "")
                        if (senderId != null && receiverId != null && formattedDateTime != null) {
                            uploadDocument(senderId, formattedDateTime, receiverId)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Document could not be sent",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
        }
    }

    private fun uploadDocument(senderId: String, formattedDateTime: String, receiverId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(
                "uploaded_document",
                Context.MODE_PRIVATE
            )
        val documentUrl: String? = sharedPreferences.getString("uploaded_document", "")
        val senderMessageObject =
            Messages(senderId, documentUrl!!, formattedDateTime, "read", "document")
        val receiverMessageObject =
            Messages(senderId, documentUrl, formattedDateTime, "unread", "document")

        FirestoreClass().fetchUserByID(senderId)
            .addOnSuccessListener { senderSnapshot ->
                FirestoreClass().fetchUserByID(receiverId)
                    .addOnSuccessListener { receiverSnapshot ->
                        val conversationObject: Conversations =
                            if (senderSnapshot.data?.get("userType")
                                    ?.equals("workshop") == true
                            ) {
                                Conversations(
                                    receiverId,
                                    senderId,
                                    receiverSnapshot.data?.get("username")
                                        .toString(),
                                    senderSnapshot.data?.get("workshopName")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    documentUrl,
                                    formattedDateTime,
                                    "unread",
                                    "read",
                                    ServerValue.TIMESTAMP,
                                    messageType = "document"
                                )
                            } else {
                                Conversations(
                                    senderId,
                                    receiverId,
                                    senderSnapshot.data?.get("username")
                                        .toString(),
                                    receiverSnapshot.data?.get("workshopName")
                                        .toString(),
                                    senderSnapshot.data?.get("imageLink")
                                        .toString(),
                                    receiverSnapshot.data?.get("imageLink")
                                        .toString(),
                                    documentUrl,
                                    formattedDateTime,
                                    "read",
                                    "unread",
                                    ServerValue.TIMESTAMP,
                                    messageType = "document"
                                )
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("messages")
                            .push()
                            .setValue(senderMessageObject)
                            .addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .push()
                                    .setValue(receiverMessageObject)
                                    .addOnSuccessListener {
                                        if (senderSnapshot.data?.get("userType")
                                                ?.equals("workshop") == true
                                        ) {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("workshopName")
                                                    .toString(),
                                                "Sent a document",
                                                receiverRoom,
                                                "user",
                                                receiverSnapshot.data?.get("username").toString()
                                            )
                                        } else {
                                            OneSignalNotificationService().createChatNotification(
                                                receiverId,
                                                senderSnapshot.data?.get("username")
                                                    .toString(),
                                                "Sent a document",
                                                receiverRoom,
                                                "workshop",
                                                receiverSnapshot.data?.get("workshopName")
                                                    .toString()
                                            )
                                        }
                                    }
                            }

                        dbRef.child("conversations").child(senderRoom)
                            .child("info")
                            .removeValue().addOnSuccessListener {
                                dbRef.child("conversations")
                                    .child(receiverRoom)
                                    .child("info")
                                    .removeValue().addOnSuccessListener {
                                        dbRef.child("conversations")
                                            .child(senderRoom)
                                            .child("info")
                                            .setValue(conversationObject)
                                            .addOnSuccessListener {
                                                dbRef.child("conversations")
                                                    .child(receiverRoom)
                                                    .child("info")
                                                    .setValue(conversationObject)
                                            }
                                    }
                            }
                    }
            }
        val preferences: SharedPreferences =
            context.getSharedPreferences(
                "chat_document",
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
        val editor2 = sharedPreferences.edit()
        editor2.clear()
        editor2.apply()
    }
}