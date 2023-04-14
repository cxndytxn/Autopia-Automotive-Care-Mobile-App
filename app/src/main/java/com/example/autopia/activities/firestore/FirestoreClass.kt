package com.example.autopia.activities.firestore

import android.util.Log
import android.widget.Toast
import com.example.autopia.activities.model.Users
import com.example.autopia.activities.model.Workshops
import com.example.autopia.activities.ui.SignUpActivity
import com.example.autopia.activities.ui.WorkshopSignUpActivity
import com.example.autopia.activities.utils.Constants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FirestoreClass {
    private val db = FirebaseFirestore.getInstance()

    fun fetchUserInfo(): Task<DocumentSnapshot> {
        return db.collection(Constants.Users).document(fetchUserID()).get()
    }

    private fun fetchUserID(): String {
        val user = FirebaseAuth.getInstance().currentUser
        var userID = ""
        if (user != null) {
            userID = user.uid
        }
        return userID
    }

    fun fetchUserByID(uid: String): Task<DocumentSnapshot> {
        return db.collection(Constants.Users).document(uid).get()
    }

    fun fetchWorkshopInfo(workshop_ID: String): Task<DocumentSnapshot> {
        return db.collection(Constants.Users).document(workshop_ID).get()
    }

    fun fetchAllWorkshopsInfo(): Task<QuerySnapshot> {
        return db.collection(Constants.Users).whereEqualTo("userType", "workshop").get()
    }

    fun fetchWorkshopsOnQuery(queryText: String): Task<QuerySnapshot> {
        return db.collection(Constants.Users).whereEqualTo("userType", "workshop")
            .orderBy("lowerName").startAt(queryText).endAt(queryText + "\uf8ff").get()
    }

    fun fetchNearbyWorkshopsInfo(
        nearbyWorkshopList: MutableList<String>
    ): Task<QuerySnapshot> {
        Log.d("received", nearbyWorkshopList.toString())
        return db.collection(Constants.Users).whereEqualTo("userType", "workshop")
            .whereIn("workshopName", nearbyWorkshopList).get()
    }

    fun registerUser(activity: SignUpActivity, userInfo: Users) {
        db.collection(Constants.Users).document(userInfo.id).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.registrationSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity.applicationContext,
                    "Error. " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun registerWorkshop(activity: WorkshopSignUpActivity, workshopInfo: Workshops) {
        db.collection(Constants.Users).document(workshopInfo.id)
            .set(workshopInfo, SetOptions.merge()).addOnSuccessListener {
                activity.registrationSuccess()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    activity.applicationContext,
                    "Error. " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

//    fun fetchVehicleInfo(id: Int): Task<DocumentSnapshot> {
//        return db.collection(Constants.Vehicles).document(id.toString()).get()
//    }

    fun fetchServiceInfo(id: Int): Task<DocumentSnapshot> {
        return db.collection(Constants.Services).document(id.toString()).get()
    }

//    fun fetchAppointmentInfo(id: String): Task<DocumentSnapshot> {
//        return db.collection(Constants.Appointments).document(id).get()
//    }
//
//    fun setAppointmentStatus(id: String, status: String): Task<Void> {
//        return db.collection(Constants.Appointments).document(id).update("status", status)
//    }
//
//    fun fetchUpcomingAppointmentInfoByWorkshopId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("workshopId", id)
//            .whereEqualTo("status", "accepted").get()
//    }
//
//    fun fetchUpcomingAppointmentInfoByUserId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("id", id)
//            .whereEqualTo("status", "accepted").get()
//    }
//
//    fun fetchPendingAppointmentInfoByWorkshopId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("workshopId", id)
//            .whereEqualTo("status", "pending").get()
//    }
//
//    fun fetchPendingAppointmentInfoByUserId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("id", id)
//            .whereEqualTo("status", "pending").get()
//    }
//
//    fun fetchRejectedAppointmentInfoByWorkshopId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("workshopId", id)
//            .whereEqualTo("status", "rejected").get()
//    }
//
//    fun fetchRejectedAppointmentInfoByUserId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("id", id)
//            .whereEqualTo("status", "rejected").get()
//    }
//
//    fun fetchAppointmentHistoryInfoByUserId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("id", id)
//            .whereEqualTo("status", "done").get()
//    }
//
//    fun fetchAppointmentHistoryInfoByWorkshopId(id: String): Task<QuerySnapshot> {
//        return db.collection(Constants.Appointments).whereEqualTo("workshopId", id)
//            .whereEqualTo("status", "done").get()
//    }
//
//    fun markAppointmentDone(id: String): Task<Void> {
//        return db.collection(Constants.Appointments).document(id).update("status", "done")
//    }
//
//    fun acceptAppointment(id: String): Task<Void> {
//        return db.collection(Constants.Appointments).document(id).update("status", "accepted")
//    }
//
//    fun rejectAppointment(id: String): Task<Void> {
//        return db.collection(Constants.Appointments).document(id).update("status", "rejected")
//    }
//
//    fun cancelAppointment(id: String): Task<Void> {
//        return db.collection(Constants.Appointments).document(id).update("status", "cancelled")
//    }

    fun fetchFavoriteWorkshops(id: String): Task<QuerySnapshot> {
        return db.collection(Constants.FavoriteWorkshops).whereEqualTo("clientId", id).get()
    }
}