package com.example.shopmap

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.room.Room
import com.example.shopmap.R
import com.example.shopmap.geofence.GeofenceService


class ListActivity : AppCompatActivity() {

    lateinit private var shops: List<Shop>
    lateinit private var listView: ListView
    lateinit private var db: MyDB
    lateinit private var adapter: ShopAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
    }

    override fun onStart() {
        super.onStart()
        db = Room.databaseBuilder(
            applicationContext,
            MyDB::class.java, "database-name"
        ).allowMainThreadQueries().build()
        shops= db.ProductDAO().all
        adapter = ShopAdapter(this, shops as MutableList<Shop>)
        listView = findViewById(R.id.viewList)
        listView.adapter=adapter;
    }

    fun refresh(view: View) {
        adapter.clear()
        adapter.addAll(db.ProductDAO().all)
    }

    fun delete(view: View) {
        db.ProductDAO().delete(db.ProductDAO().get(((view.parent as TableRow).getVirtualChildAt(0) as AppCompatTextView).text.toString().toInt()))
        adapter.clear()
        adapter.addAll(db.ProductDAO().all)
    }

    @SuppressLint("NewApi")
    fun edit(view: View) {
        var product =db.ProductDAO().get(((view as TableRow).getVirtualChildAt(0) as AppCompatTextView).text.toString().toInt())
        val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.edit_layout,null)

        val popupWindow = PopupWindow(
            view, // Custom view to show in popup window
            LinearLayout.LayoutParams.WRAP_CONTENT, // Width of popup window
            LinearLayout.LayoutParams.WRAP_CONTENT // Window height
        )

        val nazwa = view.findViewById<TextView>(R.id.e2_nazwa)
        val cena = view.findViewById<TextView>(R.id.e2_cena)
        val ilosc = view.findViewById<TextView>(R.id.e2_ilosc)
        val x = view.findViewById<TextView>(R.id.e2_x)
        val y = view.findViewById<TextView>(R.id.e2_y)
        val buttonClose = view.findViewById<Button>(R.id.b2_close)
        val buttonAdd = view.findViewById<Button>(R.id.b2_save)

        nazwa.text=product.nazwa
        cena.text= product.opis.toString()
        ilosc.text= product.promien.toString()
        x.text= product.x.toString()
        y.text= product.y.toString()

        popupWindow.isFocusable = true
        popupWindow.update()

        buttonAdd.setOnClickListener{
            product.nazwa= nazwa.text.toString()
            product.promien= ilosc.text.toString().toInt()
            product.opis= cena.text.toString()
            product.x= x.text.toString().toDouble()
            product.y= y.text.toString().toDouble()
            db.ProductDAO().update(product)
            adapter.clear()
            adapter.addAll(db.ProductDAO().all)
            Toast.makeText(applicationContext,"zaktualizowano",Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        buttonClose.setOnClickListener{
            popupWindow.dismiss()
        }

        val root_layout : ViewGroup? = findViewById(R.id.list_layout)
        // Finally, show the popup window on app
        TransitionManager.beginDelayedTransition(root_layout)
        popupWindow.showAtLocation(
            root_layout, // Location to display popup window
            Gravity.CENTER, // Exact position of layout to display popup
            0, // X offset
            0 // Y offset
        )
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun add(view: View) {
        val inflater:LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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

        popupWindow.isFocusable = true
        popupWindow.update()
        buttonAdd.setOnClickListener{
            db.ProductDAO().insertAll(Shop(if (shops.isEmpty()) 0 else shops.last().pid+1, nazwa.text.toString(), opis.text.toString(), y.text.toString().toInt(), x.text.toString().toDouble(), y.text.toString().toDouble()))
            adapter.clear()
            adapter.addAll(db.ProductDAO().all)
            Toast.makeText(applicationContext,"Dodano",Toast.LENGTH_SHORT).show()
            var geofenceHandler = GeofenceService()
            geofenceHandler.updateGeofenceLocations(applicationContext)
            popupWindow.dismiss()
        }

        buttonClose.setOnClickListener{
            popupWindow.dismiss()
        }

        val root_layout : ViewGroup? = findViewById(R.id.list_layout)
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
