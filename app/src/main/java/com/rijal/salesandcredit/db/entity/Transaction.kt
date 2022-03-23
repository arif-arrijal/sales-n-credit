package com.rijal.salesandcredit.db.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "transaction")
class Transaction(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0,
    val transactionNo: String = UUID.randomUUID().toString().substring(0, 7),
    val personId: Int,
    val commitmentDate: Date = Date(),
    val installmentPaymentDate: Date,
    val totalInstallment: Int,
    val paymentDueDate: Date,
    val transactionType: String = "",
    val transactionModule: String = "",
    val totalCapital: Double = 0.0,
    val profitSharingPercentage: Double = 0.0,
    val businessType: String? = null,
    val penyerahanBarangImgPath: String? = null,
    val akadImgPath: String? = null,
    val totalModal: Double = 0.0,
    var totalTerbayar: Double = 0.0,
    val totalKeuntungan: Double = 0.0,
    val nilaiTransaksi: Double = 0.0,
    var angsuranTerbaru: Int = 0
)