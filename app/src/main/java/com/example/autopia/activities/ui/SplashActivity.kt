package com.example.autopia.activities.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.autopia.R
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val user = FirebaseAuth.getInstance().currentUser

        Handler().postDelayed(
            {
                if (user != null) {
                    FirebaseFirestore.getInstance().collection(Constants.Users)
                        .document(user.uid)
                        .get().addOnSuccessListener {
                            if (it.data?.get("userType").toString() == "workshop") {
                                FirestoreClass().fetchWorkshopInfo(user.uid)
                                    .addOnCompleteListener { snapshot ->
                                        if (snapshot.result.get("address")
                                                .toString() == "" || snapshot.result.get("description")
                                                .toString() == "" || snapshot.result.get("contactNumber")
                                                .toString() == ""
                                        ) {
                                            startActivity(
                                                Intent(
                                                    this@SplashActivity,
                                                    WorkshopInfoActivity::class.java
                                                )
                                            )
                                            finish()
                                        } else {
                                            startActivity(
                                                Intent(
                                                    this@SplashActivity,
                                                    WorkshopNavigationDrawerActivity::class.java
                                                )
                                            )
                                            finish()
                                        }
                                    }
                            } else {
                                startActivity(
                                    Intent(
                                        this@SplashActivity,
                                        NavigationDrawerActivity::class.java
                                    )
                                )
                                finish()
                            }
                        }
                } else {
                    startActivity(Intent(this@SplashActivity, NavigationDrawerActivity::class.java))
                    finish()
                }
            }, 500
        )
    }
}