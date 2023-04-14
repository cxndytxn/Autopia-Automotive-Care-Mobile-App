package com.example.autopia.activities.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.autopia.R
import com.example.autopia.activities.adapter.WorkshopsAdapter
import com.example.autopia.activities.adapter.WorkshopsListAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Workshops
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*

class WorkshopsListFragment : Fragment() {

    private var workshopList: List<Workshops> = ArrayList()
    private lateinit var workshopListAdapter: WorkshopsListAdapter

    private val titles = arrayOf("All", "Nearby")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        //setHasOptionsMenu(true)
        val root = inflater.inflate(R.layout.fragment_service_provider_list, container, false)
        workshopListAdapter = WorkshopsListAdapter(requireContext(), workshopList)

        return root
    }

    private fun loadDataOnQuery(queryText: String) {
        FirestoreClass().fetchWorkshopsOnQuery(queryText).addOnCompleteListener {
            if (it.isSuccessful) {
                workshopList = it.result!!.toObjects(Workshops::class.java)
                workshopListAdapter.workshopsListItems = workshopList
                workshopListAdapter.notifyDataSetChanged()
            } else {
                Log.d("", "Error: ${it.exception!!.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val sharedPreferences: SharedPreferences =
            requireContext().getSharedPreferences(
                "drawerNavClicked",
                Context.MODE_PRIVATE
            )
        val isDrawerNavClicked: Boolean = sharedPreferences.getBoolean("drawerNavClicked", false)
        if (isDrawerNavClicked) {
            val bottomNavigationView: BottomNavigationView? =
                requireActivity().findViewById(R.id.bottom_navigation_view)
            bottomNavigationView?.isVisible = false
            sharedPreferences.edit().remove("drawerNavClicked").apply()
        }

        val recyclerView: RecyclerView? = requireActivity().findViewById(R.id.all_sp_rv)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = workshopListAdapter
        val searchBar: SearchView? = requireActivity().findViewById(R.id.SP_search_view)

        val viewPager: ViewPager2? = requireActivity().findViewById(R.id.sp_view_pager)
        val adapter: FragmentStateAdapter = WorkshopsAdapter(this@WorkshopsListFragment)
        val tabLayout: TabLayout? = requireActivity().findViewById(R.id.sp_tab_layout)

        searchBar?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query != "") {
                    viewPager?.isVisible = false
                    loadDataOnQuery(query.toLowerCase(Locale.ROOT))
                } else {
                    loadNoData()
                    viewPager?.isVisible = true
                }
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (query != "") {
                    viewPager?.isVisible = false
                    loadDataOnQuery(query.toLowerCase(Locale.ROOT))
                } else {
                    loadNoData()
                    viewPager?.isVisible = true
                }
                return false
            }
        })

        searchBar?.isIconifiedByDefault = false

        viewPager?.adapter = adapter

        if (tabLayout != null && viewPager != null) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = titles[position]
            }.attach()
        }
    }

    override fun onResume() {
        if (isAdded) {
            val appBarLayout: AppBarLayout? = requireActivity().findViewById(R.id.app_bar_layout)
            appBarLayout?.elevation = 0f
        }
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBar: SearchView? = requireActivity().findViewById(R.id.SP_search_view)
        val viewPager: ViewPager2? = requireActivity().findViewById(R.id.sp_view_pager)

        if (searchBar?.query.toString() != "") {
            viewPager?.isVisible = false
            loadDataOnQuery(searchBar?.query.toString().toLowerCase(Locale.ROOT))
        } else {
            loadNoData()
            viewPager?.isVisible = true
            val adapter: FragmentStateAdapter = WorkshopsAdapter(this@WorkshopsListFragment)
            viewPager?.adapter = adapter
        }

        searchBar?.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (query != "") {
                    viewPager?.isVisible = false
                    loadDataOnQuery(query.toLowerCase(Locale.ROOT))
                } else {
                    loadNoData()
                    viewPager?.isVisible = true
                    val adapter: FragmentStateAdapter = WorkshopsAdapter(this@WorkshopsListFragment)
                    viewPager?.adapter = adapter
                }
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (query != "") {
                    viewPager?.isVisible = false
                    loadDataOnQuery(query.toLowerCase(Locale.ROOT))
                } else {
                    loadNoData()
                    viewPager?.isVisible = true
                    val adapter: FragmentStateAdapter = WorkshopsAdapter(this@WorkshopsListFragment)
                    viewPager?.adapter = adapter
                }
                return false
            }
        })
    }

    private fun loadNoData() {
        workshopList = emptyList()
        workshopListAdapter.workshopsListItems = workshopList
        workshopListAdapter.notifyDataSetChanged()
    }

    override fun onStop() {
        super.onStop()
        if (isAdded) {
            val appBarLayout: AppBarLayout? = requireActivity().findViewById(R.id.app_bar_layout)
            appBarLayout?.elevation = 8f
        }
    }
}