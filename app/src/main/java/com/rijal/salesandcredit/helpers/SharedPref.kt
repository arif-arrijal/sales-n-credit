package com.rijal.salesandcredit.helpers

import android.content.Context
import android.content.SharedPreferences

class SharedPref(context: Context) {
    companion object {
        const val IS_LOGIN = "is_login"
        const val SAVINGS = "savings"
    }

    private val sp: SharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    private val spe: SharedPreferences.Editor = sp.edit()

    private val allPreferences = arrayListOf(
        IS_LOGIN
    )

    fun putData(key: String, value: Any?) {
        if (value == null) {
            return
        }

        if (value is Boolean) {
            spe.putBoolean(key, value)
        } else {
            spe.putString(key, value.toString())
        }
        spe.commit()
    }

    fun getBoolean(key: String, default: Boolean = false) = sp.getBoolean(key, default)
    fun getLong(key: String, default: Long = 0) = sp.getLong(key, default)
    fun getString(key: String, default: String? = null) = sp.getString(key, default)

    fun cleanPreference() {
        allPreferences.forEach { pref ->
            spe.putString(pref, null)
        }
        spe.commit()
    }

    fun cleanSpecificPreference(key: String) {
        spe.putString(key, null)
        spe.commit()
    }

}
