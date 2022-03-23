package com.rijal.salesandcredit.db.dao

import androidx.room.*
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.db.entity.ItemTransaction
import com.rijal.salesandcredit.db.entity.TransactionWithItems
import java.util.*

@Dao
interface TransactionDao {

    @Transaction
    @Query("SELECT * FROM `transaction` t left join person p on p.personId = t.personId where transactionModule = :module ORDER by transactionId DESC")
    suspend fun findByTransactionModule(module: String): List<TransactionWithItems>

    @Transaction
    @Query("SELECT * FROM `transaction` t left join person p on p.personId = t.personId where transactionModule = :module and p.name like '%' || :query || '%' ORDER by transactionId DESC")
    suspend fun findByQueryAndTransactionModule(module: String, query: String): List<TransactionWithItems>

    @Transaction
    @Query("SELECT * FROM `transaction` t left join person p on p.personId = t.personId where t.transactionId = :id")
    suspend fun findOne(id: Int): TransactionWithItems

    @Query("SELECT * FROM `transaction` WHERE transactionModule = :module")
    suspend fun findByModule(module: String): List<com.rijal.salesandcredit.db.entity.Transaction>

    @Query("SELECT * FROM `transaction`")
    suspend fun findAll(): List<com.rijal.salesandcredit.db.entity.Transaction>

    @Transaction
    @Query("SELECT i.*, it.qty FROM item_transaction it left join item i ON i.itemId = it.itemId WHERE it.transactionId = :id")
    suspend fun findItemByTransactionId(id: Int): List<Item>

    @Transaction
    @Query("SELECT i.*, it.qty FROM item_transaction it left join item i ON i.itemId = it.itemId")
    suspend fun findItemTransaction(): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: com.rijal.salesandcredit.db.entity.Transaction): Long

    @Insert
    suspend fun insertItemTransaction(data: List<ItemTransaction>)

    @Query("SELECT Coalesce(SUM(Coalesce(totalCapital, 0)), 0)  FROM `transaction` WHERE installmentPaymentDate BETWEEN :start and :end")
    suspend fun findSummaryInjectionByModuleAndInstallmentBetween(start: Date, end: Date): Double

    @Transaction
    @Query("SELECT Coalesce(SUM(Coalesce(it.qty, 0)  * Coalesce(i.purchasePrice, 0)), 0) FROM item_transaction it left join item i on i.itemId = it.itemId left join `transaction` t on t.transactionId = it.transactionId  WHERE installmentPaymentDate BETWEEN :start and :end")
    suspend fun findSummarySalesByInstallmentBetween(start: Date, end: Date): Double

    @Transaction
    @Query("SELECT * FROM `transaction` WHERE installmentPaymentDate BETWEEN :start and :end")
    suspend fun findByInstallmentBetween(start: Date, end: Date): List<com.rijal.salesandcredit.db.entity.Transaction>

    @Transaction
    suspend fun findSummaryByInstallmentAt(start: Date, end: Date): Double {
        val summaryInjection = findSummaryInjectionByModuleAndInstallmentBetween(start, end)
        val summarySales = findSummarySalesByInstallmentBetween(start, end)
        return summaryInjection + summarySales
    }

    @Transaction
    suspend fun insertItemWithTransaction(data: com.rijal.salesandcredit.db.entity.Transaction, itemList: List<ItemTransaction>? = null) {
        val id = insert(data)

        itemList?.let { list ->
            if (list.isNotEmpty()) {
                list.forEach { it.transactionId = id.toInt() }
                insertItemTransaction(list)
            }
        }

    }
}