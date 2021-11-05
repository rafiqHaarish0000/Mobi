package com.mobiversa.ezy2pay.utils

import com.mobiversa.ezy2pay.dataModel.TransactionStatusData

interface AppInterface {

    interface TransactionStatus{
        fun onLongPress(item: TransactionStatusData)
    }
}