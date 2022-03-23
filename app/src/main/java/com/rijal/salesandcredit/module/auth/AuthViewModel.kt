package com.rijal.salesandcredit.module.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rijal.salesandcredit.db.AppDatabase
import com.rijal.salesandcredit.db.entity.User
import com.rijal.salesandcredit.helpers.ActionState
import com.rijal.salesandcredit.helpers.valueOrDefault
import kotlinx.coroutines.launch

class AuthViewModel(val db: AppDatabase): ViewModel() {
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val registerUserState = MutableLiveData<ActionState<Long>>()

    fun init() {
        username.value = ""
        password.value = ""
    }

    fun countUser(): LiveData<Int> {
        return db.userDao().count()
    }

    fun registerUser(user: User) {
        viewModelScope.launch {
            val id = db.userDao().insert(user)
            registerUserState.value = ActionState(id)
        }
    }

    fun findUser(): LiveData<User?> {
        return db.userDao().findByUsernameAndPassword(username.valueOrDefault(), password.valueOrDefault())
    }
}