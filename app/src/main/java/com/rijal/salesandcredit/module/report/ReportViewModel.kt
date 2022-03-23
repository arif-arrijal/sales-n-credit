package com.rijal.salesandcredit.module.report

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import kotlinx.coroutines.launch

class ReportViewModel(val db: AppDatabase): ViewModel() {
    val totalModal = MutableLiveData(0.0)
    val totalUntung = MutableLiveData(0.0)
    val totalAset = MutableLiveData(0.0)
    val totalBiaya = MutableLiveData(0.0)
    val totalTertagih = MutableLiveData(0.0)
    val keuntunganBersih = MutableLiveData(0.0)

    fun calculateReportData() {
        viewModelScope.launch {
            val totalCostData = db.costDao().findAll()
            val transaction = db.transactionDao().findAll()
            val totalInvoice = db.invoiceDao().findAll()

            var _totalModal = 0.0
            var _totalUntung = 0.0
            var _totalCost = 0.0
            var _totalTertagih = 0.0
            var _totalAset = 0.0

            totalCostData.forEach { _totalCost += it.amount }

            transaction.forEach {
                _totalModal += it.totalModal
                _totalUntung += it.totalKeuntungan
                _totalAset += it.nilaiTransaksi
            }

            totalInvoice.forEach {
                if (it.inputDate != null) {
                    _totalTertagih += it.totalTerbayar
                }
            }

            val _keuntunganBersih = _totalTertagih - _totalModal - _totalCost

            totalModal.value = _totalModal
            totalUntung.value = _totalUntung
            totalBiaya.value = _totalCost
            totalTertagih.value = _totalTertagih
            totalAset.value = _totalAset
            keuntunganBersih.value = _keuntunganBersih
        }
    }
}