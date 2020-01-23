package com.example.shopmap

import androidx.room.*

@Dao
interface ShopDAO {
    @get:Query("SELECT * FROM shop")
    val all: List<Shop>

    @Query("SELECT * FROM shop WHERE pid IN (:productIds)")
    fun loadAllByIds(productIds: IntArray): List<Shop>

    @Insert
    fun insertAll(vararg shops: Shop)

    @Delete
    fun delete(shop: Shop)

    @Update
    fun update(shop: Shop)

    @Query("SELECT * FROM shop WHERE pid LIKE :id")
    fun get(id: Int): Shop
}