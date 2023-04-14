package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.NotificationsAdapter
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.model.Notifications
import com.example.autopia.activities.utils.ProgressDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class NotificationsFragment : Fragment() {

    private var notificationList: List<Notifications> = ArrayList()
    private lateinit var notificationListAdapter: NotificationsAdapter
    private lateinit var viewModel: ApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val user = FirebaseAuth.getInstance().currentUser
        val root: View
        if (user != null) {
            root = inflater.inflate(R.layout.fragment_notifications, container, false)
        } else {
            root = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = root.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController: NavController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
        }

        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = false

        return root
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = false
    }

    override fun onPause() {
        super.onPause()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = true

        val workshopBottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.workshop_bottom_navigation_view)
        workshopBottomNavigationView?.isVisible = true
    }

    override fun onStart() {
        super.onStart()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null && view != null) {
            val repository = Repository()
            val viewModelFactory = ApiViewModelFactory(repository)
            val progressDialog = ProgressDialog(requireActivity())
            progressDialog.startLoading()
            viewModel = ViewModelProvider(this, viewModelFactory)[ApiViewModel::class.java]
            viewModel.getNotificationsByUserId(uid)
            viewModel.notifications.observe(viewLifecycleOwner) { response ->
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    notificationList = response.body()!!
                    notificationListAdapter =
                        NotificationsAdapter(requireActivity(), notificationList.reversed())
                    val recyclerView: RecyclerView? =
                        requireView().findViewById(R.id.notification_rv)
                    val llm = LinearLayoutManager(requireContext())
                    recyclerView?.layoutManager = llm
                    recyclerView?.adapter = notificationListAdapter
                    recyclerView?.setHasFixedSize(true)
                    notificationListAdapter.notificationList = notificationList.reversed()
                    progressDialog.dismissLoading()
                } else {
                    val noNotificationLayout: ConstraintLayout? =
                        requireActivity().findViewById(R.id.noNotificationLayout)
                    noNotificationLayout?.visibility = View.VISIBLE
                    progressDialog.dismissLoading()
                }
            }
        }
    }
}