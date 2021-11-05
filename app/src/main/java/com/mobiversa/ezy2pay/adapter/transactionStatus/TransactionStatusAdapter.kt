package com.mobiversa.ezy2pay.adapter.transactionStatus

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.dataModel.TransactionStatusData
import com.mobiversa.ezy2pay.databinding.RecyclerEzylinkTransactionListItemBinding
import com.mobiversa.ezy2pay.utils.Constants

class TransactionStatusAdapter :
    PagingDataAdapter<TransactionStatusData, TransactionStatusAdapter.TransactionStatusViewHolder>(
        DIFF_CALLBACK
    ) {
    inner class TransactionStatusViewHolder(val binding: RecyclerEzylinkTransactionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transactionStatusData: TransactionStatusData): RecyclerEzylinkTransactionListItemBinding {
            return binding.apply {
                dataModel = transactionStatusData
                executePendingBindings()
            }
        }
    }

    override fun onBindViewHolder(holder: TransactionStatusViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            val bind = (holder).bind(it)
            when (it.status) {
                Constants.TransactionStatus.FAILURE -> {
                    Glide
                        .with(bind.root.context)
                        .load(R.drawable.failed_transaction_40_px)
                        .into(bind.imageView)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.failed_transaction_40_px,
                                bind.root.context.theme
                            ),
                            null
                        )
                    } else {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.failed_transaction_40_px
                            ),
                            null
                        )
                    }
                }
                Constants.TransactionStatus.SUCCESS -> {
                    Glide
                        .with(bind.root.context)
                        .load(R.drawable.success_transaction_40_px)
                        .into(bind.imageView)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.success_transaction_40_px,
                                bind.root.context.theme
                            ),
                            null
                        )
                    } else {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.success_transaction_40_px
                            ),
                            null
                        )
                    }
                }
                Constants.TransactionStatus.PENDING -> {
                    Glide
                        .with(bind.root.context)
                        .load(R.drawable.pending_transaction_40_px)
                        .into(bind.imageView)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.pending_transaction_40_px,
                                bind.root.context.theme
                            ),
                            null
                        )
                    } else {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.pending_transaction_40_px
                            ),
                            null
                        )
                    }
                }
                else -> {
                    Glide
                        .with(bind.root.context)
                        .load(R.drawable.link_not_used_16_px)
                        .into(bind.imageView)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.link_not_used_16_px,
                                bind.root.context.theme
                            ),
                            null
                        )
                    } else {
                        bind.textViewStatusText.setCompoundDrawables(
                            null,
                            null,
                            bind.root.context.resources.getDrawable(
                                R.drawable.link_not_used_16_px
                            ),
                            null
                        )
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionStatusViewHolder {
        return TransactionStatusViewHolder(
            RecyclerEzylinkTransactionListItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }
}

val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionStatusData>() {
    override fun areItemsTheSame(
        oldItem: TransactionStatusData,
        newItem: TransactionStatusData
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: TransactionStatusData,
        newItem: TransactionStatusData
    ): Boolean {
        return oldItem.id == newItem.id
    }

}
