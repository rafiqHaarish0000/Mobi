package com.mobiversa.ezy2pay.ui.history.transactionStatus

import androidx.lifecycle.ViewModel
import com.mobiversa.ezy2pay.dataModel.TransactionStatusResponse
import com.mobiversa.ezy2pay.utils.AppRepository

class TransactionStatusViewModel(private val appRepository: AppRepository) : ViewModel() {

    // TODO: Implement the ViewModel

    suspend fun getTransactionStatus(tid: String): TransactionStatusResponse {
        return appRepository.getTransactionStatus(tid)
    }
}