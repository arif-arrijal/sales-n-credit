package com.rijal.salesandcredit.module.person

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Person
import com.rijal.salesandcredit.helpers.ActionState
import com.rijal.salesandcredit.helpers.notifyObserver
import com.rijal.salesandcredit.helpers.valueOrDefault
import kotlinx.coroutines.launch

class PersonViewModel(val db: AppDatabase): ViewModel() {
    var id = 0
    val name = MutableLiveData<String>()
    val address = MutableLiveData<String>()
    val idCardNo = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val searchQuery = MutableLiveData<String>()
    val imgPath = MutableLiveData<String>()

    val loadingState = MutableLiveData(false)
    val insertState = MutableLiveData<ActionState<Boolean>>()
    val updateState = MutableLiveData<ActionState<Boolean>>()
    val dataList = MutableLiveData<ArrayList<Person>>(arrayListOf())

    fun insert(data: Person) {
        viewModelScope.launch {
            loadingState.value = true
            db.personDao().insert(data)
            insertState.value = ActionState(true)
            loadingState.value = false
        }
    }
    fun update(data: Person) {
        viewModelScope.launch {
            loadingState.value = true
            db.personDao().update(data)
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
                    db.personDao().findAll()
                } else {
                    db.personDao().findByQuery(searchQuery.valueOrDefault())
                }
                addAll(data)
                loadingState.value = false
            }
            dataList.notifyObserver()
        }
    }
    fun findById() {
        viewModelScope.launch {
            val data = db.personDao().findOne(id)
            name.value = data.name
            address.value = data.address
            idCardNo.value = data.idCardNo
            phone.value = data.phone
            imgPath.value = data.idCardImgPath
        }
    }
}