package com.rijal.salesandcredit.helpers

data class ActionState<T>(
    val data: T? = null,
    val message: String? = null,
    val isSuccess: Boolean = true,
    var isConsumed: Boolean = false,
    var isLoading: Boolean = false
) {
    fun read(callback: (() -> Unit)? = null) {
        if (!isConsumed) {
            isConsumed = true
            callback?.invoke()
        }
    }
}