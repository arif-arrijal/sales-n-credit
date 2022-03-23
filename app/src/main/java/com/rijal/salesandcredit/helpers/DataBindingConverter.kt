package com.rijal.salesandcredit.helpers

import java.text.SimpleDateFormat
import java.util.*

class DataBindingConverter {
    companion object {
        @JvmStatic
        fun dateToStringDate(value: Date?): String {
            if (value == null) {
                return ""
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            return try {
                dateFormat.format(value)
            } catch (e: Exception) {
                ""
            }
        }

        @JvmStatic
        fun toRupiah(textCurrency: Double?): String {
            return (textCurrency ?: 0.0).toRupiah()
        }
    }
}