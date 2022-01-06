package com.mobiversa.ezy2pay.ui.receipt

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.dataModel.PrintReceiptRequestData
import com.mobiversa.ezy2pay.dataModel.PrintReceiptResponse
import com.mobiversa.ezy2pay.network.response.PrintReceiptResponseDataModel
import com.mobiversa.ezy2pay.utils.AppRepository

class PrintReceiptViewModel(val appRepository: AppRepository) : ViewModel() {
    private val service = PrintReceiptModel()
    var printReceiptData = MutableLiveData<PrintReceiptResponseDataModel>()

    fun getReceipt(receiptParam: HashMap<String, String>) {
       // Log.i(TAG, "getReceipt: $receiptParam")
        printReceiptData = service.getPrintReceipt(receiptParam)
    }

    suspend fun sendReceipt(requestDataPrint: PrintReceiptRequestData): PrintReceiptResponse {
        return appRepository.sendReceipt(requestDataPrint)
    }
}
