package com.example.autopia.activities.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.autopia.R
import kotlinx.android.synthetic.main.fragment_news_details.*
import kotlinx.android.synthetic.main.news_card_view.*
import kotlinx.android.synthetic.main.news_card_view.view.*

class NewsDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val image = arguments?.getString("image")
        val title = arguments?.getString("title")
        val desc = arguments?.getString("desc")
        val date = arguments?.getString("date")

        if (news_image != null) {
            Glide.with(requireContext()).load(image)
                .into(news_image)
        }
        news_title?.text = title
        news_desc?.text = desc
        news_date?.text = date
    }
}