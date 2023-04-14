package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.autopia.R
import kotlinx.android.synthetic.main.fragment_services_details.*

class ServicesDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_services_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title")
        val desc = arguments?.getString("description")
        val image = arguments?.getString("image")

        services_title?.text = title
        services_desc?.text = desc
        if (services_image != null) {
            Glide.with(requireView()).load(image).into(services_image)
        }
    }
}