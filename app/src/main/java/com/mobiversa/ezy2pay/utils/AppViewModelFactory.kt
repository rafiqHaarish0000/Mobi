package com.mobiversa.ezy2pay.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobiversa.ezy2pay.ui.history.transactionHistory.TransactionHistoryViewModel
import com.mobiversa.ezy2pay.ui.history.transactionHistoryDetails.HistoryDetailViewModel
import com.mobiversa.ezy2pay.ui.history.transactionStatus.TransactionStatusViewModel

class AppViewModelFactory internal constructor(private val appRepository: AppRepository) :
    ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass.canonicalName) {
            HistoryDetailViewModel::class.java.canonicalName -> {
                HistoryDetailViewModel(appRepository) as T
            }
            TransactionHistoryViewModel::class.java.canonicalName -> {
                TransactionHistoryViewModel(appRepository) as T
            }
            TransactionStatusViewModel::class.java.canonicalName->{
                TransactionStatusViewModel(appRepository) as T
            }
            else -> {
                throw Exception("Provided view model does not match")
            }
        }
    }
}