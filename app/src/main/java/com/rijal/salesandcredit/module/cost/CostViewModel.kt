package com.rijal.salesandcredit.module.cost

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Cost
import com.rijal.salesandcredit.helpers.ActionState
import com.rijal.salesandcredit.helpers.notifyObserver
import com.rijal.salesandcredit.helpers.toFormattedString
import com.rijal.salesandcredit.helpers.valueOrDefault
import kotlinx.coroutines.launch

class CostViewModel(val db: AppDatabase): ViewModel() {
    var id = 0
    val searchQuery = MutableLiveData<String>()
    val selectedData = MutableLiveData<Cost>()
    val dataList = MutableLiveData<ArrayList<Cost>>(arrayListOf())

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val amount = MutableLiveData<Double>()
    val createdAt = MutableLiveData<String>()

    val loadingState = MutableLiveData(false)
    val insertState = MutableLiveData<ActionState<Boolean>>()

    fun insert(data: Cost) {
        viewModelScope.launch {
            loadingState.value = true
            db.costDao().insert(data)
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun findAll() {
        viewModelScope.launch {
            dataList.valueOrDefault().apply {
                clear()
                loadingState.value = true
                val data = if (searchQuery.valueOrDefault().isEmpty()) {
                    db.costDao().findAll()
                } else {
                    db.costDao().findByQuery(searchQuery.valueOrDefault())
                }
                addAll(data)
                loadingState.value = false
            }
            dataList.notifyObserver()
        }
    }


    fun findById() {
        viewModelScope.launch {
            val data = db.costDao().findOne(id)
            title.value = data.title
            description.value = data.description
            amount.value = data.amount
            createdAt.value = data.createdAt.toFormattedString("dd-MM-yyyy")
            selectedData.value = data
        }
    }
}