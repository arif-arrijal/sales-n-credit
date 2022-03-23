package com.rijal.salesandcredit.helpers

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("textDate")
fun setTextDate(view: TextView, textTime: Date) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val text = try {
        dateFormat.format(textTime)
    } catch (e: Exception) {
        ""
    }

    view.text = text
}

@BindingAdapter("textTime")
fun setTextTime(view: TextView, textTime: Date) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val text = try {
        dateFormat.format(textTime)
    } catch (e: Exception) {
        ""
    }

    view.text = text
}

@BindingAdapter("textCurrency")
fun setTextCurrency(view: TextView, textCurrency: Double?) {
    view.text = (textCurrency ?: 0.0).toRupiah()
}
@BindingAdapter("textCurrency")
fun setTextCurrency(view: TextView, textCurrency: Int?) {
    view.text = (textCurrency ?: 0).toRupiah()
}
@BindingAdapter("textCurrency")
fun setTextCurrency(view: TextView, textCurrency: Long?) {
    view.text = (textCurrency ?: 0).toRupiah()
}

@BindingAdapter("textPoin")
fun setTextPoin(view: TextView, textPoin: Double?) {
    val numberFormat = NumberFormat.getInstance()
    numberFormat.maximumFractionDigits = 0
    view.text = numberFormat.format(textPoin ?: 0.0)
}


@BindingAdapter("textDateTime")
fun setDateTime(view: TextView, textTime: Date) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val text = try {
        dateFormat.format(textTime)
    } catch (e: Exception) {
        ""
    }

    view.text = text
}