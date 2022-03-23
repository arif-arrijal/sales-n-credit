package com.rijal.salesandcredit.module.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.helpers.SearchEnum
import com.rijal.salesandcredit.helpers.notifyObserver
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.model.UiModel
import kotlinx.coroutines.launch

class SearchViewModel(val db: AppDatabase): ViewModel() {

    var enum: String = ""
    val searchQuery = MutableLiveData<String>()
    val dataList = MutableLiveData<ArrayList<UiModel.Search>>(arrayListOf())
    val itemList = MutableLiveData<ArrayList<Item>>(arrayListOf())


    fun search() {
        viewModelScope.launch {
            val query = searchQuery.valueOrDefault()
            dataList.valueOrDefault().clear()
            itemList.valueOrDefault().clear()

            when (enum) {
                SearchEnum.ITEM.toString() -> {
                    val result = when {
                        query.isEmpty() -> db.itemDao().findAll()
                        else -> db.itemDao().findByQuery(query)
                    }
                    dataList.valueOrDefault().addAll(result.map { y -> UiModel.Search(y.itemId, y.name, y.series) })
                    itemList.valueOrDefault().addAll(result)
                }
                SearchEnum.PERSON.toString() -> {
                    val result = when {
                        query.isEmpty() -> db.personDao().findAll()
                        else -> db.personDao().findByQuery(query)
                    }
                    dataList.valueOrDefault().addAll(result.map { y -> UiModel.Search(y.personId, y.name, y.phone) })
                }
            }

            dataList.notifyObserver()
        }
    }
}