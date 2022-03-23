package com.rijal.salesandcredit.module.invoice

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Invoice
import com.rijal.salesandcredit.db.entity.TransactionWithItems
import com.rijal.salesandcredit.helpers.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class InvoiceViewModel(val db: AppDatabase): ViewModel() {
    var id = MutableLiveData(0)
    var transactionId = 0
    val selectedInvoice = MutableLiveData<Invoice>()
    val maximumDueDate = MutableLiveData<String>()
    val invoiceDueDate = MutableLiveData<String>()
    val inputDate = MutableLiveData<String>()
    val totalTerbayar = MutableLiveData<Double>()
    val totalPayment = MutableLiveData<Double>()
    val totalUnpaid = MutableLiveData<Double>()
    val totalPaid = MutableLiveData<Double>()
    val installmentAt = MutableLiveData<Int>()
    val selectedTransaction = MutableLiveData<TransactionWithItems>()
    val collectibility = MutableLiveData<Int>()
    val unpaidInvoice = MutableLiveData<Invoice?>()

    val invoiceList = MutableLiveData<ArrayList<Invoice>>(arrayListOf())
    val loadingState = MutableLiveData(false)
    val insertState = MutableLiveData<ActionState<Boolean>>()
    val checkCollectibilityState = MutableLiveData<ActionState<Boolean>>()
    val updateState = MutableLiveData<ActionState<Boolean>>()
    val generateInvoiceState = MutableLiveData<ActionState<String>>()

    fun findAll() {
        viewModelScope.launch {
            val data = db.invoiceDao().findByTransactionId(transactionId)
            invoiceList.valueOrDefault().apply {
                clear()
                addAll(data)
            }
            invoiceList.notifyObserver()
        }
    }

    fun save(invoice: Invoice) {
        viewModelScope.launch {
            val installmentAt = db.invoiceDao().findByTransactionIdAndInstallmentAt(transactionId, invoice.installmentAt)
            if (installmentAt != null) {
                insertState.value = ActionState(message = "Data tagihan sudah ada. Silahkan buat data tagihan lain.")
                return@launch
            }

            db.invoiceDao().insert(invoice)
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun update(invoice: Invoice) {
        viewModelScope.launch {
            db.invoiceDao().updateInvoiceDanTransaksi(invoice)
            updateState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun generateInvoice(invoice: Invoice) {
        viewModelScope.launch {
            val transaction = db.transactionDao().findOne(invoice.transactionId)
            val data = "Kepada Bapak/Ibu ${transaction?.name} yang terhormat." +
                    "\n\n" +
                    "Berdasarkan Akad nomor ${transaction?.transactionNo}, dengan ini kami berniat untuk melakukan penagihan ke ${invoice.installmentAt} sebesar ${invoice.totalPayment.toRupiah()}." +
                    "\n\n" +
                    "Silahkan melakukan pembayaran sebelum jatuh tempo pada ${invoice.maximumDueDate.toFormattedString("dd-MM-yyyy")}." +
                    "\n\n" +
                    "Demikian kami sampaikan, Atas kerjasamanya kami ucapkan Terima Kasih."
            generateInvoiceState.value = ActionState(data)
        }
    }

    fun findOne() {
        viewModelScope.launch {
            val data = db.invoiceDao().findOne(id.valueOrDefault())
            maximumDueDate.value = data.maximumDueDate.toFormattedString("dd-MM-yyyy")
            transactionId = data.transactionId
            invoiceDueDate.value = data.invoiceDueDate.toFormattedString("dd-MM-yyyy")
            inputDate.value = data.inputDate?.toFormattedString("dd-MM-yyyy")
            installmentAt.value = data.installmentAt
            totalPayment.value = data.totalPayment
            totalUnpaid.value = data.totalUnpaid
            totalTerbayar.value = data.totalTerbayar
            selectedInvoice.value = data
        }
    }

    fun findUnpaid() {
        viewModelScope.launch {
            val data = db.invoiceDao().findByTransactionIdAndNotPaid(transactionId)
            unpaidInvoice.value = data
        }
    }

    fun checkCollectibility() {
        viewModelScope.launch {
            try {
                checkCollectibilityState.value = ActionState(isLoading = true)
                val transactionData = db.transactionDao().findOne(transactionId)
                transactionData?.apply {
                    totalPaid.value = this.totalTerbayar
                    selectedTransaction.value = this
                }

                var _collectibility = 0

                if (transactionData != null && transactionData.totalInstallment > 0) {
                    var notPay = 0
                    var latePay = 0
                    val invoice = db.invoiceDao().findByTransactionId(transactionId)
                    val dateNow = Date()

                    invoice.forEach {

                        if (it.inputDate == null && it.maximumDueDate < dateNow) {
                            notPay ++
                        } else if (it.inputDate != null && it.inputDate!! > it.maximumDueDate) {
                            latePay ++
                        }
                    }
                    if (notPay >= 3) {
                        _collectibility = 2
                    } else if (latePay > 0) {
                        _collectibility = 1
                    }
                }
                collectibility.value = _collectibility
                checkCollectibilityState.value = ActionState(true)
            } catch (e: Exception) {
                Timber.i(e.message)
                checkCollectibilityState.value = ActionState(isLoading = false)
            }

        }
    }
}