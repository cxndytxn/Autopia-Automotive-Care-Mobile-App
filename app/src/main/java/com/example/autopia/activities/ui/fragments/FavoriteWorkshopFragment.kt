package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.FavoriteWorkshopAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Workshops
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class FavoriteWorkshopFragment : Fragment() {

    private var workshopList: MutableList<Workshops> = mutableListOf()
    private lateinit var workshopListAdapter: FavoriteWorkshopAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        val user = FirebaseAuth.getInstance().currentUser

        val root: View
        if (user != null) {
            root = inflater.inflate(R.layout.fragment_favorite_workshop, container, false)
        } else {
            root = inflater.inflate(R.layout.empty_state_not_logged_in, container, false)
            val button: Button? = root.findViewById(R.id.emptyStateLoginButton)
            button?.setOnClickListener {
                val navController =
                    requireActivity().findNavController(R.id.nav_host_fragment)
                navController.navigate(R.id.loginActivity)
            }
        }

        return root
    }

    override fun onStart() {
        super.onStart()

        loadData()
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = false

        loadData()
    }

    override fun onStop() {
        super.onStop()
        val bottomNavigationView: BottomNavigationView? =
            requireActivity().findViewById(R.id.bottom_navigation_view)
        bottomNavigationView?.isVisible = true
    }

    private fun loadData() {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val recyclerView: RecyclerView? =
                requireActivity().findViewById(R.id.favorite_workshop_rv)

            workshopListAdapter = FavoriteWorkshopAdapter(requireContext(), workshopList)

            recyclerView?.layoutManager = LinearLayoutManager(requireContext())
            recyclerView?.adapter = workshopListAdapter
            recyclerView?.setHasFixedSize(true)

            FirestoreClass().fetchFavoriteWorkshops(user.uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    workshopList = it.result.toObjects(Workshops::class.java)
                    workshopListAdapter.workshopsListItems = workshopList
                    workshopListAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}