package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_transaction")
class ItemTransaction(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var transactionId: Int = 0,
    var itemId: Int = 0,
    var qty: Int = 0
)