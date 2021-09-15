package com.mobiversa.ezy2pay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.dataModel.ResponseTransactionStatusDataModel
import com.mobiversa.ezy2pay.databinding.RecyclerEzylinkTransactionListItemBinding

class TransactionStatusAdapter :
    RecyclerView.Adapter<TransactionStatusAdapter.TransactionStatusViewHolder>() {

    private var dataSet: ArrayList<ResponseTransactionStatusDataModel.ResponseData.TransactionStatusData> =
        ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionStatusViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TransactionStatusViewHolder(
            RecyclerEzylinkTransactionListItemBinding.inflate(
                inflater, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionStatusViewHolder, position: Int) {
        val item = dataSet[position]
        item.run {
            holder.bind(this@run)
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun updateDataSet(data: ArrayList<ResponseTransactionStatusDataModel.ResponseData.TransactionStatusData>) {
        this.dataSet = data
        notifyDataSetChanged()
    }


    class TransactionStatusViewHolder(val binding: RecyclerEzylinkTransactionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transactionStatusData: ResponseTransactionStatusDataModel.ResponseData.TransactionStatusData): RecyclerEzylinkTransactionListItemBinding {
            return binding.apply {
                dataModel = transactionStatusData
                executePendingBindings()
            }
        }

    }
}

