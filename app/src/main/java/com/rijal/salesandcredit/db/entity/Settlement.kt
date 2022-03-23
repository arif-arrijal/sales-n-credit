package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "settlement")
class Settlement(
    @PrimaryKey(autoGenerate = true) val settlementId: Int = 0,
    val transactionId: Int,
    val receivedInstallment: Double,
    val installmentAt: Int,
    val createdDate: Date
)