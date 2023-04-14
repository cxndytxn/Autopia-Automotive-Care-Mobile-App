package com.example.autopia.activities.api

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Response
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.autopia.activities.model.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ApiViewModel(private val repo: Repository) : ViewModel(), LifecycleObserver {

    val service: MutableLiveData<Response<Services>> = MutableLiveData()
    val vehicle: MutableLiveData<Response<Vehicles>> = MutableLiveData()
    val appointment: MutableLiveData<Response<Appointments>> = MutableLiveData()
    val vehicles: MutableLiveData<Response<List<Vehicles>>> = MutableLiveData()
    val services: MutableLiveData<Response<List<Services>>> = MutableLiveData()
    val appointments: MutableLiveData<Response<List<Appointments>>> = MutableLiveData()
    val serviceReminders: MutableLiveData<Response<List<ServiceReminders>>> = MutableLiveData()

    //val calendarEntities: MutableLiveData<Response<List<CalendarEntities>>> = MutableLiveData()
    val notifications: MutableLiveData<Response<List<Notifications>>> = MutableLiveData()

    //val notification: MutableLiveData<Response<Notifications>> = MutableLiveData()
    val products: MutableLiveData<Response<List<Products>>> = MutableLiveData()
    val product: MutableLiveData<Response<Products>> = MutableLiveData()
    val feedbacks: MutableLiveData<Response<List<Feedbacks>>> = MutableLiveData()
    val feedback: MutableLiveData<Response<Feedbacks>> = MutableLiveData()
    val promotions: MutableLiveData<Response<List<Promotions>>> = MutableLiveData()
    val promotion: MutableLiveData<Response<Promotions>> = MutableLiveData()
    val customers: MutableLiveData<Response<List<Customers>>> = MutableLiveData()
    val customer: MutableLiveData<Response<Customers>> = MutableLiveData()

    //Example variables for JavaRx
    val loading = MutableLiveData<Boolean>()
    val loadError = MutableLiveData<Boolean>()
    private val disposable = CompositeDisposable()

    //Example method with JavaRx
//    private suspend fun fetchNotifications(user_id: String) {
//        loading.value = true
//        disposable.add(
//            RetrofitInstance.fetchNotifications(user_id).subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(object : DisposableSingleObserver<Response<List<Notifications>>>() {
//                    override fun onSuccess(t: Response<List<Notifications>>) {
//                        notifications.value = t
//                        loadError.value = false
//                        loading.value = false
//                    }
//
//                    override fun onError(e: Throwable) {
//                        loadError.value = true
//                        loading.value = false
//                    }
//                })
//        )
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        disposable.clear()
//    }

    //------------------------------------------------------Original codes---------------------------------------------------------------------

    fun getNotificationsByUserId(user_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getNotificationsByUserId(user_id)
                notifications.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getVehiclesByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getVehiclesByClientId(client_id)
                vehicles.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (s: SocketTimeoutException) {

            }
        }
    }

    fun getVehicleById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getVehicleById(id)
                vehicle.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getServicesByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getServicesByWorkshopId(workshop_id)
                services.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getServiceById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getServiceById(id)
                service.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getProductById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getProductById(id)
                product.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    fun getAppointments() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response = repo.getAppointments()
//                appointments.postValue(response)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    fun getAppointmentById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getAppointmentById(id)
                appointment.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getNoShowAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getNoShowAppointmentsByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getScheduledAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getScheduledAppointmentsByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRescheduleAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getRescheduleAppointmentsByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRescheduleAppointmentsByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getRescheduleAppointmentsByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPendingAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getPendingAppointmentsByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAcceptedAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withTimeout(TimeUnit.SECONDS.toMillis(200L)) {
                try {
                    val response = repo.getAcceptedAppointmentsByWorkshopId(workshop_id)
                    appointments.postValue(response)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun getRejectedAppointmentsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getRejectedAppointmentsByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getHistoriesByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getHistoriesByWorkshopId(workshop_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPendingAppointmentsByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getPendingAppointmentsByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getAcceptedAppointmentsByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getAcceptedAppointmentsByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getRejectedAppointmentsByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getRejectedAppointmentsByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getHistoriesByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getHistoriesByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

//    fun getCalendarEntitiesByWorkshopId(workshop_id: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val response = repo.getCalendarEntitiesByWorkshopId(workshop_id)
//                calendarEntities.postValue(response)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }

    fun getNoShowAppointmentsByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getNoShowAppointmentsByClientId(client_id)
                appointments.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getServiceRemindersByClientId(client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getServiceRemindersByClientId(client_id)
                serviceReminders.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFeedbacksByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFeedbacksByWorkshopId(workshop_id)
                feedbacks.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getProductsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getProductsByWorkshopId(workshop_id)
                products.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getFeedback(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getFeedback(id)
                feedback.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPromotionById(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getPromotionById(id)
                promotion.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getPromotionsByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getPromotionsByWorkshopId(workshop_id)
                promotions.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getCustomersByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getCustomersByWorkshopId(workshop_id)
                customers.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getActiveCustomersByWorkshopId(workshop_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getActiveCustomersByWorkshopId(workshop_id)
                customers.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getDuplicatedCustomer(workshop_id: String, client_id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = repo.getDuplicatedCustomer(workshop_id, client_id)
                customers.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}