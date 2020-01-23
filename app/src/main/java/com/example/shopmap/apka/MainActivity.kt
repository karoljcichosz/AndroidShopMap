package com.example.shopmap.apka

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.shopmap.ListActivity
import com.example.shopmap.MapsActivity
import com.example.shopmap.R
import com.example.shopmap.geofence.GeofenceService

class MainActivity : AppCompatActivity() {

    lateinit private var intentMap: Intent
    lateinit private var intentList : Intent



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        intentList=Intent(this, ListActivity::class.java)
        intentMap=Intent(this, MapsActivity::class.java)
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        startService(Intent(this, GeofenceService::class.java))
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    fun clickList(view: View)
    {
        startActivity(intentList)
    }
    fun clickMap(view: View)
    {
        startActivity(intentMap)
    }


}
