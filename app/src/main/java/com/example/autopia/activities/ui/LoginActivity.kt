package com.example.autopia.activities.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.autopia.R
import com.example.autopia.activities.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.onesignal.OneSignal
import com.onesignal.OneSignal.ExternalIdError
import com.onesignal.OneSignal.OSExternalUserIdUpdateCompletionHandler
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.hide()

        val signUpButton: Button? = findViewById(R.id.dontHaveAccount)
        signUpButton?.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
        }

        val loginButton: Button? = findViewById(R.id.loginButton)
        loginButton?.setOnClickListener {
            val isValid: Boolean = validateInputFields()
            if (isValid) {
                loginFunction()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Please ensure no fields are empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val forgotPasswordButton: Button? = findViewById(R.id.cancelAppointmentButton)
        forgotPasswordButton?.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validateInputFields(): Boolean {

        val email: TextView? = findViewById(R.id.loginEmail)
        val password: TextView? = findViewById(R.id.loginPassword)
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }

        return !(emailText.isEmpty() || passwordText.isEmpty())
    }

    private fun loginFunction() {
        val email: TextView? = findViewById(R.id.loginEmail)
        val password: TextView? = findViewById(R.id.loginPassword)
        val emailText: String = email?.text.toString().trim { it <= ' ' }
        val passwordText: String = password?.text.toString().trim { it <= ' ' }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(emailText, passwordText)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_SHORT)
                        .show()
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        FirebaseFirestore.getInstance().collection(Constants.Users)
                            .document(user.uid).get().addOnSuccessListener {
                                if (it.data?.get("userType").toString() == "workshop") {
                                    if (it.data?.get("address")
                                            .toString() == "" || it.data?.get("description")
                                            .toString() == "" || it.data?.get("contactNumber")
                                            .toString() == ""
                                    ) {
                                        oneSignalSetUser(user.uid)
                                        val intent = Intent(
                                            this@LoginActivity,
                                            WorkshopInfoActivity::class.java
                                        )
                                        startActivity(intent)
                                    } else {
                                        oneSignalSetUser(user.uid)
                                        val intent = Intent(
                                            this@LoginActivity,
                                            WorkshopNavigationDrawerActivity::class.java
                                        )
                                        startActivity(intent)
                                    }
                                } else {
                                    oneSignalSetUser(user.uid)
                                    val intent =
                                        Intent(
                                            this@LoginActivity,
                                            NavigationDrawerActivity::class.java
                                        )
                                    startActivity(intent)
                                }
                            }
                    }

                } else {
                    Toast.makeText(
                        applicationContext,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun oneSignalSetUser(externalUserId: String) {
        OneSignal.setExternalUserId(
            externalUserId,
            object : OSExternalUserIdUpdateCompletionHandler {
                override fun onSuccess(results: JSONObject) {
                    try {
                        if (results.has("push") && results.getJSONObject("push").has("success")) {
                            val isPushSuccess = results.getJSONObject("push").getBoolean("success")
                            OneSignal.onesignalLog(
                                OneSignal.LOG_LEVEL.VERBOSE,
                                "Set external user id for push status: $isPushSuccess"
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    try {
                        if (results.has("email") && results.getJSONObject("email").has("success")) {
                            val isEmailSuccess =
                                results.getJSONObject("email").getBoolean("success")
                            OneSignal.onesignalLog(
                                OneSignal.LOG_LEVEL.VERBOSE,
                                "Set external user id for email status: $isEmailSuccess"
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    try {
                        if (results.has("sms") && results.getJSONObject("sms").has("success")) {
                            val isSmsSuccess = results.getJSONObject("sms").getBoolean("success")
                            OneSignal.onesignalLog(
                                OneSignal.LOG_LEVEL.VERBOSE,
                                "Set external user id for sms status: $isSmsSuccess"
                            )
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(error: ExternalIdError) {
                    // The results will contain channel failure statuses
                    // Use this to detect if external_user_id was not set and retry when a better network connection is made
                    OneSignal.onesignalLog(
                        OneSignal.LOG_LEVEL.VERBOSE,
                        "Set external user id done with error: $error"
                    )
                }
            })
    }
}