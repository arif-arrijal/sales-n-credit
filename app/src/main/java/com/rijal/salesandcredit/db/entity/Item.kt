package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
class Item(
    @PrimaryKey(autoGenerate = true) var itemId: Int = 0,
    val name: String = "",
    val series: String = "",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    var qty: Int = 0,
    var uom: String = ""
    ) {
    fun totalPrice(): Double {
        return sellingPrice * qty
    }
}