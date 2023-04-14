package com.example.autopia.activities.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.autopia.R
import com.example.autopia.activities.firestore.FirestoreClass
import com.example.autopia.activities.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        supportActionBar?.hide()

        val loginButton: Button? = findViewById(R.id.haveAccount)
        loginButton?.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        val serviceProviderButton: Button? = findViewById(R.id.serviceProviderButton)
        serviceProviderButton?.setOnClickListener {
            val intent = Intent(this@SignUpActivity, WorkshopSignUpActivity::class.java)
            startActivity(intent)
        }

        val signUpButton: Button? = findViewById(R.id.signUpButton)
        signUpButton?.setOnClickListener {
            val isValid: Boolean = validateInputFields()
            val isAligned: Boolean = validatePasswords()
            if (isValid && isAligned) {
                signUpFunction()
            } else if (!isValid && isAligned) {
                Toast.makeText(applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT).show()
            } else if (isValid && !isAligned) {
                Toast.makeText(applicationContext,
                    "Confirmed password and password are not matching.",
                    Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePasswords(): Boolean {
        val password: TextView? = findViewById(R.id.signUpPassword)
        val confirmedPassword: TextView? = findViewById(R.id.signUpConfirmedPassword)
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val confirmedPasswordText: String = confirmedPassword?.text.toString().trim { it <= ' ' }

        return (passwordText == confirmedPasswordText)
    }

    private fun validateInputFields(): Boolean {
        val username: TextView? = findViewById(R.id.signUpUsername)
        val email: TextView? = findViewById(R.id.signUpEmail)
        val password: TextView? = findViewById(R.id.signUpPassword)
        val confirmedPassword: TextView? = findViewById(R.id.signUpConfirmedPassword)
        val usernameText: String = username?.text.toString().trim { it <= ' ' }
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val confirmedPasswordText: String = confirmedPassword?.text.toString().trim { it <= ' ' }

        return !(emailText.isEmpty() || passwordText.isEmpty() || usernameText.isEmpty() || confirmedPasswordText.isEmpty())
    }

    private fun signUpFunction() {
        val email: TextView? = findViewById(R.id.signUpEmail)
        val password: TextView? = findViewById(R.id.signUpPassword)
        val username: TextView? = findViewById(R.id.signUpUsername)
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }
        val usernameText: String = username?.text.toString().trim { it <= ' ' }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val user = Users(
                        firebaseUser.uid,
                        emailText,
                        usernameText,
                        "user",
                        "true"
                    )

                    FirestoreClass().registerUser(this@SignUpActivity, user)
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
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
        startActivity(intent)
    }
}