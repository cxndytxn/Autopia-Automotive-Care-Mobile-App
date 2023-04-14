package com.example.autopia.activities.api

import com.example.autopia.activities.model.*
import io.reactivex.Single
import retrofit2.Response
import java.util.LinkedList

class Repository {
    suspend fun getServicesByWorkshopId(workshop_id: String): Response<List<Services>> {
        return RetrofitInstance.api.getServicesByWorkshopId(workshop_id)
    }

    suspend fun getVehiclesByClientId(client_id: String): Response<List<Vehicles>> {
        return RetrofitInstance.api.getVehiclesByClientId(client_id)
    }

    suspend fun getVehicleById(id: Int): Response<Vehicles> {
        return RetrofitInstance.api.getVehicleById(id)
    }

    suspend fun getServiceById(id: Int): Response<Services> {
        return RetrofitInstance.api.getServiceById(id)
    }

    suspend fun getAppointmentById(id: Int): Response<Appointments> {
        return RetrofitInstance.api.getAppointmentById(id)
    }

    suspend fun getScheduledAppointmentsByWorkshopId(workshop_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getScheduledAppointmentsByWorkshopId(workshop_id)
    }

    suspend fun getPendingAppointmentsByWorkshopId(workshop_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getPendingAppointmentsByWorkshopId(workshop_id)
    }

    suspend fun getAcceptedAppointmentsByWorkshopId(workshop_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getAcceptedAppointmentsByWorkshopId(workshop_id)
    }

    suspend fun getRejectedAppointmentsByWorkshopId(workshop_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getRejectedAppointmentsByWorkshopId(workshop_id)
    }

    suspend fun getHistoriesByWorkshopId(workshop_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getHistoriesByWorkshopId(workshop_id)
    }

    suspend fun getPendingAppointmentsByClientId(client_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getPendingAppointmentsByClientId(client_id)
    }

    suspend fun getAcceptedAppointmentsByClientId(client_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getAcceptedAppointmentsByClientId(client_id)
    }

    suspend fun getRejectedAppointmentsByClientId(client_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getRejectedAppointmentsByClientId(client_id)
    }

//    suspend fun getCalendarEntitiesByWorkshopId(workshop_id: String): Response<List<CalendarEntities>> {
//        return RetrofitInstance.api.getCalendarEntitiesByWorkshopId(workshop_id)
//    }

    suspend fun getHistoriesByClientId(client_id: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getHistoriesByClientId(client_id)
    }

    suspend fun getNoShowAppointmentsByWorkshopId(workshopId: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getNoShowAppointmentsByWorkshopId(workshopId)
    }

    suspend fun getNoShowAppointmentsByClientId(clientId: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getNoShowAppointmentsByClientId(clientId)
    }

    suspend fun getRescheduleAppointmentsByWorkshopId(workshopId: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getRescheduleAppointmentsByWorkshopId(workshopId)
    }

    suspend fun getRescheduleAppointmentsByClientId(clientId: String): Response<List<Appointments>> {
        return RetrofitInstance.api.getRescheduleAppointmentsByClientId(clientId)
    }

    suspend fun getNotificationsByUserId(userId: String): Response<List<Notifications>> {
        return RetrofitInstance.api.getNotificationsByUserId(userId)
    }

    suspend fun getServiceRemindersByClientId(clientId: String): Response<List<ServiceReminders>> {
        return RetrofitInstance.api.getServiceRemindersByClientId(clientId)
    }

//    suspend fun getNotificationById(Id: String): Response<Notifications> {
//        return RetrofitInstance.api.getNotificationById(Id)
//    }

    suspend fun getProductsByWorkshopId(workshop_id: String): Response<List<Products>> {
        return RetrofitInstance.api.getProductsByWorkshopId(workshop_id)
    }

    suspend fun getProductById(id: Int): Response<Products> {
        return RetrofitInstance.api.getProductById(id)
    }

    suspend fun getFeedbacksByWorkshopId(workshopId: String): Response<List<Feedbacks>> {
        return RetrofitInstance.api.getFeedbacksByWorkshopId(workshopId)
    }

    suspend fun getFeedback(id: Int): Response<Feedbacks> {
        return RetrofitInstance.api.getFeedback(id)
    }

    suspend fun getPromotionsByWorkshopId(workshopId: String): Response<List<Promotions>> {
        return RetrofitInstance.api.getPromotionsByWorkshopId(workshopId)
    }

    suspend fun getCustomersByWorkshopId(workshopId: String): Response<List<Customers>> {
        return RetrofitInstance.api.getCustomersByWorkshopId(workshopId)
    }

    suspend fun getPromotionById(id: Int): Response<Promotions> {
        return RetrofitInstance.api.getPromotionById(id)
    }

    suspend fun getActiveCustomersByWorkshopId(workshopId: String): Response<List<Customers>> {
        return RetrofitInstance.api.getActiveCustomersByWorkshopId(workshopId)
    }

    suspend fun getDuplicatedCustomer(workshopId: String, clientId: String): Response<List<Customers>> {
        return RetrofitInstance.api.getDuplicatedCustomer(workshopId, clientId)
    }
}