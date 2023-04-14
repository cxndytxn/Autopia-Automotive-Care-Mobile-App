package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.ConversationsAdapter
import com.example.autopia.activities.constants.Constants
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Conversations
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ConversationFragment : Fragment() {

    private lateinit var dbRef: DatabaseReference
    private var conversationList: MutableList<Conversations> = mutableListOf()
    private lateinit var conversationListAdapter: ConversationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val user = FirebaseAuth.getInstance().currentUser
        val view: View
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    if (it.result.data?.get("userType")?.equals("workshop") == true) {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
                        bottomNavigationView?.isVisible = true
                    } else {
                        val bottomNavigationView: BottomNavigationView? =
                            requireActivity().findViewById(R.id.bottom_navigation_view)
                        bottomNavigationView?.isVisible = true
                    }
                }
            }
        }
        return if (user != null) {
            view = inflater.inflate(R.layout.fragment_conversation, container, false)
            view
        } else {
            view = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = view.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
            view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView? = requireActivity().findViewById(R.id.chat_rv)

        conversationListAdapter =
            ConversationsAdapter(
                requireContext(),
                conversationList
            )
        dbRef =
            FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL).reference.child("conversations")
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirestoreClass().fetchUserByID(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    dbRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            conversationList.clear()
                            if (dataSnapshot.exists()) {
                                for (snapshot in dataSnapshot.children) {
                                    if (it.result.data?.get("userType")
                                            ?.equals("workshop") == true
                                    ) {
                                        if (isAdded) {
                                            val appBarLayout: AppBarLayout? =
                                                requireActivity().findViewById(R.id.workshop_app_bar_layout)
                                            appBarLayout?.elevation = 8f
                                        }
                                        for (snap in snapshot.children) {
                                            if (snap.exists()) {
                                                val timestamp =
                                                    snap.child("timestamp").value as Long?
                                                val userId = snap.child("userId").value as String?
                                                val workshopId =
                                                    snap.child("workshopId").value as String?
                                                val username =
                                                    snap.child("username").value as String?
                                                val workshopName =
                                                    snap.child("workshopName").value as String?
                                                val userImage =
                                                    snap.child("userImage").value as String?
                                                val workshopImage =
                                                    snap.child("workshopImage").value as String?
                                                val dateTime =
                                                    snap.child("dateTime").value as String?
                                                val latestMsg =
                                                    snap.child("latestMsg").value as String?
                                                val userReadStatus =
                                                    snap.child("userReadStatus").value as String?
                                                val workshopReadStatus =
                                                    snap.child("workshopReadStatus").value as String?
                                                val messageType =
                                                    snap.child("messageType").value as String?
                                                val conversation = Conversations(
                                                    userId,
                                                    workshopId,
                                                    username,
                                                    workshopName,
                                                    userImage,
                                                    workshopImage,
                                                    latestMsg,
                                                    dateTime,
                                                    userReadStatus,
                                                    workshopReadStatus,
                                                    returnedTimestamp = timestamp,
                                                    messageType = messageType
                                                )
                                                //search if matching receiver item had been registered into conversation list
                                                //to avoid showing same receiver repetitively
                                                val matching =
                                                    conversationList.find { item -> item.userId == conversation.userId }
                                                if (conversation.workshopId == user.uid && matching == null
                                                ) {
                                                    conversationList.add(conversation)
                                                    //sort the display of conversation by latest timestamp
                                                    conversationList.sortByDescending { list -> list.returnedTimestamp }
                                                }
                                            } else {
                                                val noConversationLayout: ConstraintLayout? =
                                                    requireActivity().findViewById(
                                                        R.id.noConversationLayout
                                                    )
                                                noConversationLayout?.visibility = View.VISIBLE
                                            }
                                        }
                                    } else {
                                        if (isAdded) {
                                            val appBarLayout: AppBarLayout? =
                                                requireActivity().findViewById(R.id.app_bar_layout)
                                            appBarLayout?.elevation = 8f
                                        }
                                        for (snap in snapshot.children) {
                                            if (snap.exists()) {
                                                val timestamp =
                                                    snap.child("timestamp").value as Long?
                                                val userId = snap.child("userId").value as String?
                                                val workshopId =
                                                    snap.child("workshopId").value as String?
                                                val username =
                                                    snap.child("username").value as String?
                                                val workshopName =
                                                    snap.child("workshopName").value as String?
                                                val userImage =
                                                    snap.child("userImage").value as String?
                                                val workshopImage =
                                                    snap.child("workshopImage").value as String?
                                                val dateTime =
                                                    snap.child("dateTime").value as String?
                                                val latestMsg =
                                                    snap.child("latestMsg").value as String?
                                                val userReadStatus =
                                                    snap.child("userReadStatus").value as String?
                                                val workshopReadStatus =
                                                    snap.child("workshopReadStatus").value as String?
                                                val messageType =
                                                    snap.child("messageType").value as String?
                                                val conversation = Conversations(
                                                    userId,
                                                    workshopId,
                                                    username,
                                                    workshopName,
                                                    userImage,
                                                    workshopImage,
                                                    latestMsg,
                                                    dateTime,
                                                    userReadStatus,
                                                    workshopReadStatus,
                                                    returnedTimestamp = timestamp,
                                                    messageType = messageType
                                                )
                                                val matching =
                                                    conversationList.find { item -> item.workshopId == conversation.workshopId }
                                                if (conversation.userId == user.uid && matching == null
                                                ) {
                                                    conversationList.add(conversation)
                                                    conversationList.sortByDescending { list -> list.returnedTimestamp }
                                                }
                                            } else {
                                                val noConversationLayout: ConstraintLayout? =
                                                    requireActivity().findViewById(
                                                        R.id.noConversationLayout
                                                    )
                                                noConversationLayout?.visibility = View.VISIBLE

                                            }
                                        }
                                    }
                                }
                            } else {
                                val noConversationLayout: ConstraintLayout? =
                                    requireActivity().findViewById(
                                        R.id.noConversationLayout
                                    )
                                noConversationLayout?.visibility = View.VISIBLE
                            }
                            if (isAdded) {
                                recyclerView?.adapter = conversationListAdapter
                                recyclerView?.layoutManager = LinearLayoutManager(view.context)
                                recyclerView?.setHasFixedSize(true)
                                conversationListAdapter.conversationListItems =
                                    conversationList
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
            }
        }
    }
}
