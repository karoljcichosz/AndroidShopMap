package com.example.shopmap

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.shopmap.R


class ShopAdapter (
    private val context: Activity,
    private var shops: MutableList<Shop>

) : ArrayAdapter<Shop>(context, R.layout.list_single, shops) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.list_single, null, true)
        val nazwa : TextView = rowView.findViewById(R.id.nazwa)
        val id : TextView = rowView.findViewById(R.id.id)
        val cena : TextView = rowView.findViewById(R.id.cena)
        val ilosc : TextView = rowView.findViewById(R.id.ilosc)
        val x : TextView = rowView.findViewById(R.id.x)
        val y : TextView = rowView.findViewById(R.id.y)
        id.text = shops[position].pid.toString()
        nazwa.text = shops[position].nazwa
        cena.text= shops[position].opis
        ilosc.text = shops[position].promien.toString()
        x.text = shops[position].x.toString()
        y.text = shops[position].y.toString()
        return rowView
    }

    fun refresh(shops: List<Shop>) {
        this.shops = shops as MutableList<Shop>
        notifyDataSetChanged()
    }


}
