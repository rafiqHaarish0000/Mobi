package com.mobiversa.ezy2pay.adapter.transactionStatus

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.dataModel.TransactionStatusAdapterModal
import com.mobiversa.ezy2pay.dataModel.TransactionStatusData
import com.mobiversa.ezy2pay.databinding.RecyclerEzylinkTransactionDateItemBinding
import com.mobiversa.ezy2pay.databinding.RecyclerEzylinkTransactionListItemBinding
import com.mobiversa.ezy2pay.utils.Constants


//internal const val DATE_VIEW: Int = 0
//internal const val TRANSACTION_VIEW: Int = 1

class TransactionStatusAdapterBackup :
    RecyclerView.Adapter<TransactionStatusAdapterBackup.TransactionStatusViewHolder>() {


//    private var dataSet: ArrayList<TransactionStatusAdapterModal> =
//        ArrayList()

    private var dataSet: ArrayList<TransactionStatusData> =
        ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionStatusViewHolder {
        val inflater = LayoutInflater.from(parent.context)
/*        return when (viewType) {
            DATE_VIEW -> {
                TransactionDateViewHolder(
                    RecyclerEzylinkTransactionDateItemBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            TRANSACTION_VIEW -> {
                TransactionStatusViewHolder(
                    RecyclerEzylinkTransactionListItemBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> {
                throw Exception("No View Found")
            }
        }*/
        return TransactionStatusViewHolder(
            RecyclerEzylinkTransactionListItemBinding.inflate(
                inflater, parent, false
            )
        )
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: TransactionStatusViewHolder, position: Int) {
        val item = dataSet[position]
        item.run {
            val bind = (holder).bind(this@run)

            when (item.status) {
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
/*        when (getItemViewType(position)) {
            DATE_VIEW -> {
                val item = dataSet[position]
                item.run {
                    (holder as TransactionDateViewHolder).bind(this@run)
                }
            }
            TRANSACTION_VIEW -> {
                val item = dataSet[position].transactionData
                item?.run {
                    val bind = (holder as TransactionStatusViewHolder).bind(this@run)

                    when (item.status) {
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
                                .load(R.drawable.link_not_used_transaction_16_px)
                                .into(bind.imageView)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                bind.textViewStatusText.setCompoundDrawables(
                                    null,
                                    null,
                                    bind.root.context.resources.getDrawable(
                                        R.drawable.link_not_used_transaction_16_px,
                                        bind.root.context.theme
                                    ),
                                    null
                                )
                            } else {
                                bind.textViewStatusText.setCompoundDrawables(
                                    null,
                                    null,
                                    bind.root.context.resources.getDrawable(
                                        R.drawable.link_not_used_transaction_16_px
                                    ),
                                    null
                                )
                            }
                        }
                    }
                }
            }*/
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }


//    override fun getItemCount(): Int = dataSet.size


/*    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        return if (dataSet[position].viewType == 1) DATE_VIEW else TRANSACTION_VIEW
    }*/

    fun updateDataSet(data: ArrayList<TransactionStatusData>) {
        dataSet = data
        notifyDataSetChanged()
    }


    class TransactionStatusViewHolder(val binding: RecyclerEzylinkTransactionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transactionStatusData: TransactionStatusData): RecyclerEzylinkTransactionListItemBinding {
            return binding.apply {
                dataModel = transactionStatusData
                executePendingBindings()
            }
        }
    }

    class TransactionDateViewHolder(val binding: RecyclerEzylinkTransactionDateItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dateString: TransactionStatusAdapterModal) {
            binding.apply {
                dataModel = dateString
                executePendingBindings()
            }
        }
    }
}

