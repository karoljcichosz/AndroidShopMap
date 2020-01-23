package com.example.shopmap

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "Shop" )
class Shop {
    @PrimaryKey
    var pid: Int = 0
    @ColumnInfo(name = "nazwa")
    var nazwa: String? = null
    @ColumnInfo(name = "opis")
    var opis: String? = null
    @ColumnInfo(name = "promien")
    var promien: Int = 0
    @ColumnInfo(name = "x")
    var x: Double = 0.0
    @ColumnInfo(name = "y")
    var y: Double = 0.0

    constructor(pid : Int, nazwa : String, opis : String, promien : Int, x : Double, y : Double)
    {
        this.pid=pid
        this.nazwa=nazwa
        this.opis=opis
        this.promien=promien
        this.x=x
        this.y=y
    }
}
