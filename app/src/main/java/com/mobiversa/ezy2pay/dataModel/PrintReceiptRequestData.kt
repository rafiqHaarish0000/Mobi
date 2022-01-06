package com.mobiversa.ezy2pay.dataModel

import com.mobiversa.ezy2pay.network.response.PrintReceiptResponseDataModel

data class PrintReceiptRequestData(
    val service: String,
    val username: String,
    val sessionId: String,
    val hostType: String,
    val trxId: String,
    var mobileNo: String = "",
    var email: String = "",
    var whatsApp: String = ""
)

sealed class PrintReceiptResponse {
    data class Success(val data: PrintReceiptResponseDataModel) : PrintReceiptResponse()
    data class Error(val errorMessage: String) : PrintReceiptResponse()
    data class Exception(val exceptionMessage: String) : PrintReceiptResponse()
}

