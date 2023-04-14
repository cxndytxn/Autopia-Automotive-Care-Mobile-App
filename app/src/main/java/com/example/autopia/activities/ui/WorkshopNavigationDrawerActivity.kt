package com.example.autopia.activities.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.api.ApiViewModelFactory
import com.example.autopia.activities.api.Repository
import com.example.autopia.activities.constants.Constants
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.example.autopia.activities.model.Vehicles
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.ArrayList

class WorkshopNavigationDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_workshop_navigation_drawer)
        var user = FirebaseAuth.getInstance().currentUser
        val navView: NavigationView? = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.workshop_nav_host_fragment)
        val navBottomView: BottomNavigationView? = findViewById(R.id.workshop_bottom_navigation_view)

        val toolbar: Toolbar? = findViewById(R.id.workshop_toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout? = findViewById(R.id.workshop_drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        navBottomView?.itemIconTintList = null

        navView?.menu?.findItem(R.id.logInActivity)?.setOnMenuItemClickListener {
            if (user != null) {
                Toast.makeText(
                    this,
                    "Please logout before you log into another account.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent =
                    Intent(this@WorkshopNavigationDrawerActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            true
        }

        navView?.menu?.findItem(R.id.logoutActivity)?.setOnMenuItemClickListener {
            if (user != null) {
                FirebaseAuth.getInstance().signOut()
                user = null
                Toast.makeText(this, "You had been logged out.", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    this@WorkshopNavigationDrawerActivity,
                    NavigationDrawerActivity::class.java
                )
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "You're not logged in!", Toast.LENGTH_SHORT).show()
            }
            true
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.workshopHomeFragment,
                R.id.appointmentFragment,
                R.id.historyFragment,
                R.id.chatFragment,
                R.id.workshopProfileFragment
            ), drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView?.setupWithNavController(navController)
        navBottomView?.setupWithNavController(navController)

        supportFragmentManager.addOnBackStackChangedListener {
            val fragments: List<Fragment> = supportFragmentManager.fragments
            for (fragment: Fragment in fragments) {
                if (fragment.isVisible) {
                    if (fragment.tag == "serviceProviderFragment") {
                        toolbar?.title = "Service Providers"
                    } else if (fragment.tag == "appointmentFragment") {
                        toolbar?.title = "Appointment"
                    }
                }
            }

            if (supportFragmentManager.backStackEntryCount == 0) {
                toolbar?.title = "Home"
            }

        }
        toolbar?.setNavigationOnClickListener {
            navController.navigateUp(appBarConfiguration)
        }

        if (user != null) {
            checkUnreadMessages(user!!.uid)
        }
    }

    private fun checkUnreadMessages(uid: String) {
        dbRef =
            FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL).reference.child("conversations")
        Thread {
            FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
                if (it.isSuccessful) {
                    dbRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                for (snapshot in dataSnapshot.children) {
                                    for (snap in snapshot.children) {
                                        if (snap.exists()) {
                                            for (s in snap.children) {
                                                val readStatus =
                                                    s.child("readStatus").value as String?
                                                val bottomNavigationView: BottomNavigationView? =
                                                    findViewById(
                                                        R.id.workshop_bottom_navigation_view
                                                    )
                                                val badge: BadgeDrawable? =
                                                    bottomNavigationView?.getOrCreateBadge(R.id.chatFragment)
                                                badge?.isVisible = readStatus == "unread"
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController =
            findNavController(R.id.workshop_nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}