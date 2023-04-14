package com.example.autopia.activities.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.autopia.R
import com.example.autopia.activities.model.Workshops
import kotlinx.android.synthetic.main.horizontal_workshop_card.view.*

class WorkshopsCardAdapter(var context: Context, var workshopsListItems: List<Workshops>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class WorkshopsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var context: Context = itemView.context
        fun bind(workshopsModel: Workshops) {
            itemView.horizontal_workshop_name.text = workshopsModel.workshopName
            Glide.with(itemView.context).load(workshopsModel.imageLink)
                .into(itemView.horizontal_workshop_image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.horizontal_workshop_card, parent, false)
        return WorkshopsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return workshopsListItems.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as WorkshopsViewHolder).bind(workshopsListItems[position])

        val workshopListItem = workshopsListItems[position]

        holder.itemView.setOnClickListener {
            Log.d("debug", workshopListItem.id)
            val bundle = bundleOf("workshop_id" to workshopListItem.id)
            findNavController(holder.itemView).navigate(R.id.workshopInfoFragment, bundle)

//            val navController = context..findNavController(R.id.nav_host_fragment)
//            navController.navigate(R.id.addVehicleFragment)
//
//            val manager: FragmentManager = (context as NavigationDrawerActivity).supportFragmentManager
//            manager.beginTransaction().replace(R.id.nav_host_fragment, AddVehicleFragment(), "add_vehicle_fragment").commit()

        }

//        holder.itemView.vehicle_card_view.setOnLongClickListener {
//            Log.d("", "")
//        }

//        holder.itemView.cartListView.cartDeleteButton.setOnClickListener {
//            val preferences: SharedPreferences = holder.itemView.context.getSharedPreferences("deleteItem", Context.MODE_PRIVATE)
//            val editor = preferences.edit()
//
//            editor.putString("delete", cartListItem.documentid)
//            editor.apply()
//
//            val cartActivity =  context as MainActivity
//            DeleteDialog().show(cartActivity.supportFragmentManager, "deleteDialog")
//
//            android.os.Handler().postDelayed(
//                {
//                    val manager: FragmentManager = (context as MainActivity).supportFragmentManager
//                    manager.beginTransaction()
//                        .replace(R.id.fragment, CartFragment(), "cart_fragment").commit()
//                }, 1000
//            )
//        }
//
//        holder.itemView.cartListView.cartStepper.setOnValueChangeListener { view, oldValue, newValue ->
//            val price = holder.itemView.cartMealPrice.text.trimStart()
//            val trimPrice = price.drop(10)
//            val currentPrice: Float = trimPrice.toString().toFloat()
//            val singlePrice = currentPrice / oldValue
//            holder.itemView.cartMealQuantity.text = String.format("Qty:     $newValue")
//            val newPrice = singlePrice* newValue
//            holder.itemView.cartMealPrice.text = String.format("Price:  RM$newPrice")
//
//            val userID = FirebaseAuth.getInstance().currentUser.uid
//
//            val calendar = Calendar.getInstance()
//            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            format.timeZone = TimeZone.getTimeZone("GMT+08:00")
//            val time = format.format(calendar.time)
//
//            val cartItem = hashMapOf(
//                "mealName" to cartListItem.mealName,
//                "mealImage" to cartListItem.mealImage,
//                "mealPrice" to newPrice.toString(),
//                "mealQuantity" to newValue,
//                "userid" to userID,
//                "restaurantName" to cartListItem.restaurantName,
//                "paymentStatus" to false,
//                "documentid" to cartListItem.documentid,
//                "time" to time
//            )
    }
}

//class WorkshopsCardAdapter() :
//    SliderViewAdapter<WorkshopsCardAdapter.VH>() {
//    private var mSliderItems = ArrayList<String>()
//    fun renewItems(sliderItems: ArrayList<String>) {
//        mSliderItems = sliderItems
//        notifyDataSetChanged()
//    }
//
//    fun addItem(sliderItem: String) {
//        mSliderItems.add(sliderItem)
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup): VH {
//        val inflate: View = LayoutInflater.from(parent.context).inflate(R.layout.image_holder, null)
//        return VH(inflate)
//    }
//
//    override fun onBindViewHolder(viewHolder: VH, position: Int) {
//        //load image into view
//        Picasso.get().load(mSliderItems[position]).fit().into(viewHolder.imageView)
//    }
//
//    override fun getCount(): Int {
//        return mSliderItems.size
//    }
//
//    inner class VH(itemView: View) : ViewHolder(itemView) {
//        var imageView: ImageView = itemView.findViewById(R.id.imageSlider)
//
//    }
//}