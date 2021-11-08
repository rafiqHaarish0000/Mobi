package com.mobiversa.ezy2pay.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.RecyclerLoadStateBinding

class RecyclerViewLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<TransactionHistoryLoadStateViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): TransactionHistoryLoadStateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TransactionHistoryLoadStateViewHolder(
            RecyclerLoadStateBinding.inflate(
                inflater,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: TransactionHistoryLoadStateViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState).apply {
            holder.binding.buttonRetry.setOnClickListener {
                retry.invoke()
            }
        }
    }


}

class TransactionHistoryLoadStateViewHolder(val binding: RecyclerLoadStateBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState): Any {

        binding.apply {
            executePendingBindings()
        }

        when (loadState) {
            is LoadState.Error -> {
                Toast.makeText(
                    binding.root.context,
                    loadState.error.message,
                    Toast.LENGTH_SHORT
                ).show()

                binding.buttonRetry.isVisible = true
            }
            else -> {
                binding.loading.isVisible = loadState is LoadState.Loading
                binding.loading.isGone = loadState is LoadState.NotLoading
                binding.buttonRetry.isVisible = loadState !is LoadState.Loading
            }
        }

        return binding
    }
}