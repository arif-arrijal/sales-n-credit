package com.rijal.salesandcredit.db.dao

import androidx.room.*
import com.rijal.salesandcredit.db.entity.Invoice
import java.util.*

@Dao
interface InvoiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: Invoice)

    @Update
    suspend fun update(data: Invoice)

    @Update
    suspend fun updateTransaksi(data: com.rijal.salesandcredit.db.entity.Transaction)

    @Query("SELECT * FROM `transaction` WHERE transactionId = :id")
    suspend fun findTransactionById(id: Int): com.rijal.salesandcredit.db.entity.Transaction?

    @Query("SELECT * FROM invoice")
    suspend fun findAll(): List<Invoice>

    @Query("SELECT * FROM invoice WHERE inputDate between :start and :end")
    suspend fun findBetween(start: Date, end: Date): List<Invoice>

    @Query("SELECT * FROM invoice WHERE invoiceId = :id")
    suspend fun findOne(id: Int): Invoice

    @Query("SELECT * FROM invoice WHERE transactionId = :id AND installmentAt = :installmentAt")
    suspend fun findByTransactionIdAndInstallmentAt(id: Int, installmentAt: Int): Invoice?

    @Query("SELECT * FROM invoice WHERE transactionId = :id AND inputDate IS NULL")
    suspend fun findByTransactionIdAndNotPaid(id: Int): Invoice?

    @Query("SELECT * FROM invoice WHERE transactionId = :id ORDER BY invoiceId DESC")
    suspend fun findByTransactionId(id: Int): List<Invoice>

    @Transaction
    suspend fun updateInvoiceDanTransaksi(data: Invoice) {
        findTransactionById(data.transactionId)?.let { transaction ->
            transaction.totalTerbayar = transaction.nilaiTransaksi - data.totalUnpaid
            transaction.angsuranTerbaru = data.installmentAt
            updateTransaksi(transaction)

            val totalBayar = data.totalTerbayar
            val nilaiAsli  = data.totalTerbayar / ((transaction.profitSharingPercentage + 100.0) / 100)
            val totalKeuntungan = totalBayar - nilaiAsli
            data.totalKeuntungan = totalKeuntungan
            update(data)
        }

    }
}