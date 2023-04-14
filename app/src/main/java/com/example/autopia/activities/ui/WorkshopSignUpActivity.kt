package com.example.autopia.activities.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.autopia.R
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Services
import com.example.autopia.activities.model.Workshops
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WorkshopSignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_provider_sign_up)
        supportActionBar?.hide()

        val ownerButton: Button? = findViewById(R.id.vehicleOwnerButton)
        ownerButton?.setOnClickListener {
            val intent = Intent(this@WorkshopSignUpActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        val loginButton: Button? = findViewById(R.id.SPHaveAccount)
        loginButton?.setOnClickListener {
            val intent = Intent(this@WorkshopSignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        val signUpButton: Button? = findViewById(R.id.signUpSPButton)
        signUpButton?.setOnClickListener {
            val isValid: Boolean = validateInputFields()
            val isAligned: Boolean = validatePasswords()
            if (isValid && isAligned) {
                signUpFunction()
            } else if (!isValid && isAligned) {
                Toast.makeText(
                    applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (isValid && !isAligned) {
                Toast.makeText(
                    applicationContext,
                    "Confirmed password and password are not matching.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validatePasswords(): Boolean {
        val password: TextView? = findViewById(R.id.signUpSPPassword)
        val confirmedPassword: TextView? = findViewById(R.id.signUpSPConfirmedPassword)
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val confirmedPasswordText: String = confirmedPassword?.text.toString().trim { it <= ' ' }

        return (passwordText == confirmedPasswordText)
    }

    private fun validateInputFields(): Boolean {
        val companyName: TextView? = findViewById(R.id.signUpCompanyName)
        val email: TextView? = findViewById(R.id.signUpSPEmail)
        val password: TextView? = findViewById(R.id.signUpSPPassword)
        val confirmedPassword: TextView? = findViewById(R.id.signUpSPConfirmedPassword)
        val companyNameText: String = companyName?.text.toString().trim { it <= ' ' }
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val confirmedPasswordText: String = confirmedPassword?.text.toString().trim { it <= ' ' }

        return !(emailText.isEmpty() || passwordText.isEmpty() || companyNameText.isEmpty() || confirmedPasswordText.isEmpty())
    }

    private fun signUpFunction() {
        val email: TextView? = findViewById(R.id.signUpSPEmail)
        val password: TextView? = findViewById(R.id.signUpSPPassword)
        val companyName: TextView? = findViewById(R.id.signUpCompanyName)
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val companyNameText: String = companyName?.text.toString().trim { it <= ' ' }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    Log.d("Success", "Success")
                    val workshop = Workshops(
                        firebaseUser.uid,
                        emailText,
                        companyNameText,
                        null,
                        "workshop",
                        "",
                        "",
                        companyNameText.toLowerCase(Locale.ROOT),
                        "",
                        "",
                        "",
                        null,
                        "",
                        "",
                        arrayListOf(),
                    )

                    val service = Services(
                        null,
                        task.result.user!!.uid,
                        "Inspection",
                        "General vehicle inspection",
                        0.0,
                        null
                    )
                    val apiInterface = ApiInterface.create().postServices(service)
                    apiInterface.enqueue(object : Callback<Services> {
                        override fun onResponse(
                            call: Call<Services>,
                            response: Response<Services>,
                        ) {
                        }

                        override fun onFailure(call: Call<Services>, t: Throwable) {
                        }
                    })

                    FirestoreClass().registerWorkshop(this@WorkshopSignUpActivity, workshop)
                    Toast.makeText(this, "Account registered successfully!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.d("Failure", "Failure")
                    Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

    fun registrationSuccess() {
        val intent = Intent(this@WorkshopSignUpActivity, LoginActivity::class.java)
        startActivity(intent)
    }
}