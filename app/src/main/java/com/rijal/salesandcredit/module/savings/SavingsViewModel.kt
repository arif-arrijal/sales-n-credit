package com.rijal.salesandcredit.module.savings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SavingsViewModel: ViewModel() {
    val savings = MutableLiveData(0L)
    val amount = MutableLiveData(0L)
    val mode = MutableLiveData("")
}