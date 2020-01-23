package com.example.shopmap.geofence

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.example.AlertShop
import com.example.shopmap.MyDB
import com.example.shopmap.R
import com.example.shopmap.Shop
import com.google.android.gms.location.*
import java.util.*
import kotlin.collections.ArrayList

class GeofenceService : IntentService("geo") {

    private lateinit var locationManager: LocationManager
    private lateinit var geofencingClient : GeofencingClient
    private var geofenceList = ArrayList<Geofence>()
    private lateinit var shops : List<Shop>
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    lateinit private var db : MyDB
    private val binder = LocalBinder()

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        db= Room.databaseBuilder(
            this.applicationContext,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        geofencingClient = LocationServices.getGeofencingClient(this)
        updateGeofenceLocations(this)
        shops= db.ProductDAO().all
        shops.forEach{shop -> geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(shop.x, shop.y, shop.promien.toFloat())
                .setRequestId(shop.nazwa)
                .setExpirationDuration(MIN_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): GeofenceService= this@GeofenceService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification( geofenceTransitionDetails, applicationContext)
        }
    }

    private fun getGeofencingRequest(geofenceList: ArrayList<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            if(!geofenceList.isEmpty())
                addGeofences(geofenceList)
        }.build()
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun updateGeofenceLocations(context: Context) {
        shops= db.ProductDAO().all
        geofenceList.clear()
        shops.forEach{shop -> geofenceList.add(
            Geofence.Builder()
                .setCircularRegion(shop.x, shop.y, if(shop.promien==0) 1f else shop.promien.toFloat())
                .setRequestId(shop.nazwa)
                .setExpirationDuration(MIN_TIME)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .build())
        }
            geofencingClient.removeGeofences(getPendingIntent(context))
        if(!geofenceList.isEmpty())
            geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getPendingIntent(context))?.run {
                addOnSuccessListener {
                    // Geofences added
                    // ...
                }
                addOnFailureListener {
                    // Failed to add geofences
                    // ...
                }
            }
        }


    private val CHANNEL: String = "geofence_channel"
    private fun getGeofenceTransitionDetails(
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String {
        // Get the Ids of each geofence that was triggered.
        val triggeringGeofencesIdsList = ArrayList<String>()
        for (geofence in triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.requestId)
        }
        val triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList)

        if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_ENTER)
            return "ENTER" + ": " + triggeringGeofencesIdsString
        else if(geofenceTransition== Geofence.GEOFENCE_TRANSITION_EXIT)
            return "exit" + ": " + triggeringGeofencesIdsString
        else
            return "blad"
    }

    private fun sendNotification(geofenceTransitionDetails: String, context: Context) {
        createNotificationChannel(context)
        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, AlertShop::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        var builder = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Nowa lokalizacja")
            .setContentText(geofenceTransitionDetails)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val nm = NotificationManagerCompat.from(context)
        val random = Random()
        val m: Int = random.nextInt(9999 - 1000) + 1000
        nm.notify(m, builder.build())
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL
            val descriptionText = "AAA"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
