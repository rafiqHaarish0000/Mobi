package com.mobiversa.ezy2pay.dataModel

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