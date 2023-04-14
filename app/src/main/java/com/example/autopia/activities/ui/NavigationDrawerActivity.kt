package com.example.autopia.activities.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.autopia.R
import com.example.autopia.activities.api.ApiViewModel
import com.example.autopia.activities.constants.Constants
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Appointments
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.onesignal.OneSignal
import com.onesignal.OneSignal.OSExternalUserIdUpdateCompletionHandler
import org.json.JSONObject
import java.util.ArrayList

//, FragmentManager.OnBackStackChangedListener
//, NavigationView.OnNavigationItemSelectedListener
open class NavigationDrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer)
        val user = FirebaseAuth.getInstance().currentUser
        val navView: NavigationView? = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        val navBottomView: BottomNavigationView? = findViewById(R.id.bottom_navigation_view)
        val toolbar: Toolbar? = findViewById(com.example.autopia.R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawerLayout: DrawerLayout? = findViewById(R.id.drawer_layout)
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
                val intent = Intent(this@NavigationDrawerActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            true
        }

        navView?.menu?.findItem(R.id.logoutActivity)?.setOnMenuItemClickListener {
            if (user != null) {
                FirebaseAuth.getInstance().signOut()
                oneSignalRemoveUser(user.uid)
                Toast.makeText(this, "You had been logged out.", Toast.LENGTH_SHORT).show()
                val intent =
                    Intent(this@NavigationDrawerActivity, NavigationDrawerActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "You're not logged in!", Toast.LENGTH_SHORT).show()
            }
            true
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.appointmentFragment,
                R.id.historyFragment,
                R.id.chatFragment,
                R.id.profileFragment
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

        NavController.OnDestinationChangedListener { _, destination, arguments ->
            val vehicleId = arguments?.getInt("vehicle_id")

            navController.currentDestination?.label =
                if (destination.id == R.id.addVehicleFragment && vehicleId != null) {
                    "Edit Vehicle"
                } else {
                    "Add Vehicle"
                }
        }

        navBottomView?.setOnItemSelectedListener { item ->
            NavigationUI.onNavDestinationSelected(item, navController)

            return@setOnItemSelectedListener true
        }

        if (user != null) {
            checkUnreadMessages(user.uid)
        }
    }

    private fun checkUnreadMessages(uid: String) {
        dbRef =
            FirebaseDatabase.getInstance(Constants.FIREBASE_DATABASE_URL).reference.child("conversations")

        FirestoreClass().fetchUserByID(uid).addOnCompleteListener {
            if (it.isSuccessful) {
                dbRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (snapshot in dataSnapshot.children) {
                                if (snapshot.key?.endsWith(uid) == true) {
                                    for (snap in snapshot.children) {
                                        for (s in snap.children) {
                                            val readStatus =
                                                s.child("readStatus").value as String?
                                            val bottomNavigationView: BottomNavigationView =
                                                findViewById(
                                                    R.id.bottom_navigation_view
                                                )
                                            val badge: BadgeDrawable =
                                                bottomNavigationView.getOrCreateBadge(R.id.chatFragment)
                                            badge.isVisible = readStatus == "unread"
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
    }

    private fun oneSignalRemoveUser(externalUserId: String) {
        OneSignal.removeExternalUserId(object : OSExternalUserIdUpdateCompletionHandler {
            override fun onSuccess(results: JSONObject?) {
                OneSignal.onesignalLog(
                    OneSignal.LOG_LEVEL.VERBOSE,
                    "Remove external user id done with results: $results"
                )

                // Push can be expected in almost every situation with a success status, but
                // as a pre-caution its good to verify it exists
                if (results != null) {
                    if (results.has("push") && results.getJSONObject("push").has("success")) {
                        val isPushSuccess = results.getJSONObject("push").getBoolean("success")
                        OneSignal.onesignalLog(
                            OneSignal.LOG_LEVEL.VERBOSE,
                            "Remove external user id for push status: $isPushSuccess"
                        )
                    }
                }

                // Verify the email is set or check that the results have an email success status
                if (results != null) {
                    if (results.has("email") && results.getJSONObject("email").has("success")) {
                        val isEmailSuccess =
                            results.getJSONObject("email").getBoolean("success")
                        OneSignal.onesignalLog(
                            OneSignal.LOG_LEVEL.VERBOSE,
                            "Remove external user id for email status: $isEmailSuccess"
                        )
                    }
                }
            }

            override fun onFailure(p0: OneSignal.ExternalIdError?) {
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}