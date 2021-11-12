package com.mobiversa.ezy2pay.ui.history.transactionStatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mobiversa.ezy2pay.dataModel.*
import com.mobiversa.ezy2pay.dataSource.NetworkStatusDataSource
import com.mobiversa.ezy2pay.utils.AppRepository
import kotlinx.coroutines.flow.Flow

class TransactionStatusViewModel(private val appRepository: AppRepository) : ViewModel() {

    // TODO: Implement the ViewModel

    suspend fun getTransactionStatus(tid: String): TransactionStatusResponse {
        return appRepository.getTransactionStatus(tid)
    }


    fun getTransactionStatusPaging(
        transactionLinkDeleteRequestData: TransactionStatusRequestDataModel,
    ): Flow<PagingData<TransactionStatusData>> {
        val flow = Pager(
            PagingConfig(pageSize = 20)
        ) {
            NetworkStatusDataSource(
                appRepository.apiService,
                transactionLinkDeleteRequestData
            )
        }.flow
            .cachedIn(viewModelScope)
        return flow
    }

    suspend fun deleteTransactionLink(item: TransactionStatusData): TransactionLinkDeleteResponse {
       return appRepository.deleteTransactionLink(TransactionLinkDeleteRequestData(
            tid = item.tid,
            mid = item.mid,
            smsData = item.smsLink
        ))
    }
}