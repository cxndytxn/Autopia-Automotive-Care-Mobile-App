package com.example.autopia.activities.ui

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopia.R
import com.example.autopia.activities.adapter.ChatBotAdapter
import com.example.autopia.activities.model.Messages
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.protobuf.DescriptorProtos

class ChatBotActivity : AppCompatActivity() {
//    private var mAdapter: ChatBotAdapter? = null
//    private var messageArrayList: List<Messages> = listOf()
//    private var initialRequest = false
//    private var mContext: Context? = null
//    private var watsonAssistant: Assistant? = null
//    private var watsonAssistantSession: Response<SessionResponse>? = null
//
//    private fun createServices() {
//        watsonAssistant =
//            Assistant(
//                "2019-02-28",
//                IamAuthenticator(applicationContext.getString(R.string.assistant_api_key))
//            )
//        watsonAssistant!!.serviceUrl = applicationContext.getString(R.string.assistant_url)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                finish()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

//        val supportActionBar = findViewById<Toolbar>(R.id.bot_toolbar)
//        setSupportActionBar(supportActionBar)
//        getSupportActionBar()?.title = "AutoBot"
//        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
//
//        mContext = applicationContext
//        val inputMessage: EditText = findViewById(R.id.chat_bot_input)
//        val btnSend: MaterialButton = findViewById(R.id.bot_send_button)
//        val recyclerView: RecyclerView = findViewById(R.id.chat_bot_rv)
//        messageArrayList = ArrayList()
//        mAdapter = ChatBotAdapter(applicationContext, messageArrayList)
//        val layoutManager = LinearLayoutManager(this)
//        layoutManager.stackFromEnd = true
//        recyclerView.layoutManager = layoutManager
//        recyclerView.itemAnimator = DefaultItemAnimator()
//        recyclerView.adapter = mAdapter
//        inputMessage.setText("")
//        initialRequest = true
//        btnSend.setOnClickListener {
//            if (checkInternetConnection()) {
//                sendMessage()
//            }
//        }
//        createServices()
//        sendMessage()
    }

    // Sending a message to Watson Assistant Service
//    private fun sendMessage() {
//        val inputMessage: EditText = findViewById(R.id.chat_bot_input)
//        val inMessage = inputMessage.text.toString().trim { it <= ' ' }
//        if (!initialRequest) {
//            val message = Messages(
//                FirebaseAuth.getInstance().currentUser?.uid!!,
//                inMessage,
//                "",
//                "read",
//                "Text"
//            )
//            messageArrayList.toMutableList().add(message)
//        } else {
//            val message = Messages(
//                FirebaseAuth.getInstance().currentUser?.uid!!,
//                inMessage,
//                "",
//                "read",
//                "Text"
//            )
//            initialRequest = false
//        }
//        inputMessage.setText("")
//        mAdapter?.notifyDataSetChanged()
//        val thread = Thread {
//            try {
//                if (watsonAssistantSession == null) {
//                    val call: ServiceCall<SessionResponse> = watsonAssistant?.createSession(
//                        CreateSessionOptions.Builder()
//                            .assistantId(applicationContext.getString(R.string.assistant_id))
//                            .build()
//                    ) as ServiceCall<SessionResponse>
//                    watsonAssistantSession = call.execute()
//                }
//                val input: MessageInput = MessageInput.Builder()
//                    .text(inMessage)
//                    .build()
//                val options: DescriptorProtos.MessageOptions = DescriptorProtos.MessageOptions.Builder()
//                    .assistantId(applicationContext.getString(R.string.assistant_id))
//                    .input(input)
//                    .sessionId(watsonAssistantSession?.result?.sessionId)
//                    .build()
//                val response: Response<MessageResponse> =
//                    watsonAssistant?.message(options)?.execute() as Response<MessageResponse>
//                Log.i("bot bot bot", "run: " + response.result)
//                if (response.result.output != null && response.result.output.generic.isNotEmpty()
//                ) {
//                    val responses: List<RuntimeResponseGeneric> =
//                        response.result.output.generic
//                    for (r in responses) {
//                        var outMessage: Messages
//                        when (r.responseType()) {
//                            "text" -> {
//                                outMessage = Messages(
//                                    "Bot",
//                                    r.text(),
//                                    "",
//                                    "",
//                                    ""
//                                )
//                                messageArrayList.toMutableList().add(outMessage)
//                            }
//                            "option" -> {
//                                outMessage = Messages()
//                                val title: String = r.title()
//                                var optionsOutput = ""
//                                var i = 0
//                                while (i < r.options().size) {
//                                    val option: DialogNodeOutputOptionsElement =
//                                        r.options()[i]
//                                    optionsOutput =
//                                        """
//                                          $optionsOutput${option.label}
//
//                                          """.trimIndent()
//                                    i++
//                                }
//                                outMessage = Messages(
//                                    "Bot",
//                                    title,
//                                    optionsOutput,
//                                    "",
//                                    ""
//                                )
//                                messageArrayList.toMutableList().add(outMessage)
//                            }
//                            "image" -> {
////                                outMessage = Messages(r)
////                                messageArrayList.add(outMessage)
//                            }
//                            else -> Log.e("Error", "Unhandled message type")
//                        }
//                    }
//                    runOnUiThread {
//                        mAdapter?.notifyDataSetChanged()
//                        if (mAdapter?.itemCount!! > 1) {
//                            val recyclerView: RecyclerView = findViewById(R.id.chat_bot_rv)
//                            recyclerView.layoutManager?.smoothScrollToPosition(
//                                recyclerView,
//                                null,
//                                mAdapter!!.itemCount - 1
//                            )
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        thread.start()
//    }
//
//    private fun checkInternetConnection(): Boolean {
//        // get Connectivity Manager object to check connection
//        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val activeNetwork = cm.activeNetworkInfo
//        val isConnected = activeNetwork != null &&
//                activeNetwork.isConnectedOrConnecting
//
//        // Check for network connections
//        return if (isConnected) {
//            true
//        } else {
//            Toast.makeText(this, " No Internet Connection available ", Toast.LENGTH_LONG).show()
//            false
//        }
//    }
}