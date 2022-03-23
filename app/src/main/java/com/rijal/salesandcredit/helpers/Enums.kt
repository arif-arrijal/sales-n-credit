package com.rijal.salesandcredit.helpers

enum class TransactionType {
    CASH,
    CREDIT,
    PAYMENT_ONCE
}

enum class InfoEnum {
    SUCCESS,
    INFO,
    WARNING,
    DANGER
}

enum class TransactionEnum {
    SALES,
    CAPITAL_INJECTION
}

enum class SearchEnum {
    PERSON,
    ITEM
}

class IntentExtra {
    companion object {
        const val ROUTE = "ROUTE"
        const val ARGS = "ARGS"
    }
}

enum class UploadImageMode {
    CAMERA, FROM_FILE
}