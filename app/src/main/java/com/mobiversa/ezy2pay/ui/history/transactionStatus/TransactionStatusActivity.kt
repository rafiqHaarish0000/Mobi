package com.mobiversa.ezy2pay.ui.history.transactionStatus

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.transactionStatus.TransactionStatusAdapter
import com.mobiversa.ezy2pay.base.AppFunctions
import com.mobiversa.ezy2pay.dataModel.ResponseTransactionStatusDataModel
import com.mobiversa.ezy2pay.dataModel.TransactionStatusResponse
import com.mobiversa.ezy2pay.databinding.ActivityTransactionStatusBinding
import com.mobiversa.ezy2pay.dialogs.SearchFilterDialog
import com.mobiversa.ezy2pay.utils.AppRepository
import com.mobiversa.ezy2pay.utils.AppViewModelFactory
import com.mobiversa.ezy2pay.utils.Constants
import kotlinx.android.synthetic.main.activity_transaction_status.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


internal val TAG = TransactionStatusActivity::class.java.canonicalName

class TransactionStatusActivity : AppCompatActivity() {

    private val searchCallback = object : SearchFilterDialog.SearchFilterCallBack {
        override fun onSearch() {

        }

        override fun onCancel() {

        }

    }
    private lateinit var transactionStatusAdapter: TransactionStatusAdapter
    private lateinit var _binding: ActivityTransactionStatusBinding
    private val binding: ActivityTransactionStatusBinding get() = _binding
    private lateinit var viewModel: TransactionStatusViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide();
        _binding = DataBindingUtil.setContentView(
            this@TransactionStatusActivity,
            R.layout.activity_transaction_status
        )
        setContentView(binding.root)
        setupViewModel()

//        setSupportActionBar(binding.toolbar)
        transactionStatusAdapter = TransactionStatusAdapter()

        binding.imageButtonBack.setOnClickListener {
            finish()
        }
        binding.recyclerTransactionStatusList.apply {
            layoutManager =
                LinearLayoutManager(
                    this@TransactionStatusActivity,
                    LinearLayoutManager.VERTICAL,
                    false
                )
            adapter = transactionStatusAdapter
        }

        binding.imageButtonFilter.setOnClickListener {
            val dialog = SearchFilterDialog.newInstance(searchCallback)
            dialog.isCancelable = false
            dialog.show(supportFragmentManager, SearchFilterDialog::class.java.canonicalName)
        }


        intent.getStringExtra(Constants.NavigationKey.TID)?.let { tid ->
            Log.i(TAG, "onViewCreated: tid -> $tid")
//            getTransactionStatus(tid)
            getTransactionStatusPaging(tid)
        }
    }

    private fun getTransactionStatusPaging(tid: String) {
        lifecycleScope.launch {
            viewModel.getTransactionStatusPaging(tid, "2021-11-05", "2021-10-15").collectLatest {
                transactionStatusAdapter.submitData(it)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    private fun getTransactionStatus(tid: String) {
        lifecycleScope.launch {
            try {
                AppFunctions.Dialogs.showLoadingDialog("Loading", this@TransactionStatusActivity)
                when (val result = viewModel.getTransactionStatus(tid)) {
                    is TransactionStatusResponse.Success -> {

                        if (result.data.responseData.motoTxndetails.isNotEmpty()) {
                            displayTransactionStatusList(result.data)
                        } else {
                            showErrorCondition("No Transaction Available")
                        }
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
            } finally {
                AppFunctions.Dialogs.closeLoadingDialog()
            }

        }
    }

    private fun showErrorCondition(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun displayTransactionStatusList(data: ResponseTransactionStatusDataModel) {
        val arrayList = data.responseData.motoTxndetails
//        transactionStatusAdapter.updateDataSet(arrayList)
    }

    private fun setupViewModel() {
        val viewModelFactory = AppViewModelFactory(
            AppRepository.getInstance()
        )
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(TransactionStatusViewModel::class.java)
        binding.apply {
            transactionViewModel = viewModel
            executePendingBindings()
        }
    }
}