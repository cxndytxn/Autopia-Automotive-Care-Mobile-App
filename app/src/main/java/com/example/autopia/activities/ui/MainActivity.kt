package com.example.autopia.activities.ui

import android.graphics.Color
import android.os.Bundle
import com.example.autopia.R
import com.example.autopia.activities.adapter.ImageSliderAdapter
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView

class MainActivity : NavigationDrawerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val actionBar: ActionBar? = supportActionBar
        //actionBar?.title = Html.fromHtml("<font color='#000000'>Home</font>")

        val imageSlider: SliderView? = findViewById(R.id.imageSlider)
        val imageList: ArrayList<String> = ArrayList()
        imageList.add("https://s3-ap-southeast-1.amazonaws.com/wemotorcom/blog/wp-content/uploads/2016/01/Proton-CNY-PROMOTION-2016.jpg")
        imageList.add("https://www.piston.my/wp-content/uploads/2018/05/SDAC-Ford-Raya-Promotion-ENG.jpg")
        imageList.add("https://media.istockphoto.com/photos/child-hands-formig-heart-shape-picture-id951945718?k=6&m=951945718&s=612x612&w=0&h=ih-N7RytxrTfhDyvyTQCA5q5xKoJToKSYgdsJ_mHrv0=")
        if (imageSlider != null) {
            setImageInSlider(imageList, imageSlider)
        }
    }

    private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
        val adapter = ImageSliderAdapter()
        adapter.renewItems(images)
        imageSlider.setIndicatorAnimation(IndicatorAnimationType.THIN_WORM)
        imageSlider.setSliderTransformAnimation(SliderAnimations.ZOOMOUTTRANSFORMATION)
        imageSlider.indicatorSelectedColor = Color.BLACK
        imageSlider.indicatorUnselectedColor = Color.WHITE
        imageSlider.scrollTimeInSec = 2
        imageSlider.indicatorRadius = 5
        imageSlider.setSliderAdapter(adapter)
        imageSlider.isAutoCycle = true
        imageSlider.startAutoCycle()
    }
}