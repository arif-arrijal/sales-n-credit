package com.rijal.salesandcredit.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.adapter.EasyAdapter
import kotlinx.coroutines.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson
import java.text.NumberFormat

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.hideKeyboard() =
    view?.let { activity?.hideKeyboard(it) }

fun Activity.hideKeyboard() =
    hideKeyboard(currentFocus ?: View(this))

fun Context.hideKeyboard(view: View) = view.hideKeyboard()

fun Context.putPrefData(key: String, value: Any?) =
    SharedPref(this).putData(key, value)

fun Context.removePrefData(key: String) {
    SharedPref(this).cleanSpecificPreference(key)
}
fun Context.getPrefBoolean(key: String, default: Boolean = false) =
    SharedPref(this).getBoolean(key, default)

fun Context.getPrefString(key: String, default: String = "") =
    SharedPref(this).getString(key, default)

fun Context.getPrefLong(key: String, default: Long = 0) =
    SharedPref(this).getLong(key, default)

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    Timber.i("[TOAST] $message")
}

fun Fragment.toast(message: String) = activity?.toast(message)

fun MutableLiveData<Boolean>.valueOrDefault(): Boolean = this.value ?: false
fun MutableLiveData<String>.valueOrDefault(): String = this.value ?: ""
fun MutableLiveData<Int>.valueOrDefault(): Int = this.value ?: 0
fun <T> LiveData<ArrayList<T>>.valueOrDefault(): ArrayList<T> = this.value ?: arrayListOf()
fun <T> LiveData<List<T>>.valueOrDefault(): List<T> = this.value ?: listOf()

fun <T> MutableLiveData<ArrayList<T>>.valueOrDefault(): ArrayList<T> = this.value ?: arrayListOf()
fun <T> MutableLiveData<List<T>>.valueOrDefault(): List<T> = this.value ?: listOf()
fun <T> MutableLiveData<T>.notifyObserver() = this.apply { this.value = this.value }
fun <T> MutableLiveData<T>.notifyObserverAsync() = this.apply { this.postValue(this.value) }

fun Fragment.snackbar(
    message: String?,
    type: InfoEnum,
    duration: Int = Snackbar.LENGTH_SHORT
): Snackbar? {
    return activity?.snackbar(message, type, duration)
}

fun Fragment.errorSnackbar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) : Snackbar? {
    return activity?.snackbar(message, InfoEnum.DANGER, duration)
}
fun Fragment.infoSnackbar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) : Snackbar? {
    return activity?.snackbar(message, InfoEnum.INFO, duration)
}
fun Fragment.successSnackbar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) : Snackbar? {
    return activity?.snackbar(message, InfoEnum.SUCCESS, duration)
}
fun Fragment.warningSnackbar(message: String?, duration: Int = Snackbar.LENGTH_SHORT) : Snackbar? {
    return activity?.snackbar(message, InfoEnum.WARNING, duration)
}

fun Context.getColorId(id: Int): Int = ContextCompat.getColor(this, id)

fun Fragment.getColorId(id: Int): Int {
    return context?.getColorId(id) ?: 0
}

fun Activity.snackbar(
    message: String?,
    type: InfoEnum,
    duration: Int = Snackbar.LENGTH_SHORT
): Snackbar? {
    if (message == null) return null
    val view = window.decorView.rootView.findViewById<View>(android.R.id.content)
    val sb = Snackbar.make(view, message, duration)
    sb.animationMode = Snackbar.ANIMATION_MODE_SLIDE
    sb.setBackgroundTint(
        getColorId(
            when (type) {
                InfoEnum.INFO -> R.color.info_color
                InfoEnum.SUCCESS -> R.color.success_color
                InfoEnum.WARNING -> R.color.warning_color
                InfoEnum.DANGER -> R.color.danger_color
            }
        )
    )
    sb.show()
    Timber.i("[SNACKBAR] $message")
    return sb
}

fun View.visible(isVisible: Boolean, hideOption: Int = View.GONE){
    if(isVisible && visibility != View.VISIBLE){
        visibility = View.VISIBLE
    }else if(!isVisible && visibility == View.VISIBLE){
        visibility = hideOption
    }
}

fun TextInputLayout.setHintStyle(id: Int) {
    doOnLayout {
        setHintTextAppearance(id)
    }
}

fun Date.toFormattedString(format: String): String {
    val dateFormatter = SimpleDateFormat(format, Locale("id", "ID"))
    return dateFormatter.format(this)
}

fun String.toDate(format: String): Date {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return try {
        dateFormatter.parse(this)
    } catch (e: Exception) {
        Date()
    }
}

fun EditText.watch(
    beforeChangedDelay: Long = 0,
    onBeforeChanged: ((String) -> Unit)? = null,
    onChangedDelay: Long = 0,
    onChanged: ((String) -> Unit)? = null,
    afterChangedDelay: Long = 0,
    onAfterChanged: ((String) -> Unit)? = null
) {

    var lastInput = ""
    val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var beforeChangedJob: Job? = null
    var onChangedJob: Job? = null
    var afterChangedJob: Job? = null

    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            onBeforeChanged?.let { beforeChanged ->
                if (s.toString().isNotEmpty()) {
                    val newtInput = s.toString()
                    beforeChangedJob?.cancel()
                    if (lastInput != newtInput) {
                        lastInput = newtInput

                        beforeChangedJob = uiScope.launch {
                            delay(beforeChangedDelay)
                            if (lastInput == newtInput) {
                                beforeChanged(newtInput)
                            }
                        }
                    }
                }
            }
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChanged?.let { changed ->
                val newtInput = s.toString() ?: ""
                onChangedJob?.cancel()
                if (lastInput != newtInput) {
                    lastInput = newtInput

                    onChangedJob = uiScope.launch {
                        delay(onChangedDelay)
                        if (lastInput == newtInput) {
                            changed(newtInput)
                        }
                    }
                }
            }
        }

        override fun afterTextChanged(editable: Editable?) {
            onAfterChanged?.let { afterChanged ->
                if (editable != null) {
                    val newtInput = editable.toString()
                    afterChangedJob?.cancel()
                    if (lastInput != newtInput) {
                        lastInput = newtInput

                        afterChangedJob = uiScope.launch {
                            delay(afterChangedDelay)
                            if (lastInput == newtInput) {
                                afterChanged(newtInput)
                            }
                        }
                    }
                }
            }
        }

    })
}

fun Activity.informationDialog(content: String, onOk: (() -> Unit)?) {
    val inflater = layoutInflater
    val loadingView = inflater.inflate(R.layout.dialog_information, null)

    val dialog = AlertDialog.Builder(this)
        .setCancelable(false)
        .setView(loadingView)
        .create()
        .apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    val tvContent = loadingView.findViewById<TextView>(R.id.tv_content)
    val btnOk = loadingView.findViewById<LinearLayoutCompat>(R.id.ll_ok)

    tvContent.text = content
    btnOk.setOnClickListener {
        onOk?.invoke()
        dialog.dismiss()
    }

    dialog.show()
}

fun <T> T.toJson(): String = try {
    Gson().toJson(this)
} catch (e: Exception) {
    ""
}

fun <T> String.toObj(classType: Class<T>): T {
    return Gson().fromJson(this, classType)
}

fun Activity.pickDataDialog(adapter: EasyAdapter, onSearch: (() -> Unit), onAdd: (() -> Unit)) {
    val inflater = layoutInflater
    val loadingView = inflater.inflate(R.layout.dialog_find, null)

    val dialog = AlertDialog.Builder(this)
        .setCancelable(false)
        .setView(loadingView)
        .create()
        .apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

    val etSearch = loadingView.findViewById<EditText>(R.id.et_search)
    val ibSearch = loadingView.findViewById<ImageButton>(R.id.ib_search)
    val rvMain = loadingView.findViewById<RecyclerView>(R.id.rv_main)
    val cvAdd = loadingView.findViewById<CardView>(R.id.cv_add)
    val llCancel = loadingView.findViewById<LinearLayoutCompat>(R.id.ll_cancel)

    
    cvAdd.setOnClickListener {
        onAdd.invoke()
    }

    llCancel.setOnClickListener {
        dialog.dismiss()
    }


    dialog.show()
}

fun Context.colorDrawable(id: Int): Drawable {
    return ColorDrawable(colorFromId(id))
}

fun Fragment.colorFromId(id: Int): Int {
    return activity?.colorFromId(id) ?: 0
}

fun Context.colorFromId(colorId: Int): Int {
    return if (Build.VERSION.SDK_INT < 23) {
        ContextCompat.getColor(this, colorId)
    } else {
        resources.getColor(colorId, null)
    }
}

fun Double.toRupiah(): String {
    val localeID =  Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(this).toString()
}
fun Int.toRupiah(): String {
    val localeID =  Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(this).toString()
}
fun Long.toRupiah(): String {
    val localeID =  Locale("in", "ID")
    val numberFormat = NumberFormat.getCurrencyInstance(localeID)
    numberFormat.maximumFractionDigits = 0
    return numberFormat.format(this).toString()
}
fun EditText.doubleVal(): Double {
    return this.text.toString().toDouble()
}
fun EditText.doubleValOrNull(): Double? {
    return this.text.toString().toDoubleOrNull()
}
fun EditText.intVal(): Int {
    return this.text.toString().toInt()
}
fun EditText.intValOrNull(): Int? {
    return this.text.toString().toIntOrNull()
}
fun EditText.stringVal(): String {
    return this.text.toString()
}

fun AppCompatActivity.getCurrentFragment(id: Int): Fragment? {
    val navHostFragment: NavHostFragment? = supportFragmentManager.findFragmentById(id) as NavHostFragment?
    return navHostFragment?.childFragmentManager?.fragments?.get(0)
}


fun Intent?.getFilePath(context: Context): String {
    return this?.data?.let { data -> RealPathUtil.getRealPath(context, data) ?: "" } ?: ""
}

fun String.cleanCurrency(): String {
    return this.replace(("[^\\d.]").toRegex(), "").replace(".", "")
}

fun AlertDialog.visible(state: Boolean, dismissDialog: Boolean = false) {
    when {
        state -> this.show()
        dismissDialog -> this.dismiss()
        else -> this.hide()
    }
}