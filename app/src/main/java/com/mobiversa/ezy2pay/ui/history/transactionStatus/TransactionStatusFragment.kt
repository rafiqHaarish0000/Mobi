package com.mobiversa.ezy2pay.ui.history.transactionStatus

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.TransactionStatusAdapter
import com.mobiversa.ezy2pay.dataModel.ResponseTransactionStatusDataModel
import com.mobiversa.ezy2pay.dataModel.TransactionStatusResponse
import com.mobiversa.ezy2pay.databinding.TransactionStatusFragmentBinding
import com.mobiversa.ezy2pay.utils.AppRepository
import com.mobiversa.ezy2pay.utils.AppViewModelFactory
import com.mobiversa.ezy2pay.utils.Constants
import kotlinx.coroutines.launch

internal val TAG = TransactionStatusFragment::class.java.canonicalName

class TransactionStatusFragment : Fragment(R.layout.transaction_status_fragment) {


    private lateinit var transactionStatusAdapter: TransactionStatusAdapter
    private lateinit var _binding: TransactionStatusFragmentBinding
    private val binding: TransactionStatusFragmentBinding get() = _binding
    private lateinit var viewModel: TransactionStatusViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = TransactionStatusFragmentBinding.bind(view)
        setupViewModel()
        setHasOptionsMenu(true)

        arguments?.let {
            val tid = it.getString(Constants.NavigationKey.TID, "")
            Log.i(TAG, "onViewCreated: tid -> $tid")
            getTransactionStatus(tid)
        }

        transactionStatusAdapter = TransactionStatusAdapter()
        binding.recyclerTransactionStatusList.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = transactionStatusAdapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun getTransactionStatus(tid: String) {
        lifecycleScope.launch {

            try {
                when (val result = viewModel.getTransactionStatus(tid)) {
                    is TransactionStatusResponse.Success -> {
                        displayTransactionStatusList(result.data)
                    }

                    is TransactionStatusResponse.Error -> {
                        showErrorCondition(result.errorMessage)
                    }

                    is TransactionStatusResponse.Exception -> {
                        showErrorCondition(result.exceptionMessage)
                    }
                }
            } catch (e: Exception) {
                // exception
                showErrorCondition(e.message!!)
            }

        }
    }

    private fun showErrorCondition(errorMessage: String) {

    }

    private fun displayTransactionStatusList(data: ResponseTransactionStatusDataModel) {

        transactionStatusAdapter.updateDataSet(data.responseData.motoTxndetails)

    }

    private fun setupViewModel() {
        val viewModelFactory = AppViewModelFactory(
            AppRepository.getInstance()
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(TransactionStatusViewModel::class.java)
        binding.apply {
            transactionViewModel = viewModel
            lifecycleOwner = this@TransactionStatusFragment.viewLifecycleOwner
            executePendingBindings()
        }
    }


}