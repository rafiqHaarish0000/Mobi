package com.mobiversa.ezy2pay.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobiversa.ezy2pay.dataModel.TransactionStatusData
import com.mobiversa.ezy2pay.dataModel.TransactionStatusRequestDataModel
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.utils.AppExceptions
import com.mobiversa.ezy2pay.utils.Constants
import java.net.UnknownHostException

class NetworkStatusDataSource(
    val apiService: ApiService,
    private val transactionLinkDeleteRequestData: TransactionStatusRequestDataModel
) :
    PagingSource<Int, TransactionStatusData>() {
    override fun getRefreshKey(state: PagingState<Int, TransactionStatusData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, TransactionStatusData> {
        try {
            // Start refresh at page 1 if undefined.
            var nextPageNumber = params.key ?: 1

            transactionLinkDeleteRequestData.pageNo = nextPageNumber.toString()
            val response =
                apiService.getTransactionStatus(
                    transactionLinkDeleteRequestData
                )

            if (response.isSuccessful) {
                val data = response.body()!!

                when (data.responseCode) {
                    Constants.Network.RESPONSE_SUCCESS -> {
                        nextPageNumber++
                        return LoadResult.Page(
                            data = data.responseData.motoTxndetails,
                            prevKey = null, // Only paging forward.
                            nextKey = nextPageNumber
                        )

                    }
                    else -> {
                        return LoadResult.Page(
                            data = emptyList(),
                            prevKey = null, // Only paging forward.
                            nextKey = null
                        )
                    }
                }
            } else {
                return LoadResult.Page(
                    data = emptyList(),
                    prevKey = null, // Only paging forward.
                    nextKey = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(
                throwable = e
            )
        } catch (e: UnknownHostException) {
            return LoadResult.Error(
                throwable = AppExceptions.NoConnectivityException()
            )
        }
    }


}