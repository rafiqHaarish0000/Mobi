package com.mobiversa.ezy2pay.dataModel

import com.mobiversa.ezy2pay.utils.AppFunctions
import com.mobiversa.ezy2pay.utils.Constants


data class TransactionStatusRequestDataModel(
    val service: String = "MOTO_LINK_LIST",
    var tid: String,
    var pageNo: String = "1",
    var fromDate: String = "",
    var toDate: String = "",
    var searchKey: String = "",
    var linkTxnStatus: String = ""
)

data class ResponseTransactionStatusDataModel(
    val responseCode: String,
    val responseMessage: String,
    val responseDescription: String,
    val responseData: ResponseData
) {
    data class ResponseData(
        val motoTxndetails: ArrayList<TransactionStatusData>
    )
}

data class TransactionStatusData(
    val id: String,
    val reqDate: String,
    val mid: String,
    val tid: String,
    val name: String,
    val smsLink: String,
    val status: String,
    val timestamp: String,
    val amount: Long,
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
) {
    fun getConvertedAmount(): String {
        return AppFunctions.parseAmount(amount.toString())
    }

    fun getParsedDate(): String {
        return AppFunctions.parseDate(
            outputDatePattern = "dd-MM-yyyy | hh:mm aa",
            dateString = reqDate
        )
    }

    fun getMonthParsedDate(): String {
        return AppFunctions.parseDate(
            outputDatePattern = "MMMM yyyy",
            dateString = reqDate
        )
    }

    fun getParsedReference(): String {
        return String.format("#%s", reference)
    }

    fun getParsedStatus(): String {
        return when (status) {
            Constants.TransactionStatus.FAILURE -> {
                Constants.TransactionStatus.STRING_FAILURE
            }
            Constants.TransactionStatus.SUCCESS -> {
                Constants.TransactionStatus.STRING_SUCCESS
            }
            Constants.TransactionStatus.PENDING -> {
                Constants.TransactionStatus.STRING_PENDING
            }
            else -> {
                Constants.TransactionStatus.STING_NOT_USED
            }
        }
    }
}


data class TransactionStatusAdapterModal(
    val viewType: Int,
    var date: String? = null,
    var transactionData: TransactionStatusData? = null
) {
    fun getParsedDate(): String {
        return AppFunctions.parseDate(outputDatePattern = "MMM, yyyy", dateString = date!!)
    }
}

sealed class TransactionStatusResponse {
    data class Success(val data: ResponseTransactionStatusDataModel) : TransactionStatusResponse()
    data class Error(val errorMessage: String) : TransactionStatusResponse()
    data class Exception(val exceptionMessage: String) : TransactionStatusResponse()
}


// TODO: 07-11-2021


data class TransactionLinkDeleteRequestData(
    val service: String = "MOTO_LINK_DE_ACTIVE",
    val tid: String,
    val mid: String,
    val smsData: String
)

//{
//    "responseCode": "0000",
//    "responseMessage": "SUCCESS",
//    "responseDescription": "Link Deleted Successfully"
//}

data class TransactionLinkDeleteResponseData(
    val responseCode: String,
    val responseMessage: String,
    val responseDescription: String
)

sealed class TransactionLinkDeleteResponse {
    data class Success(val message: String) :
        TransactionLinkDeleteResponse()
    data class Error(val errorMessage: String) : TransactionLinkDeleteResponse()
    data class Exception(val exceptionMessage: String) : TransactionLinkDeleteResponse()
}
