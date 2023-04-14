package com.example.autopia.activities.api

import com.example.autopia.activities.constants.Constants.Companion.BASE_URL
import com.example.autopia.activities.model.*
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.LoggingEventListener
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ApiInterface {
    @Headers("Content-Type:application/json")
    @GET("api/Appointments")
    fun getAppointments(): Call<List<Appointments>>

    @Headers("Content-Type:application/json")
    @GET("api/Appointments/{id}")
    fun getAppointmentById(
        @Path("id") id: Int,
    ): Call<Appointments>

    @Headers("Content-Type:application/json")
    @POST("api/Services")
    fun postServices(
        @Body services: Services,
    ): Call<Services>

    @Headers("Content-Type:application/json")
    @POST("api/Vehicles")
    fun postVehicles(
        @Body vehicles: Vehicles,
    ): Call<Vehicles>

    @Headers("Content-Type:application/json")
    @POST("api/Appointments")
    fun postAppointments(
        @Body appointments: Appointments,
    ): Call<Appointments>

    @Headers("Content-Type:application/json")
    @POST("api/Notifications")
    fun postNotifications(
        @Body notifications: Notifications,
    ): Call<Notifications>

    @Headers("Content-Type:application/json")
    @POST("api/Feedbacks")
    fun postFeedbacks(
        @Body feedbacks: Feedbacks,
    ): Call<Feedbacks>

    @Headers("Content-Type:application/json")
    @PUT("api/Vehicles/{id}")
    fun putVehicles(
        @Body vehicles: Vehicles,
        @Path("id") id: Int,
    ): Call<Vehicles>

    @Headers("Content-Type:application/json")
    @PUT("api/Services/{id}")
    fun putServices(
        @Body services: Services,
        @Path("id") id: Int,
    ): Call<Services>

    @Headers("Content-Type:application/json")
    @PUT("api/Appointments/{id}")
    fun putAppointments(
        @Body appointments: Appointments,
        @Path("id") id: Int,
    ): Call<Appointments>

    @Headers("Content-Type:application/json")
    @PUT("api/ServiceReminders/{id}")
    fun putServiceReminders(
        @Body serviceReminders: ServiceReminders,
        @Path("id") id: Int,
    ): Call<ServiceReminders>

    @Headers("Content-Type:application/json")
    @DELETE("api/Vehicles/{id}")
    fun deleteVehicles(
        @Path("id") id: Int,
    ): Call<Vehicles>

    @Headers("Content-Type:application/json")
    @DELETE("api/Services/{id}")
    fun deleteServices(
        @Path("id") id: Int,
    ): Call<Services>

    @Headers("Content-Type:application/json")
    @POST("api/ServiceReminders")
    fun postServiceReminders(
        @Body serviceReminders: ServiceReminders,
    ): Call<ServiceReminders>

    @Headers("Content-Type:application/json")
    @DELETE("api/ServiceReminders/{id}")
    fun deleteServiceReminders(
        @Path("id") id: Int,
    ): Call<ServiceReminders>

    @Headers("Content-Type:application/json")
    @DELETE("api/Products/{id}")
    fun deleteProducts(
        @Path("id") id: Int,
    ): Call<Products>

    @Headers("Content-Type:application/json")
    @POST("api/Products")
    fun postProducts(
        @Body products: Products,
    ): Call<Products>

    @Headers("Content-Type:application/json")
    @PUT("api/Products/{id}")
    fun putProducts(
        @Body products: Products,
        @Path("id") id: Int,
    ): Call<Products>

    @Headers("Content-Type:application/json")
    @POST("api/Promotions")
    fun postPromotions(
        @Body promotions: Promotions,
    ): Call<Promotions>

    @Headers("Content-Type:application/json")
    @POST("api/Customers")
    fun postCustomers(
        @Body customers: Customers,
    ): Call<Customers>

    @Headers("Content-Type:application/json")
    @GET("api/Feedbacks/appointment/{id}")
    fun getFeedbackByAppointmentId(
        @Path("id") id: Int,
    ): Call<List<Feedbacks>>

    companion object {
        fun create(): ApiInterface {
            val listener = LoggingEventListener.Factory()

            val okHttpClient =
                OkHttpClient.Builder()
//                    .pingInterval(1, TimeUnit.SECONDS)
//                    .callTimeout(60, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .eventListenerFactory(listener)
//                    .connectionPool(ConnectionPool(10, 2, TimeUnit.HOURS))
                    .build()

            val retrofit =
                Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build()

            return retrofit.create(ApiInterface::class.java)
        }
    }
}