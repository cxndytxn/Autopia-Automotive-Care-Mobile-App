package com.example.autopia.activities.ui.fragments.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.ImageSliderAdapter
import com.example.autopia.activities.adapter.NewsAdapter
import com.example.autopia.activities.adapter.WorkshopsCardAdapter
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.News
import com.example.autopia.activities.model.Workshops
import com.example.autopia.activities.utils.Constants
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private var workshopList: List<Workshops> = ArrayList()
    private lateinit var workshopListAdapter: WorkshopsCardAdapter
    private var newsList: List<News> = ArrayList()
    private lateinit var newsListAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val imageSlider = root.findViewById<SliderView>(R.id.imageSlider)
        val imageList: ArrayList<String> = ArrayList()
        imageList.add("https://s3-ap-southeast-1.amazonaws.com/wemotorcom/blog/wp-content/uploads/2016/01/Proton-CNY-PROMOTION-2016.jpg")
        imageList.add("https://www.piston.my/wp-content/uploads/2018/05/SDAC-Ford-Raya-Promotion-ENG.jpg")
        imageList.add("https://mrexecutiveauto.com/wp-content/uploads/2018/06/2.jpg")
        setImageInSlider(imageList, imageSlider)

        val viewAllBtn: Button = root.findViewById(R.id.viewAllSPButton)
        viewAllBtn.setOnClickListener {
            val navController = requireActivity().findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.serviceProviderListFragment)
        }

        loadData()

        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadData()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection(Constants.Users).document(user.uid).get()
                .addOnSuccessListener { snapshot ->
                    "Good to see you, ${
                        snapshot.data?.get("username").toString()
                    }.".also { greetingsText.text = it }
                }
        } else {
            "Hello there!".also { greetingsText.text = it }
        }

        if (isAdded) {
            val appBarLayout: AppBarLayout? =
                requireActivity().findViewById(R.id.app_bar_layout)
            if (appBarLayout != null) {
                appBarLayout.elevation = 8f
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadData() {
        Thread {
            FirestoreClass().fetchAllWorkshopsInfo().addOnCompleteListener {
                if (it.isSuccessful && isAdded) {
                    workshopList = it.result.toObjects(Workshops::class.java)
                    workshopListAdapter = WorkshopsCardAdapter(requireActivity(), workshopList)
                    val recyclerView: RecyclerView? =
                        requireActivity().findViewById(R.id.service_provider_rv)
                    recyclerView?.layoutManager = LinearLayoutManager(
                        this.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    recyclerView?.adapter = workshopListAdapter
                    recyclerView?.setHasFixedSize(true)
                    workshopListAdapter.workshopsListItems = workshopList
                    workshopListAdapter.notifyDataSetChanged()
                } else {
                    Log.d("Debug", "Error: ${it.exception?.message}")
                }
            }
        }.start()

        FirebaseFirestore.getInstance().collection(Constants.News).get().addOnCompleteListener {
            if (it.isSuccessful) {
                newsList = it.result.toObjects(News::class.java)
                newsListAdapter = NewsAdapter(requireActivity(), newsList)
                val recyclerView: RecyclerView? = requireActivity().findViewById(R.id.news_rv)
                recyclerView?.layoutManager = LinearLayoutManager(requireContext())
                recyclerView?.adapter = newsListAdapter
                recyclerView?.setHasFixedSize(true)
                newsListAdapter.newsListItems = newsList
                newsListAdapter.notifyDataSetChanged()
            }
        }
    }
}

private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
    val adapter = ImageSliderAdapter()
    adapter.renewItems(images)
    imageSlider.setIndicatorAnimation(IndicatorAnimationType.THIN_WORM)
    imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
    imageSlider.indicatorSelectedColor = Color.BLACK
    imageSlider.indicatorUnselectedColor = Color.GRAY
    imageSlider.scrollTimeInSec = 2
    imageSlider.indicatorRadius = 5
    imageSlider.setSliderAdapter(adapter)
    imageSlider.isAutoCycle = true
    imageSlider.startAutoCycle()
}
