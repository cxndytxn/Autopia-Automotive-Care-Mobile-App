package com.example.autopia.activities.api

import com.example.autopia.activities.constants.Constants.Companion.BASE_URL
import com.example.autopia.activities.model.Notifications
import io.reactivex.Single
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private val listener = LoggingEventListener.Factory()

    private val okHttpClient =
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .eventListenerFactory(listener)
            .build()

    private val retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()

//    suspend fun fetchNotifications(user_id: String): Single<Response<List<Notifications>>> {
//        return api.getNotificationsByUserId(user_id)
//    }

    val api: Get by lazy {
        retrofit.create(Get::class.java)
    }
}
