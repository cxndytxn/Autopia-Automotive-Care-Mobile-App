package com.example.autopia.activities.utils

import android.util.Log
import com.example.autopia.activities.api.ApiInterface
import com.example.autopia.activities.model.Notifications
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OneSignalNotificationService {
    private val ONESIGNAL_APP_ID = "5db400c8-98a6-4b9e-b816-20a435dba7e5"

    fun createChatNotification(
        externalUserId: String,
        username: String,
        message: String,
        receiverRoom: String,
        receiverType: String,
        receiverName: String
    ) {
        try {
            var jsonResponse: String
            val url = URL("https://onesignal.com/api/v1/notifications")
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.useCaches = false
            con.doOutput = true
            con.doInput = true
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            con.setRequestProperty(
                "Authorization",
                "Basic ZGYzZDFlY2MtZTlkMi00YjY1LWE5ODctMjA4NDQ5N2JhODE4"
            )
            con.requestMethod = "POST"
            val strJsonBody = ("{"
                    + "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
                    + "\"include_external_user_ids\": [\"" + externalUserId + "\"],"
                    + "\"channel_for_external_user_ids\": \"push\","
                    //+ "\"data\": {\"room_id\": \"" + receiverRoom + "\"},"
                    + "\"priority\": \"10\","
                    + "\"data\": {\"room_id\": \"" + receiverRoom + "\", \"receiver_type\": \"" + receiverType + "\", \"receiver_id\": \"" + externalUserId + "\", \"receiver_name\": \"" + receiverName + "\"},"
                    + "\"contents\": {\"en\": \"" + message + "\"},"
                    + "\"headings\": {\"en\": \"You've received a message from " + username + "\"}"
                    + "}")
            println("strJsonBody:\n$strJsonBody")
            val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
            con.setFixedLengthStreamingMode(sendBytes.size)
            val thread = Thread {
                try {
                    val outputStream: OutputStream = con.outputStream
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.responseCode
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.inputStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.errorStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
            val now = formatter.format(LocalDateTime.now())
            val notification = Notifications(
                null,
                externalUserId,
                "You've received a message from $username",
                message,
                now
            )
            postNotification(notification)
            thread.start()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun createAppointmentNotification(externalUserId: String, headings: String, message: String) {
        try {
            var jsonResponse: String
            val url = URL("https://onesignal.com/api/v1/notifications")
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.useCaches = false
            con.doOutput = true
            con.doInput = true
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            con.setRequestProperty(
                "Authorization",
                "Basic ZGYzZDFlY2MtZTlkMi00YjY1LWE5ODctMjA4NDQ5N2JhODE4"
            )
            con.requestMethod = "POST"
            val strJsonBody = ("{"
                    + "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
                    + "\"include_external_user_ids\": [\"" + externalUserId + "\"],"
                    + "\"priority\": \"10\","
                    + "\"channel_for_external_user_ids\": \"push\","
                    + "\"data\": {\"receiver_id\": \"" + externalUserId + "\"},"
                    + "\"contents\": {\"en\": \"" + message + "\"},"
                    + "\"headings\": {\"en\": \"" + headings + "\"}"
                    + "}")
            println("strJsonBody:\n$strJsonBody")
            val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
            con.setFixedLengthStreamingMode(sendBytes.size)
            val thread = Thread {
                try {
                    val outputStream: OutputStream = con.outputStream
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.responseCode
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.inputStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.errorStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
            val now = formatter.format(LocalDateTime.now())
            val notification = Notifications(null, externalUserId, headings, message, now)
            postNotification(notification)
            thread.start()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun createPromotionNotification(
        externalUserId: List<kotlin.String>,
        headings: kotlin.String,
        message: kotlin.String,
        imageLink: kotlin.String,
    ) {
        Log.d("whr's the image", imageLink.toString())
        try {
            var jsonResponse: String
            val url = URL("https://onesignal.com/api/v1/notifications")
            val con: HttpURLConnection = url.openConnection() as HttpURLConnection
            con.useCaches = false
            con.doOutput = true
            con.doInput = true
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            con.setRequestProperty(
                "Authorization",
                "Basic ZGYzZDFlY2MtZTlkMi00YjY1LWE5ODctMjA4NDQ5N2JhODE4"
            )
            con.requestMethod = "POST"
            val strJsonBody: String
            if (imageLink != null && imageLink != "") {
                strJsonBody = ("{"
                        + "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
                        + "\"include_external_user_ids\": [\"" + externalUserId.toString().replace("[", "").replace("]", "").replace(", ", "\", \"") + "\"],"
                        + "\"priority\": \"10\","
                        + "\"channel_for_external_user_ids\": \"push\","
                        //+ "\"data\": {\"receiver_id\": \"" + externalUserId.toString().replace("[", "").replace("]", "").replace(", ", "\", \"") + "\"},"
                        + "\"contents\": {\"en\": \"" + message + "\"},"
                        + "\"headings\": {\"en\": \"" + headings + "\"},"
                        + "\"big_picture\": \"" + imageLink + "\""
                        + "}")
            } else {
                strJsonBody = ("{"
                        + "\"app_id\": \"" + ONESIGNAL_APP_ID + "\","
                        + "\"include_external_user_ids\": [\"" + externalUserId.toString().replace("[", "").replace("]", "").replace(", ", "\", \"") + "\"],"
                        + "\"priority\": \"10\","
                        + "\"channel_for_external_user_ids\": \"push\","
//                        + "\"data\": {\"receiver_id\": \"" + externalUserId.toString().replace("[", "").replace("]", "").replace(", ", "\", \"") + "\"},"
                        + "\"contents\": {\"en\": \"" + message + "\"},"
                        + "\"headings\": {\"en\": \"" + headings + "\"}"
                        + "}")
            }
            println("strJsonBody:\n$strJsonBody")
            val sendBytes = strJsonBody.toByteArray(charset("UTF-8"))
            con.setFixedLengthStreamingMode(sendBytes.size)
            val thread = Thread {
                try {
                    val outputStream: OutputStream = con.outputStream
                    outputStream.write(sendBytes)
                    val httpResponse: Int = con.responseCode
                    println("httpResponse: $httpResponse")
                    if (httpResponse >= HttpURLConnection.HTTP_OK
                        && httpResponse < HttpURLConnection.HTTP_BAD_REQUEST
                    ) {
                        val scanner = Scanner(con.inputStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    } else {
                        val scanner = Scanner(con.errorStream, "UTF-8")
                        jsonResponse =
                            if (scanner.useDelimiter("\\A").hasNext()) scanner.next() else ""
                        scanner.close()
                    }
                    println("jsonResponse:\n$jsonResponse")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a")
            val now = formatter.format(LocalDateTime.now())
//            val notification = Notifications(null, externalUserId, headings, message, now)
//            postNotification(notification)
            thread.start()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private fun postNotification(notification: Notifications) {
        val apiInterface = ApiInterface.create().postNotifications(notification)
        apiInterface.enqueue(object : Callback<Notifications> {
            override fun onResponse(
                call: Call<Notifications>,
                response: Response<Notifications>,
            ) {
                Log.d("Notification", "posted to database")
            }

            override fun onFailure(call: Call<Notifications>, t: Throwable) {
                t.printStackTrace()
            }
        })
    }
}