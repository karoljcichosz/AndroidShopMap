package com.example.shopmap

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.shopmap.geofence.GeofenceService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 1000f
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this) //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER

    }


    lateinit private var shops: List<Shop>
    lateinit private var db: MyDB

    override fun onStart() {
        super.onStart()
        db = Room.databaseBuilder(
            applicationContext,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        shops= db.ProductDAO().all

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)==null)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        else
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!.latitude, locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!.longitude)))
        shops.forEach{shop ->
            mMap.addMarker(MarkerOptions().position(LatLng(shop.x,shop.y)).title(shop.nazwa));
            if (shop.promien!=0)
                mMap.addCircle(CircleOptions().center(LatLng(shop.x,shop.y)).radius(shop.promien.toDouble()))
        }
        mMap.setOnMapClickListener { latLng: LatLng? ->
            if (latLng != null) {
                add(latLng)
            }
        }
        mMap.isMyLocationEnabled=true

    }
    private lateinit var mService : GeofenceService
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as GeofenceService.LocalBinder
            mService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
        }
    }


    override fun onLocationChanged(location: Location?) {

    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun add(latLng: LatLng) {
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.add_view,null)

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        val nazwa = view.findViewById<TextView>(R.id.e_nazwa)
        val opis = view.findViewById<TextView>(R.id.e_cena)
        val promien = view.findViewById<TextView>(R.id.e_ilosc)
        val x = view.findViewById<TextView>(R.id.e_x)
        val y = view.findViewById<TextView>(R.id.e_y)
        val buttonClose = view.findViewById<Button>(R.id.b_close)
        val buttonAdd = view.findViewById<Button>(R.id.b_add)
        x.text=latLng.latitude.toString().toBigDecimal().toString()
        y.text=latLng.longitude.toString().toBigDecimal().toString()
        popupWindow.isFocusable = true
        popupWindow.update()
        buttonAdd.setOnClickListener{
            var shop =Shop(if (shops.isEmpty()) 0 else shops.last().pid+1, nazwa.text.toString(), opis.text.toString(), if ( promien.text.toString().isBlank() ) 0; else promien.text.toString().toInt(), x.text.toString().toDouble(), y.text.toString().toDouble())
            db.ProductDAO().insertAll(shop)
            mMap.addMarker(MarkerOptions().position(LatLng(shop.x,shop.y)).title(shop.nazwa))
            if (shop.promien!=0)
                mMap.addCircle(CircleOptions().center(LatLng(shop.x,shop.y)).radius(shop.promien.toDouble()))
            Toast.makeText(applicationContext,"Dodano", Toast.LENGTH_SHORT).show()
            mService.updateGeofenceLocations(applicationContext);
            popupWindow.dismiss()
        }

        buttonClose.setOnClickListener{
            popupWindow.dismiss()
        }

        val root_layout : ViewGroup? = findViewById(R.id.map)
        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(root_layout)
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }
}
