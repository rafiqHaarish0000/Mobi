package com.mobiversa.ezy2pay.dataSource

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mobiversa.ezy2pay.dataModel.TransactionHistoryRequestData
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.ForSettlement
import com.mobiversa.ezy2pay.utils.Constants

internal val TAG = TransactionHistoryDataSource::class.java.canonicalName

class TransactionHistoryDataSource(
    private val requestData: TransactionHistoryRequestData,
    private val apiService: ApiService
) : PagingSource<Int, ForSettlement>() {

    override fun getRefreshKey(state: PagingState<Int, ForSettlement>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ForSettlement> {
        try {
            var nextPageNumber = params.key ?: 1
            requestData.pageNo = nextPageNumber.toString()

            val response = apiService.getTransactionHistoryNew(requestData)

            if (response.isSuccessful) {
                val data = response.body()!!
                return if (data.responseCode == Constants.Network.RESPONSE_SUCCESS) {
                    Log.e(TAG, "load: Success")
                    nextPageNumber++
                    LoadResult.Page(
                        data = data.responseData.forSettlement!!,
                        prevKey = null,
                        nextKey = nextPageNumber
                    )
                } else {
                    Log.e(TAG, "load: Failure")
                    LoadResult.Page(
                        data = emptyList(),
                        nextKey = null,
                        prevKey = null
                    )
                }
            } else {
                Log.e(TAG, "load: Unsuccessful")
                return LoadResult.Page(
                    data = emptyList(),
                    nextKey = null,
                    prevKey = null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoadResult.Error(e)
        }
    }

}