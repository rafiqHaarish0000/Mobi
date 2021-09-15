package com.mobiversa.ezy2pay.utils

import android.util.Log
import com.mobiversa.ezy2pay.dataModel.NGrabPayRequestData
import com.mobiversa.ezy2pay.dataModel.NGrabPayResponse
import com.mobiversa.ezy2pay.dataModel.RequestTransactionStatusDataModel
import com.mobiversa.ezy2pay.dataModel.TransactionStatusResponse
import com.mobiversa.ezy2pay.network.ApiService
import java.io.IOException

internal val TAG = AppRepository::class.java.canonicalName

class AppRepository {
    private val apiResponse = ApiService.serviceRequest()

    companion object {
        fun getInstance() = AppRepository()
    }


    suspend fun voidNGPayTransaction(
        pathStr: String,
        requestData: NGrabPayRequestData
    ): NGrabPayResponse {


        val request =
            apiResponse.voidOnlineGrabPayTransaction(linkValue = pathStr, requestData = requestData)
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
        val request = apiResponse.getTransactionStatus(RequestTransactionStatusDataModel(tid = tid))
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

}