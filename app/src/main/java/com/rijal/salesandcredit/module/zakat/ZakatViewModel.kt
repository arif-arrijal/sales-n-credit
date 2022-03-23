package com.rijal.salesandcredit.module.zakat

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.helpers.*
import kotlinx.coroutines.launch

class ZakatViewModel(val db: AppDatabase): ViewModel() {
    val zakat = MutableLiveData(0.0)
    val shodaqoh = MutableLiveData(0.0)

    val titleShodaqoh = MutableLiveData("")
    val titleZakat = MutableLiveData("")
    val totalUntungBulanIni = MutableLiveData(0.0)
    val totalBiayaBulanIni = MutableLiveData(0.0)
    val labaBersihBulanIni = MutableLiveData(0.0)
    val totalAset = MutableLiveData(0.0)
    val totalTabungan = MutableLiveData(0.0)

    val firstDayMonth = DateUtil.firstDayInMonth()
    val lastDayMonth = DateUtil.lastDayInMonth()
    val firstDayYear = DateUtil.firstDayInYear()
    val lastDayYear = DateUtil.lastDayInYear()
    val shodaqohPct = 10.0 / 100.0
    val zakatPct = 2.5 / 100.0

    fun calculate() {
        calculateShodaqoh()
        calculateZakat()
    }

    fun calculateShodaqoh() {
        viewModelScope.launch {
            val costs = db.costDao().findBetween(firstDayMonth, lastDayMonth)
            val totalInvoice = db.invoiceDao().findBetween(firstDayMonth, lastDayMonth)
            var _totalUntung = 0.0
            var _totalCost = 0.0

            costs.forEach {
                _totalCost += it.amount
            }
            totalInvoice.forEach {
                _totalUntung += it.totalKeuntungan
            }

            val labaBersih = (_totalUntung - _totalCost)

            totalUntungBulanIni.value = _totalUntung
            totalBiayaBulanIni.value = _totalCost
            labaBersihBulanIni.value = labaBersih
            shodaqoh.value = labaBersih * shodaqohPct
            titleShodaqoh.value = "Shodaqoh Bulan ${firstDayMonth.toFormattedString("MMMM yyyy")}"
        }
    }

    fun calculateZakat() {
        viewModelScope.launch {
            val totalInvoice = db.transactionDao().findByInstallmentBetween(firstDayYear, lastDayYear)
            val _totalTabungan = totalTabungan.value ?: 0.0
            var _totalAset = 0.0

            totalInvoice.forEach {
                _totalAset += it.nilaiTransaksi
            }

            totalAset.value = _totalAset
            zakat.value = (_totalAset + _totalTabungan) * zakatPct
            titleZakat.value = "Zakat Tahun ${firstDayYear.toFormattedString("yyyy")}"
        }
    }
}