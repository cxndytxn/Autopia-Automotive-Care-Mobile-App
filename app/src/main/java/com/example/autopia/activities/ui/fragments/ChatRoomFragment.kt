package com.example.autopia.activities.ui.fragments

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.MessagesAdapter
import com.example.autopia.activities.constants.Constants
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Conversations
import com.example.autopia.activities.model.Messages
import com.example.autopia.activities.utils.ChatRoomUtils
import com.example.autopia.activities.utils.OneSignalNotificationService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class ChatRoomFragment : Fragment() {

    private val REQUEST_EXTERNAL_STORAGE = 1
    private val PERMISSION_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private lateinit var messageAdapter: MessagesAdapter
    private lateinit var messageList: ArrayList<Messages>
    private lateinit var dbRef: DatabaseReference
    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    lateinit var imageUrl: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return inflater.inflate(R.layout.fragment_chat_room, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType")?.equals("workshop") == true) {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
                        bottomNavigationView?.isVisible = false
                    } else {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.bottom_navigation_view)
                        bottomNavigationView?.isVisible = false
                    }
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            FirestoreClass().fetchUserByID(user.uid).addOnCompleteListener {
//                if (it.isSuccessful) {
//                    if (it.result.data?.get("userType")?.equals("user") == true) {
//                        inflater.inflate(R.menu.chat_bot_menu, menu)
//                        menu.findItem(R.id.action_chat_bot).setOnMenuItemClickListener {
//                            val receiverId = arguments?.getString("receiver_id")
//                            val bundle = bundleOf("workshop_id" to receiverId)
//                            findNavController().navigate(R.id.chatBotActivity, bundle)
//                            true
//                        }
//                    }
//                }
//            }
//        }
//        super.onCreateOptionsMenu(menu, inflater)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //setHasOptionsMenu(true)
        val recyclerView: RecyclerView? = requireActivity().findViewById(R.id.chat_room_rv)
        val inputField: EditText? = requireActivity().findViewById(R.id.chat_room_input)
        val sendButton: MaterialButton? = requireActivity().findViewById(R.id.send_button)
        val attachmentButton: MaterialButton? =
            requireActivity().findViewById(R.id.attachment_button)

        messageList = ArrayList()
        messageAdapter = MessagesAdapter(requireContext(), messageList)

        val senderId = FirebaseAuth.getInstance().currentUser?.uid
        val receiverId = arguments?.getString("receiver_id")

        FirestoreClass().fetchUserByID(senderId!!).addOnSuccessListener { senderSnapshot ->
            if (senderSnapshot.data?.get("userType")?.equals("workshop") == true) {
                val workshopToolbar: Toolbar? = requireActivity().findViewById(R.id.workshop_toolbar)
                workshopToolbar?.title = arguments?.getString("receiver_name")
            } else {
                val userToolbar: Toolbar? = requireActivity().findViewById(R.id.toolbar)
                userToolbar?.title = arguments?.getString("receiver_name")
            }
        }

        senderRoom = receiverId + senderId
        receiverRoom = senderId + receiverId

        dbRef = FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL).reference

        dbRef.child("conversations").child(senderRoom!!).child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (postSnapshot in snapshot.children) {
                        val message = postSnapshot.getValue(Messages::class.java)
                        messageList.add(message!!)
                    }
                    val llm = LinearLayoutManager(context)
                    llm.stackFromEnd = true
                    recyclerView?.adapter = messageAdapter
                    recyclerView?.layoutManager = llm
                    recyclerView?.setHasFixedSize(true)
                    messageAdapter.messagesListItems = messageList
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        sendButton?.setOnClickListener {
            val message = inputField?.text.toString().trim()
            if (message.isNotEmpty()) {
                val dateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                val formattedDateTime = dateTime.format(formatter)
                val senderMessageObject =
                    Messages(senderId, message, formattedDateTime, "read", "text")
                val receiverMessageObject =
                    Messages(senderId, message, formattedDateTime, "unread", "text")

                FirestoreClass().fetchUserByID(senderId).addOnSuccessListener { senderSnapshot ->
                    if (receiverId != null) {
                        FirestoreClass().fetchUserByID(receiverId)
                            .addOnSuccessListener { receiverSnapshot ->
                                val conversationObject: Conversations =
                                    if (senderSnapshot.data?.get("userType")
                                            ?.equals("workshop") == true
                                    ) {
                                        Conversations(
                                            receiverId,
                                            senderId,
                                            receiverSnapshot.data?.get("username").toString(),
                                            senderSnapshot.data?.get("workshopName").toString(),
                                            receiverSnapshot.data?.get("imageLink").toString(),
                                            senderSnapshot.data?.get("imageLink").toString(),
                                            message,
                                            formattedDateTime,
                                            "unread",
                                            "read",
                                            ServerValue.TIMESTAMP,
                                            messageType = "text"
                                        )
                                    } else {
                                        Conversations(
                                            senderId,
                                            receiverId,
                                            senderSnapshot.data?.get("username").toString(),
                                            receiverSnapshot.data?.get("workshopName").toString(),
                                            senderSnapshot.data?.get("imageLink").toString(),
                                            receiverSnapshot.data?.get("imageLink").toString(),
                                            message,
                                            formattedDateTime,
                                            "read",
                                            "unread",
                                            ServerValue.TIMESTAMP,
                                            messageType = "text"
                                        )
                                    }

                                dbRef.child("conversations").child(senderRoom!!).child("messages")
                                    .push()
                                    .setValue(senderMessageObject).addOnSuccessListener {
                                        dbRef.child("conversations").child(receiverRoom!!)
                                            .child("messages")
                                            .push()
                                            .setValue(receiverMessageObject).addOnSuccessListener {
                                                if (senderSnapshot.data?.get("userType")
                                                        ?.equals("workshop") == true
                                                ) {
                                                    OneSignalNotificationService().createChatNotification(
                                                        receiverId,
                                                        senderSnapshot.data?.get("workshopName")
                                                            .toString(),
                                                        message,
                                                        receiverRoom!!,
                                                        "user",
                                                        receiverSnapshot.data?.get("username")
                                                            .toString()
                                                    )
                                                } else {
                                                    OneSignalNotificationService().createChatNotification(
                                                        receiverId,
                                                        senderSnapshot.data?.get("username")
                                                            .toString(),
                                                        message,
                                                        receiverRoom!!,
                                                        "workshop",
                                                        receiverSnapshot.data?.get("workshopName")
                                                            .toString()
                                                    )
                                                }
                                            }
                                    }
                                inputField?.setText("")

                                dbRef.child("conversations").child(senderRoom!!).child("info")
                                    .removeValue().addOnSuccessListener {
                                        dbRef.child("conversations").child(receiverRoom!!)
                                            .child("info")
                                            .removeValue().addOnSuccessListener {
                                                dbRef.child("conversations").child(senderRoom!!)
                                                    .child("info")
                                                    .setValue(conversationObject)
                                                    .addOnSuccessListener {
                                                        dbRef.child("conversations")
                                                            .child(receiverRoom!!)
                                                            .child("info")
                                                            .setValue(conversationObject)

                                                    }
                                            }
                                    }
                            }
                    }
                }
            }
        }

        attachmentButton?.setOnClickListener {
            val dialog = BottomSheetDialog(requireContext())
            val bottomSheetDialogView =
                layoutInflater.inflate(R.layout.fragment_bottom_sheet_dialog, null)
            dialog.setContentView(bottomSheetDialogView)
            dialog.show()

            if (dialog.isShowing) {
                val documentButton: MaterialButton? =
                    bottomSheetDialogView.findViewById(R.id.document_button)
                documentButton?.setOnClickListener {
                    val dateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                    val formattedDateTime = dateTime.format(formatter)
                    selectDocument()
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "message_info",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()
                    editor.putString("sender_id", senderId)
                    editor.putString("receiver_id", receiverId)
                    editor.putString("formatted_date_time", formattedDateTime)
                    editor.apply()
                    dialog.dismiss()
                }

                val imageButton: MaterialButton? =
                    bottomSheetDialogView.findViewById(R.id.image_button)
                imageButton?.setOnClickListener {
                    val dateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
                    val formattedDateTime = dateTime.format(formatter)

                    selectImageOrVideo()
                    val preferences: SharedPreferences =
                        requireContext().getSharedPreferences(
                            "message_info",
                            Context.MODE_PRIVATE
                        )
                    val editor = preferences.edit()
                    editor.putString("sender_id", senderId)
                    editor.putString("receiver_id", receiverId)
                    editor.putString("formatted_date_time", formattedDateTime)
                    editor.apply()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun selectDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/pdf"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            200
        )
    }

    private fun selectImageOrVideo() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        //intent.type = "images/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            100
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            imageUrl = data?.data!!
            val cr: ContentResolver = requireActivity().contentResolver
            val mime = cr.getType(imageUrl)
            if (mime != null && senderRoom != null && receiverRoom != null) {
                ChatRoomUtils(
                    requireContext(),
                    dbRef,
                    senderRoom!!,
                    receiverRoom!!
                ).uploadImageToFirebase(imageUrl, mime)
            }
            if (mime != "video/mp4") {
                val preferences: SharedPreferences =
                    requireContext().getSharedPreferences(
                        "chat_image",
                        Context.MODE_PRIVATE
                    )
                val editor = preferences.edit()
                editor.putString("chat_image", data.data!!.toString())
                editor.apply()
            } else {
                val preferences: SharedPreferences =
                    requireContext().getSharedPreferences(
                        "chat_video",
                        Context.MODE_PRIVATE
                    )
                val editor = preferences.edit()
                editor.putString("chat_video", data.data!!.toString())
                editor.apply()
            }
        } else if (requestCode == 200 && resultCode != AppCompatActivity.RESULT_CANCELED) {
            val cursor =
                requireContext().contentResolver.query(data?.data!!, null, null, null, null)
            cursor?.moveToFirst()
            val fileName =
                cursor?.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    .toString()
            cursor?.close()
            ChatRoomUtils(
                requireContext(),
                dbRef,
                senderRoom!!,
                receiverRoom!!
            ).uploadDocumentToFirebase(fileName)
            val preferences: SharedPreferences =
                requireContext().getSharedPreferences(
                    "chat_document",
                    Context.MODE_PRIVATE
                )
            val editor = preferences.edit()
            editor.putString("chat_document", data.data!!.toString())
            editor.apply()
        }
    }
}