package com.mobiversa.ezy2pay.network.response

import java.io.Serializable

data class TransactionHistoryResponseData(
    val responseCode: String,
    val responseData: TransactionHistoryData,
    val responseDescription: String,
    val responseMessage: String
)

data class TransactionHistoryData(
    var forSettlement: List<ForSettlement>? = null,
    var preAuthorization: List<ForSettlement>? = null
)

data class ForSettlement(
    val additionAmount: String,
    val amount: String?,
    val currency: String?,
    val date: String,
    val hostType: String,
    val invoiceId: String?,
    val latitude: String,
    val longitude: String,
    val mid: String,
    val rrn: String?,
    val stan: String,
    val aidResponse: String,
    val cardType: String?,
    val status: String,
    val tid: String,
    val time: String,
    val txnId: String,
    val pan: String?,
    val txnType: String?,
    val mobiRef: String?
) : Serializable {
    fun getInvoiceIdText(): String {
        return "Ref: ${invoiceId ?: '-'}"
    }
}

data class PreAuthorization(
    val additionAmount: String,
    val amount: String,
    val currency: String?,
    val date: String,
    val hostType: String,
    val invoiceId: String,
    val latitude: String,
    val longitude: String,
    val mid: String,
    val rrn: String?,
    val stan: String,
    val aidResponse: String,
    val cardType: String?,
    val status: String,
    val tid: String,
    val time: String,
    val txnId: String,
    val txnType: String?
) : Serializable