package com.rijal.salesandcredit.module.item

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.db.entity.Person
import com.rijal.salesandcredit.helpers.ActionState
import com.rijal.salesandcredit.helpers.notifyObserver
import com.rijal.salesandcredit.helpers.valueOrDefault
import kotlinx.coroutines.launch

class ItemViewModel(val db: AppDatabase): ViewModel() {
    var id = 0
    val name = MutableLiveData<String>()
    val series = MutableLiveData<String>()
    val uom = MutableLiveData<String>()
    val purchasePrice = MutableLiveData<Double>()
    val sellingPrice = MutableLiveData<Double>()
    val searchQuery = MutableLiveData<String>()
    val revenue = MutableLiveData<Double>()
    val revenuePercentage = MutableLiveData<Double>()

    val loadingState = MutableLiveData(false)
    val findOneState = MutableLiveData<ActionState<Item>>()
    val insertState = MutableLiveData<ActionState<Boolean>>()
    val updateState = MutableLiveData<ActionState<Boolean>>()
    val dataList = MutableLiveData<ArrayList<Item>>(arrayListOf())

    fun insert(data: Item) {
        viewModelScope.launch {
            loadingState.value = true
            db.itemDao().insert(data)
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun update(data: Item) {
        viewModelScope.launch {
            loadingState.value = true
            db.itemDao().update(data)
            updateState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun findAll() {
        viewModelScope.launch {
            dataList.valueOrDefault().apply {
                clear()
                loadingState.value = true
                val data = if (searchQuery.valueOrDefault().isEmpty()) {
                    db.itemDao().findAll()
                } else {
                    db.itemDao().findByQuery(searchQuery.valueOrDefault())
                }
                addAll(data)
                loadingState.value = false
            }
            dataList.notifyObserver()
        }
    }
    fun findById() {
        viewModelScope.launch {
            val data = db.itemDao().findOne(id)
            name.value = data.name
            series.value = data.series
            purchasePrice.value = data.purchasePrice
            sellingPrice.value = data.sellingPrice
            uom.value = data.uom
            findOneState.value = ActionState(data)
        }
    }
}