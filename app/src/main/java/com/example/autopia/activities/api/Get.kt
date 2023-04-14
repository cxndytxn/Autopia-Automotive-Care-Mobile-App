package com.example.autopia.activities.api

import com.example.autopia.activities.model.*
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface Get {
    @GET("api/Vehicles/client/{clientId}")
    suspend fun getVehiclesByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Vehicles>>
    //<Single<Response<List<Vehicle>>> for JavaRx for observable and observer structure which prevents main thread blocking
    //Refer to ApiInterface's retrofit builder

    @GET("api/Vehicles/{Id}")
    suspend fun getVehicleById(
        @Path("Id") id: Int,
    ): Response<Vehicles>

    @GET("api/Services/workshop/{workshopId}")
    suspend fun getServicesByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Services>>

    @GET("api/Services/{Id}")
    suspend fun getServiceById(
        @Path("Id") id: Int,
    ): Response<Services>

    @GET("api/Appointments")
    suspend fun getAppointments(): Response<List<Appointments>>

    @GET("api/Appointments/{Id}")
    suspend fun getAppointmentById(
        @Path("Id") id: Int,
    ): Response<Appointments>

    @GET("api/Appointments/workshop/scheduled/{workshopId}")
    suspend fun getScheduledAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/reschedule/{workshopId}")
    suspend fun getRescheduleAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/reschedule/{clientId}")
    suspend fun getRescheduleAppointmentsByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/noshow/{workshopId}")
    suspend fun getNoShowAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/noshow/{clientId}")
    suspend fun getNoShowAppointmentsByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/pending/{workshopId}")
    suspend fun getPendingAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/accepted/{workshopId}")
    suspend fun getAcceptedAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/rejected/{workshopId}")
    suspend fun getRejectedAppointmentsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/workshop/histories/{workshopId}")
    suspend fun getHistoriesByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/pending/{clientId}")
    suspend fun getPendingAppointmentsByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/accepted/{clientId}")
    suspend fun getAcceptedAppointmentsByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/rejected/{clientId}")
    suspend fun getRejectedAppointmentsByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/Appointments/client/histories/{clientId}")
    suspend fun getHistoriesByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<Appointments>>

    @GET("api/CalendarEntities/workshop/{workshopId}")
    suspend fun getCalendarEntitiesByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<CalendarEntities>>

    @GET("api/Notifications/user/{userId}")
    suspend fun getNotificationsByUserId(
        @Path("userId") user_id: String,
    ): Response<List<Notifications>>

    @GET("api/Notifications/{Id}")
    suspend fun getNotificationById(
        @Path("Id") id: String,
    ): Response<Notifications>

    @GET("api/ServiceReminders/client/{clientId}")
    suspend fun getServiceRemindersByClientId(
        @Path("clientId") client_id: String,
    ): Response<List<ServiceReminders>>

    @GET("api/Products/workshop/{workshopId}")
    suspend fun getProductsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Products>>

    @GET("api/Products/{Id}")
    suspend fun getProductById(
        @Path("Id") id: Int,
    ): Response<Products>

    @GET("api/Feedbacks/workshop/{workshopId}")
    suspend fun getFeedbacksByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Feedbacks>>

    @GET("api/Feedbacks/{Id}")
    suspend fun getFeedback(
        @Path("Id") id: Int,
    ): Response<Feedbacks>

    @GET("api/Promotions/{Id}")
    suspend fun getPromotionById(
        @Path("Id") id: Int,
    ): Response<Promotions>

    @GET("api/Promotions/workshop/{workshopId}")
    suspend fun getPromotionsByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Promotions>>

    @GET("api/Customers/workshop/{workshopId}")
    suspend fun getCustomersByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Customers>>

    @GET("api/Customers/workshop/active/{workshopId}")
    suspend fun getActiveCustomersByWorkshopId(
        @Path("workshopId") workshop_id: String,
    ): Response<List<Customers>>

    @GET("api/Customers/duplicated/{workshopId}/{clientId}")
    suspend fun getDuplicatedCustomer(
        @Path("workshopId") workshop_id: String,
        @Path("clientId") client_id: String,
    ): Response<List<Customers>>
}