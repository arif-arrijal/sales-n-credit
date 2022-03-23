package com.rijal.salesandcredit.db.entity

import java.util.*

class TransactionWithItems(
    val transactionId: Int = 0,
    val transactionNo: String = UUID.randomUUID().toString().substring(0, 7),
    val personId: Int,
    val commitmentDate: Date = Date(),
    val installmentPaymentDate: Date,
    val totalInstallment: Int,
    val paymentDueDate: Date,
    val transactionType: String = "",
    val transactionModule: String = "",
    val totalCapital: Double? = null,
    val profitSharingPercentage: Double? = null,
    val businessType: String? = null,
    val name: String = "",
    val address: String = "",
    val idCardNo: String = "",
    val phone: String = "",
    val penyerahanBarangImgPath: String? = null,
    val akadImgPath: String? = null,
    val totalModal: Double = 0.0,
    var totalTerbayar: Double = 0.0,
    val totalKeuntungan: Double = 0.0,
    val nilaiTransaksi: Double = 0.0,
    val angsuranTerbaru: Int = 0
)