package com.mobiversa.ezy2pay.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.dataModel.NGrabPayRequestData
import com.mobiversa.ezy2pay.dataModel.NGrabPayResponse
import com.mobiversa.ezy2pay.network.response.SuccessModel
import com.mobiversa.ezy2pay.network.response.TransactionHistoryModel
import com.mobiversa.ezy2pay.network.response.VoidHistoryModel
import com.mobiversa.ezy2pay.utils.AppRepository

class HistoryViewModel(val appRepository: AppRepository) : ViewModel() {

    private val service = HistoryModel()
    var transactionHistoryList = MediatorLiveData<TransactionHistoryModel>()
    var userVerification = MutableLiveData<SuccessModel>()
    var setVoidHistory = MutableLiveData<VoidHistoryModel>()
    var settlementData = MutableLiveData<SuccessModel>()
    var saleData = MutableLiveData<SuccessModel>()

    fun getTransactionHistory(historyParam: HashMap<String, String>) {
        transactionHistoryList = service.getTransactionHistory(historyParam)
    }

    fun getUserVerification(historyParam: HashMap<String, String>) {
        userVerification = service.getUserVerification(historyParam)
    }

    fun setVoidHistory(pathStr: String, historyParam: HashMap<String, String>) {
        setVoidHistory = service.setVoidHistory(pathStr, historyParam)
    }

    fun getSettlement(historyParam: HashMap<String, String>) {
        settlementData = service.getSettlement(historyParam)
    }

    fun setSale(saleParam: HashMap<String, String>) {
        saleData = service.setSale(saleParam)
    }

    suspend fun cancelPendingTransaction(pathStr: String, requestData: NGrabPayRequestData): NGrabPayResponse {
        return appRepository.voidNGPayTransaction(
            pathStr = pathStr,
            requestData = requestData
        )
    }


    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
}