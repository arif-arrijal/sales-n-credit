package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "cost")
class Cost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val createdAt: Date = Date()
)