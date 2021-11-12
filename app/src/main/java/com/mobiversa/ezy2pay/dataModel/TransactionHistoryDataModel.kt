package com.mobiversa.ezy2pay.dataModel

import com.mobiversa.ezy2pay.network.response.ForSettlement


data class TransactionHistoryRequestData(
    var trxType: String? = null,
    var hostType: String? = null,
    var merchantId: String? = null,
    var service: String = "TXN_HISTORY",
    var sessionId: String? = null,
    var username: String? = null,
    var type: String? = null,
    var tid: String? = null,
    var liteMid: String? = null,
    var pageNo: String = "1"
)


data class NGrabPayRequestData(
    val sessionId: String,
    val service: String,
    val partnerTxID: String,
    val description: String
) {
}

data class NGrabPayResponseData(
    val responseCode: String,
    val responseMessage: String,
    val responseDescription: String,
    val responseData: ResponseData

) {
    data class ResponseData(
        val msgID: String,
        val txID: String,
        val status: String,
        val description: String,
        val paymentMethod: String,
        val txStatus: String,
        val reason: String
    )
}

sealed class NGrabPayResponse {
    data class Success(val data: NGrabPayResponseData) : NGrabPayResponse()
    data class Error(val errorMessage: String) : NGrabPayResponse()
    data class Exception(val exceptionMessage: String) : NGrabPayResponse()
}


sealed class TransactionHistoryResponse {
//    data class Success(val data: TransactionHistoryData) : TransactionHistoryResponse()
    data class NoRowAvailable(val message: String) : TransactionHistoryResponse()
    data class Response(val data: ArrayList<ForSettlement>) : TransactionHistoryResponse()
    data class Error(val errorMessage: String) : TransactionHistoryResponse()
    data class Exception(val exceptionMessage: String) : TransactionHistoryResponse()
}