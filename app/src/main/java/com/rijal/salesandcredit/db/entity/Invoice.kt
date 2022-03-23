package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "invoice")
class Invoice(
    @PrimaryKey(autoGenerate = true) val invoiceId: Int = 0,
    val transactionId: Int,
    val totalPayment: Double,
    val installmentAt: Int,
    val maximumDueDate: Date,
    val totalTerbayar: Double = 0.0,
    var totalKeuntungan: Double = 0.0,
    val totalUnpaid: Double = 0.0,
    val invoiceDueDate: Date,
    var inputDate: Date? = null
)