package com.mobiversa.ezy2pay.utils

import android.util.Log
import com.mobiversa.ezy2pay.dataModel.*
import com.mobiversa.ezy2pay.network.ApiService
import java.io.IOException

internal val TAG = AppRepository::class.java.canonicalName

class AppRepository {
    val apiService = ApiService.serviceRequest()

    companion object {
        fun getInstance() = AppRepository()
    }


    suspend fun voidNGPayTransaction(
        pathStr: String,
        requestData: NGrabPayRequestData
    ): NGrabPayResponse {


        val request =
            apiService.voidOnlineGrabPayTransaction(linkValue = pathStr, requestData = requestData)
        try {
            // on api call success
            if (request.isSuccessful) {
                val data = request.body()
                data?.let {
                    when (it.responseCode) {

                        // on api call with response data
                        Constants.Network.RESPONSE_SUCCESS -> {
                            Log.i(TAG, "voidNGPayTransaction: Response Success")
                            return NGrabPayResponse.Success(data = it)
                        }

                        // on api call with no response data
                        Constants.Network.RESPONSE_FAILURE -> {
                            Log.i(TAG, "voidNGPayTransaction: Response Error")
                            return NGrabPayResponse.Error(errorMessage = it.responseDescription)
                        }

                        // default generic error condition
                        else -> {
                            Log.i(TAG, "voidNGPayTransaction: Response Else")
                            return NGrabPayResponse.Error(errorMessage = "Something went wrong")
                        }
                    }
                }
            }

            // default error condition
            return NGrabPayResponse.Error(errorMessage = "Something went wrong")
        } catch (e: IOException) {
            return NGrabPayResponse.Exception(exceptionMessage = e.message!!)
        }


    }

    suspend fun getTransactionStatus(tid: String): TransactionStatusResponse {
        val request = apiService.getTransactionStatus(TransactionStatusRequestDataModel(tid = tid))
        if (request.isSuccessful) {
            val data = request.body()
            data?.let {
                when (it.responseCode) {
                    Constants.Network.RESPONSE_SUCCESS -> {
                        Log.i(TAG, "voidNGPayTransaction: Response Success")
                        return TransactionStatusResponse.Success(data = it)
                    }

                    // on api call with no response data
                    Constants.Network.RESPONSE_FAILURE -> {
                        Log.i(TAG, "voidNGPayTransaction: Response Error")
                        return TransactionStatusResponse.Error(errorMessage = it.responseDescription)
                    }

                    // default generic error condition
                    else -> {
                        Log.i(TAG, "voidNGPayTransaction: Response Else")
                        return TransactionStatusResponse.Error(errorMessage = "Something went wrong")
                    }
                }
            }
        }

        return TransactionStatusResponse.Error(errorMessage = "Something went wrong")
    }

    suspend fun getTransactionHistoryData(transactionHistoryRequestData: TransactionHistoryRequestData): TransactionHistoryResponse {
        try {
            val response = apiService.getTransactionHistoryNew(transactionHistoryRequestData)
            return if (response.isSuccessful) {
                val data = response.body()!!
                if (data.responseCode.equals(
                        Constants.Network.RESPONSE_SUCCESS,
                        ignoreCase = true
                    )
                ) {
                    TransactionHistoryResponse.Success(data = data.responseData)
                } else {
                    TransactionHistoryResponse.Error(errorMessage = data.responseDescription)
                }
            } else {
                TransactionHistoryResponse.Error(errorMessage = response.message())
            }

        } catch (e: Exception) {
            return TransactionHistoryResponse.Exception(exceptionMessage = e.message!!)
        } catch (e: IOException) {
            return TransactionHistoryResponse.Exception(exceptionMessage = e.message!!)
        }
    }

    fun deleteTransactionLink(transactionLinkDeleteRequestData: TransactionLinkDeleteRequestData) {

    }

}