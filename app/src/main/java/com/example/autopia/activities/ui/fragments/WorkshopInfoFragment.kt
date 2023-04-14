package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopsViewPagerAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.ViewStatistics
import com.example.autopia.activities.utils.Constants
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_workshop_info.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

class WorkshopInfoFragment : Fragment() {

    private val titles = arrayOf("Info", "Services", "Products", "Promotions", "Feedbacks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
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

//        val config = AlanConfig.builder()
//            .setProjectId("ee2876e4440c11538f812e09d942b1392e956eca572e1d8b807a3e2338fdd0dc/stage")
//            .build()
//        val alanButton: AlanButton? = requireActivity().findViewById(R.id.alan_button)
//        alanButton?.initWithConfig(config)
//
//        val alanCallback: AlanCallback = object : AlanCallback() {
//            /// Handle commands from Alan Studio
//            override fun onCommand(eventCommand: EventCommand) {
//                try {
//                    val command = eventCommand.data
//                    val commandName = command.getJSONObject("data").getString("command")
//                    Log.d("AlanButton", "onCommand: commandName: $commandName")
//                } catch (e: JSONException) {
//                    e.message?.let { Log.e("AlanButton", it) }
//                }
//            }
//        }
//
//        alanButton?.registerCallback(alanCallback)

        return inflater.inflate(R.layout.fragment_workshop_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val workshopId = arguments?.getString("workshop_id")

        val preferences: SharedPreferences =
            requireContext().getSharedPreferences("workshop_id", Context.MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putString("workshop_id", workshopId)
        editor.apply()

        FirebaseFirestore.getInstance().collection(Constants.Feedbacks)
            .whereEqualTo("workshopId", workshopId).get().addOnCompleteListener { task ->
                val ratingBar: RatingBar? =
                    requireActivity().findViewById(R.id.workshop_info_rating)
                if (task.isSuccessful && !task.result.isEmpty) {
                    for (doc in task.result.documents) {
                        ratingBar?.rating = doc.data?.get("rating").toString().toFloat()
                    }
                } else {
                    ratingBar?.visibility = View.INVISIBLE
                    val noRatings: TextView? =
                        requireActivity().findViewById(R.id.workshop_no_rating)
                    noRatings?.visibility = View.VISIBLE
                }
            }

        FirestoreClass().fetchWorkshopInfo(workshopId!!).addOnCompleteListener { it ->
            val profileImage: ImageView? = requireActivity().findViewById(R.id.w_profile_image)
            if (profileImage != null) {
                Glide.with(requireContext()).load(it.result.data?.get("imageLink")).into(profileImage)
            }
            if (w_profile_cover != null) {
                Glide.with(requireContext()).load(it.result.data?.get("cover"))
                    .into(w_profile_cover)
            }
            val name: TextView? = requireActivity().findViewById(R.id.w_profile_name)
            name?.text = it.result.data?.get("workshopName").toString()

            val user = FirebaseAuth.getInstance().currentUser

            val bookAppointmentButton: MaterialButton? =
                requireActivity().findViewById(R.id.book_appointment_button)
            bookAppointmentButton?.setOnClickListener { _ ->
                if (user != null) {
                    val bundle = bundleOf("workshop_id" to it.result.data?.get("id"))
                    Navigation.findNavController(requireView())
                        .navigate(R.id.bookingServiceFragment, bundle)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please login to book an appointment.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val chatButton: MaterialButton? = requireActivity().findViewById(R.id.chat_button)
            chatButton?.setOnClickListener { _ ->
                if (user != null) {
                    val bundle = bundleOf(
                        "receiver_id" to it.result.data?.get("id"),
                        "receiver_name" to it.result.data?.get("workshopName")
                    )
                    Navigation.findNavController(requireView())
                        .navigate(R.id.chatRoomFragment, bundle)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please login to chat with workshop.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
//        val alanButton: AlanButton? = requireActivity().findViewById(R.id.alan_button)
//        alanButton?.disableButton()
//        alanButton?.hideButton()
    }

    override fun onStop() {
        super.onStop()
//        val appBarLayout: AppBarLayout? = requireActivity().findViewById(R.id.app_bar_layout)
//        appBarLayout?.elevation = 8f
//        val alanButton: AlanButton? = requireActivity().findViewById(R.id.alan_button)
//        alanButton?.disableButton()
//        alanButton?.hideButton()
    }

    override fun onResume() {
        super.onResume()
//        val alanButton: AlanButton? = requireActivity().findViewById(R.id.alan_button)
//        alanButton?.showButton()
    }

    override fun onStart() {
        super.onStart()

//        val alanButton: AlanButton? = requireActivity().findViewById(R.id.alan_button)
//        alanButton?.showButton()

//        val appBarLayout: AppBarLayout? = requireActivity().findViewById(R.id.app_bar_layout)
//        appBarLayout?.elevation = 0f

        val viewPager: ViewPager2? = requireActivity().findViewById(R.id.SP_view_pager)
        val tabLayout: TabLayout? = requireActivity().findViewById(R.id.workshop_tab_layout)
        val adapter: FragmentStateAdapter = WorkshopsViewPagerAdapter(this)

        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 5
        if (tabLayout != null && viewPager != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }

        //view pager doesn't dynamically change its height, so need to calculate it manually
        val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (childFragmentManager.fragments.size > position) {
                    val fragment = childFragmentManager.fragments[position]
                    fragment.view?.let {
                        //updatePagerHeightForChild(it, viewPager)
                    }
                }
            }
        }
        viewPager?.registerOnPageChangeCallback(pageChangeCallback)

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance().collection(Constants.Users)

        val workshopId = arguments?.getString("workshop_id")
        val favButton: MaterialButton? = requireActivity().findViewById(R.id.fav_button)

        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.FavoriteWorkshops)
                .whereEqualTo("clientId", user.uid).whereEqualTo("id", workshopId).get()
                .addOnCompleteListener { query ->
                    if (query.isSuccessful) {
                        if (!query.result.isEmpty) {
                            favButton?.isChecked = true
                        }
                    }
                }
            favButton?.addOnCheckedChangeListener { button, isChecked ->
                if (isChecked && button.isPressed) {
                    if (workshopId != null) {
                        db.document(workshopId).get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                val favWorkshop = hashMapOf(
                                    "id" to workshopId,
                                    "clientId" to user.uid,
                                    "workshopName" to it.result.data?.get("workshopName"),
                                    "imageLink" to it.result.data?.get("imageLink"),
                                    "userType" to "workshop",
                                    "address" to it.result.data?.get("address"),
                                    "description" to it.result.data?.get("description"),
                                    "lowerName" to it.result.data?.get("lowerName"),
                                    "latitude" to it.result.data?.get("latitude"),
                                    "longitude" to it.result.data?.get("longitude"),
                                    "contactNumber" to it.result.data?.get("contactNumber"),
                                    "location" to it.result.data?.get("location")
                                )
                                FirebaseFirestore.getInstance()
                                    .collection(Constants.FavoriteWorkshops)
                                    .whereEqualTo("clientId", user.uid)
                                    .whereEqualTo("workshopId", workshopId).get()
                                    .addOnCompleteListener { snapshot ->
                                        if (snapshot.result.isEmpty) {
                                            FirebaseFirestore.getInstance()
                                                .collection(Constants.FavoriteWorkshops)
                                                .add(favWorkshop)
                                                .addOnSuccessListener {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Workshop had been added to favorite list.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }.addOnFailureListener {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Failure adding workshop to favorite list.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } else {
                                            Log.d("Blah", "Blah")
                                        }
                                    }
                            }
                        }
                    }
                } else if (!isChecked && button.isPressed) {
                    FirebaseFirestore.getInstance().collection(Constants.FavoriteWorkshops)
                        .whereEqualTo("clientId", user.uid).whereEqualTo("id", workshopId)
                        .get().addOnSuccessListener { document ->
                            for (doc in document.documents) {
                                FirebaseFirestore.getInstance()
                                    .collection(Constants.FavoriteWorkshops)
                                    .document(doc.id).delete()
                                Toast.makeText(
                                    requireContext(),
                                    "Workshop had been removed from favorite list.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        } else {
            favButton?.setOnClickListener {
                favButton.isCheckable = false
                favButton.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "Please login to add workshop to favorite list.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.FavoriteWorkshops)
                .whereEqualTo("clientId", user.uid).whereEqualTo("workshopId", workshopId).get()
                .addOnCompleteListener { query ->
                    if (query.isSuccessful) {
                        if (!query.result.isEmpty) {
                            favButton?.isChecked = true
                        }
                    }
                }
        }

        if (workshopId != null) {
            val midnight: LocalTime = LocalTime.MIDNIGHT
            val today: LocalDate = LocalDate.now(ZoneId.of("Asia/Kuala_Lumpur"))
            val todayMidnight = LocalDateTime.of(today, midnight)
            val millis = todayMidnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            FirebaseFirestore.getInstance().collection(Constants.ViewStatistics)
                .whereEqualTo("workshopId", workshopId).whereEqualTo("millis", millis).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && !task.result.isEmpty) {
                        for (doc in task.result.documents) {
                            val statistics = ViewStatistics(
                                workshopId,
                                millis,
                                doc.getTimestamp("date")?.toDate(),
                                doc.data?.get("viewCount").toString().toInt() + 1,
                            )
                            FirebaseFirestore.getInstance().collection(Constants.ViewStatistics)
                                .document(doc.id).set(statistics)
                        }
                    } else {
                        val statistics = hashMapOf(
                            "workshopId" to workshopId,
                            "millis" to millis,
                            "date" to cal.time,
                            "viewCount" to 1
                        )
                        FirebaseFirestore.getInstance().collection(Constants.ViewStatistics)
                            .add(statistics)
                    }
                }
        }
    }

//    fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
//        view.post {
//            val wMeasureSpec =
//                View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
//            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
//            view.measure(wMeasureSpec, hMeasureSpec)
//
//            if (pager.layoutParams.height != view.measuredHeight) {
//                pager.layoutParams = (pager.layoutParams)
//                    .also { lp ->
//                        lp.height = view.measuredHeight
//                    }
//            }
//        }
//    }
}