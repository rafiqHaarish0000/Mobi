package com.mobiversa.ezy2pay.dataModel

data class RequestTransactionStatusDataModel(
    val service: String = "MOTO_LINK_LIST",
    val tid: String
)

data class ResponseTransactionStatusDataModel(
    val responseCode: String,
    val responseMessage: String,
    val responseDescription: String,
    val responseData: ResponseData
) {
    data class ResponseData(
        val motoTxndetails: ArrayList<TransactionStatusData>
    ) {
        data class TransactionStatusData(
            val id: String,
            val reqDate: String,
            val mid: String,
            val tid: String,
            val name: String,
            val smsLink: String,
            val status: String,
            val timestamp: String,
            val amount: String,
            val reference: String,
            val email: String,
            val latitude: String,
            val longitude: String,
            val location: String,
            val reqMode: String,
            val reqType: String,
            val moto3DSAuth: String,
            val motoPreAuth: String,
            val multiOption: String,
            val splitMid: String,
            val splitTid: String
        )
    }
}

sealed class TransactionStatusResponse {
    data class Success(val data: ResponseTransactionStatusDataModel) : TransactionStatusResponse()
    data class Error(val errorMessage: String) : TransactionStatusResponse()
    data class Exception(val exceptionMessage: String) : TransactionStatusResponse()
}
