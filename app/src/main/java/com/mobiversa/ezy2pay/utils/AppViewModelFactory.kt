package com.mobiversa.ezy2pay.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mobiversa.ezy2pay.ui.history.HistoryViewModel
import com.mobiversa.ezy2pay.ui.history.historyDetail.HistoryDetailViewModel

class AppViewModelFactory internal constructor(private val appRepository: AppRepository) :
    ViewModelProvider.Factory {


    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass.canonicalName) {
            HistoryDetailViewModel::class.java.canonicalName -> {
                HistoryDetailViewModel(appRepository) as T
            }
            HistoryViewModel::class.java.canonicalName -> {
                HistoryViewModel(appRepository) as T
            }
            else -> {
                throw Exception("Provided view model does not match")
            }
        }
    }
}