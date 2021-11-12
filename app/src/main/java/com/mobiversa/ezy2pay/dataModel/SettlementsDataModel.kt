package com.mobiversa.ezy2pay.dataModel

data class SettlementsDataRequestData(
    val service: String,
    val sessionId: String,
    val hostType: String,
    val merchantId: String,
    val tid: Any
) {
}

sealed class SettlementsResponse {
    data class Response(val message: String) : SettlementsResponse()
    data class Exception(val exceptionMessage: String) : SettlementsResponse()
}