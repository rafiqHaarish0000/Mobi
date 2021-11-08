package com.mobiversa.ezy2pay.ui.history

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.SuccessModel
import com.mobiversa.ezy2pay.network.response.TransactionHistoryResponseData
import com.mobiversa.ezy2pay.network.response.VoidHistoryModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistoryModel {

    private val apiResponse = ApiService.serviceRequest()
    val data = MutableLiveData<TransactionHistoryResponseData>()
    val successData = MutableLiveData<SuccessModel>()
    val voidData = MutableLiveData<VoidHistoryModel>()
    val settlementData = MutableLiveData<SuccessModel>()
    val saleData = MutableLiveData<SuccessModel>()

    fun getTransactionHistory(postParam: HashMap<String, String>): MutableLiveData<TransactionHistoryResponseData> {

        apiResponse.getTransactionHistory(postParam).enqueue(object : Callback<TransactionHistoryResponseData> {
                override fun onFailure(call: Call<TransactionHistoryResponseData>, t: Throwable) {
                    notificationFailureResponse(data)
                }
                override fun onResponse(
                    call: Call<TransactionHistoryResponseData>,
                    response: Response<TransactionHistoryResponseData>
                ) {
                    if (response.isSuccessful) {
                        data.postValue(response.body())
                    }
                }
            })

        return data
    }
    fun getUserVerification(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        apiResponse.getUserValidation(postParam)
            .enqueue(object : Callback<SuccessModel> {
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    dataFailureResponse(successData)
                }
                override fun onResponse(
                    call: Call<SuccessModel>,
                    response: Response<SuccessModel>
                ) {
                    if (response.isSuccessful) {
                        successData.postValue(response.body())
                    }
                }
            })
        return successData
    }

    fun setVoidHistory(pathStr : String,postParam: HashMap<String, String>): MutableLiveData<VoidHistoryModel> {
        apiResponse.setVoidTransaction(pathStr,postParam)
            .enqueue(object : Callback<VoidHistoryModel> {
                override fun onFailure(call: Call<VoidHistoryModel>, t: Throwable) {
                    voidFailureResponse(voidData)
                }
                override fun onResponse(
                    call: Call<VoidHistoryModel>,
                    response: Response<VoidHistoryModel>
                ) {
                    if (response.isSuccessful) {
                        voidData.postValue(response.body())
                    }
                }
            })
        return voidData
    }

    fun getSettlement(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        apiResponse.getSettlement(postParam)
            .enqueue(object : Callback<SuccessModel> {
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    dataFailureResponse(settlementData)
                }
                override fun onResponse(
                    call: Call<SuccessModel>,
                    response: Response<SuccessModel>
                ) {
                    if (response.isSuccessful) {
                        settlementData.postValue(response.body())
                    }
                }
            })
        return settlementData
    }

    fun setSale(postParam: HashMap<String, String>): MutableLiveData<SuccessModel> {
        apiResponse.setSale(postParam)
            .enqueue(object : Callback<SuccessModel> {
                override fun onFailure(call: Call<SuccessModel>, t: Throwable) {
                    dataFailureResponse(saleData)
                }
                override fun onResponse(
                    call: Call<SuccessModel>,
                    response: Response<SuccessModel>
                ) {
                    if (response.isSuccessful) {
                        saleData.postValue(response.body())
                    }
                }
            })
        return saleData
    }

    private fun notificationFailureResponse(data: MutableLiveData<TransactionHistoryResponseData>) {
        Log.e("Transaction History ", "" + data)
    }
    private fun dataFailureResponse(data: MutableLiveData<SuccessModel>) {
        Log.e("Transaction History ", "" + data)
    }
    private fun voidFailureResponse(data: MutableLiveData<VoidHistoryModel>) {
        Log.e("Transaction History ", "" + data)
    }
}

