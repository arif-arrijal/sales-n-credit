package com.rijal.salesandcredit.module.transaction

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Transaction
import com.rijal.salesandcredit.db.entity.TransactionWithItems
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.model.UiModel
import kotlinx.coroutines.launch

class CapitalInjectionViewModel(val db: AppDatabase): ViewModel() {
    var id = 0
    val transactionList = MutableLiveData<ArrayList<TransactionWithItems>>(arrayListOf())
    val searchQuery = MutableLiveData<String>()
    val tglAkad = MutableLiveData<String>()
    val tglPembayaran = MutableLiveData<String>()
    val tglJatuhTempo = MutableLiveData<String>()
    val selectedNasabah = MutableLiveData<UiModel.Search>()
    val businessType = MutableLiveData<String>()
    val totalCapitalInjection = MutableLiveData<Double>()
    val totalRevenuePercentage = MutableLiveData<Double>()
    val totalRevenue = MutableLiveData<Double>()
    val profitSharingPercentage = MutableLiveData<Double>()
    val totalInstallment = MutableLiveData<Int>()
    val selectedData = MutableLiveData<TransactionWithItems>()
    val loadingState = MutableLiveData(false)
    val insertState = MutableLiveData<ActionState<Boolean>>()
    val akadImg = MutableLiveData<String>()
    val penyerahanImg = MutableLiveData<String>()


    fun findTransactionByType() {
        viewModelScope.launch {
            val data = if (searchQuery.valueOrDefault().isEmpty()) {
                db.transactionDao().findByTransactionModule(TransactionEnum.CAPITAL_INJECTION.name)
            } else {
                db.transactionDao().findByQueryAndTransactionModule(TransactionEnum.CAPITAL_INJECTION.name, searchQuery.valueOrDefault())
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
            db.transactionDao().insertItemWithTransaction(transaction)
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }

    fun findOne() {
        viewModelScope.launch {
            val transactionData = db.transactionDao().findOne(id)
            selectedNasabah.value = UiModel.Search(transactionData.personId, transactionData.name, transactionData.idCardNo)
            totalCapitalInjection.value = transactionData.totalCapital ?: 0.0
            totalRevenuePercentage.value = transactionData.profitSharingPercentage ?: 0.0
            tglAkad.value = transactionData.installmentPaymentDate.toFormattedString("dd-MM-yyyy")
            tglJatuhTempo.value = transactionData.paymentDueDate.toFormattedString("dd-MM-yyyy")
            tglPembayaran.value = transactionData.commitmentDate.toFormattedString("dd-MM-yyyy")
            akadImg.value = transactionData.akadImgPath ?: ""
            penyerahanImg.value = transactionData.penyerahanBarangImgPath ?: ""
            selectedData.value = transactionData
        }
    }
}