package com.example.shopmap

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Shop::class], version = 1)
abstract class MyDB : RoomDatabase() {
    abstract fun ProductDAO(): ShopDAO

}