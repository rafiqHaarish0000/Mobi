package com.mobiversa.ezy2pay.ui.receipt

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.PrintReceiptResponseDataModel
import retrofit2.Call
import retrofit2.Response

class PrintReceiptModel {
    private val deliverApiService = ApiService.serviceRequest()
    val printReceiptData = MutableLiveData<PrintReceiptResponseDataModel>()

    fun getPrintReceipt(keyParams: HashMap<String, String>): MutableLiveData<PrintReceiptResponseDataModel> {

        deliverApiService.run {
            getReceiptData(keyParams).enqueue(object : retrofit2.Callback<PrintReceiptResponseDataModel> {
                override fun onFailure(call: Call<PrintReceiptResponseDataModel>, t: Throwable) {
                    receiptFailedResponse(printReceiptData)
                }
                override fun onResponse(
                    call: Call<PrintReceiptResponseDataModel>,
                    response: Response<PrintReceiptResponseDataModel>
                ) {
                    if (response.isSuccessful) {
                        printReceiptData.postValue(response.body())
                    } else {
                        receiptFailedResponse(printReceiptData)
                    }
                }
            })
        }

        return printReceiptData
    }
    fun receiptFailedResponse(data: MutableLiveData<PrintReceiptResponseDataModel>) {
       // Log.e("PrintReceiptResponseDataModel ", "" + data)
    }
}