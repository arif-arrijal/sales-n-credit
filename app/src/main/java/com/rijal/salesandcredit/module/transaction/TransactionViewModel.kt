package com.rijal.salesandcredit.module.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.db.entity.ItemTransaction
import com.rijal.salesandcredit.db.entity.Transaction
import com.rijal.salesandcredit.db.entity.TransactionWithItems
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.model.UiModel
import kotlinx.coroutines.launch

class TransactionViewModel(val db: AppDatabase): ViewModel() {
    var id = 0
    val transactionList = MutableLiveData<ArrayList<TransactionWithItems>>(arrayListOf())
    val searchQuery = MutableLiveData<String>()
    val tglAkad = MutableLiveData<String>()
    val tglPembayaran = MutableLiveData<String>()
    val tglJatuhTempo = MutableLiveData<String>()
    val totalInstallment = MutableLiveData<Int>()
    val selectedNasabah = MutableLiveData<UiModel.Search>()
    val selectedTransaction = MutableLiveData<TransactionWithItems>()
    val goodsList = MutableLiveData<ArrayList<Item>>(arrayListOf())
    val profitSharingPercentage = MutableLiveData<Double>()
    val loadingState = MutableLiveData(false)
    val insertState = MutableLiveData<ActionState<Boolean>>()
    val akadImg = MutableLiveData<String>()
    val penyerahanImg = MutableLiveData<String>()

    val totalPurchasePrice = MutableLiveData<Double>()
    val totalSellingPrice = MutableLiveData<Double>()
    val totalRevenue = MutableLiveData<Double>()
    val totalRevenuePercentage = MutableLiveData<Double>()

    fun findTransactionByType() {
        viewModelScope.launch {
            val data = if (searchQuery.valueOrDefault().isEmpty()) {
                db.transactionDao().findByTransactionModule(TransactionEnum.SALES.name)
            } else {
                db.transactionDao().findByQueryAndTransactionModule(TransactionEnum.SALES.name, searchQuery.valueOrDefault())
            }

            transactionList.valueOrDefault().apply {
                clear()
                addAll(data)
            }
            transactionList.notifyObserver()
        }
    }

    fun save(transaction: Transaction) {
        viewModelScope.launch {
            loadingState.value = true
            db.transactionDao().insertItemWithTransaction(
                transaction,
                itemList = goodsList.valueOrDefault().map { ItemTransaction(itemId = it.itemId, qty = it.qty) }
            )
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }

    fun findOne() {
        viewModelScope.launch {
            val transactionData = db.transactionDao().findOne(id)
            val itemList = db.transactionDao().findItemByTransactionId(id)
            goodsList.valueOrDefault().apply {
                clear()
                addAll(itemList)
            }
            goodsList.notifyObserver()
            selectedNasabah.value = UiModel.Search(transactionData.personId, transactionData.name, transactionData.idCardNo)
            tglAkad.value = transactionData.installmentPaymentDate.toFormattedString("dd-MM-yyyy")
            tglJatuhTempo.value = transactionData.paymentDueDate.toFormattedString("dd-MM-yyyy")
            tglPembayaran.value = transactionData.commitmentDate.toFormattedString("dd-MM-yyyy")
            akadImg.value = transactionData.akadImgPath ?: ""
            penyerahanImg.value = transactionData.penyerahanBarangImgPath ?: ""
            selectedTransaction.value = transactionData
        }
    }
}