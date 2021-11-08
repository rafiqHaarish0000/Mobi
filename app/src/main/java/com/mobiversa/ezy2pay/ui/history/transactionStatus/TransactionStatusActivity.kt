package com.mobiversa.ezy2pay.ui.history.transactionStatus

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.transactionStatus.TransactionStatusAdapter
import com.mobiversa.ezy2pay.base.AppFunctions
import com.mobiversa.ezy2pay.dataModel.ResponseTransactionStatusDataModel
import com.mobiversa.ezy2pay.dataModel.TransactionStatusData
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

    private val transactionStatusInterface = object : TransactionStatusAdapter.TransactionStatusInterface{
        override fun onItemSelect(item: TransactionStatusData) {
            showDeleteDialog(item)
        }
    }

    private fun showDeleteDialog(item: TransactionStatusData) {
        val builder = AlertDialog.Builder(this@TransactionStatusActivity)
        builder.setTitle("Authentication Required")
        builder.setMessage("Are you sure do you want to delete this line #${item.id} | ${item.getConvertedAmount()}")
        builder.setPositiveButton("Delete") { dialog, which ->
            deleteTransactionLink(item)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    private fun deleteTransactionLink(item: TransactionStatusData) {
        lifecycleScope.launch {
//            val response = viewModel.deleteTransactionLink(item)
        }
    }

    private lateinit var tid: String

    private var searchString = ""
    private var transactionStatus = ""
    private lateinit var transactionStatusAdapter: TransactionStatusAdapter
    private lateinit var _binding: ActivityTransactionStatusBinding
    private val binding: ActivityTransactionStatusBinding get() = _binding
    private lateinit var viewModel: TransactionStatusViewModel


    private val searchCallback = object : SearchFilterDialog.SearchFilterCallBack {
        override fun onSearch(fromDate: Long, toDate: Long, status: String) {
            transactionStatus = status
            getTransactionStatusPaging(tid, searchString, status)
        }

        override fun onCancel() {

        }

        override fun onSearchWithTransaction(status: String) {
            transactionStatus = status
            getTransactionStatusPaging(tid, searchString, status)
        }
    }

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
        transactionStatusAdapter = TransactionStatusAdapter(callBack = transactionStatusInterface)


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
            this.tid = tid
            getTransactionStatusPaging(tid, "", transactionStatus)

        }

        binding.imageButtonClear.setOnClickListener {
            binding.editTextSearch.apply {
                text.clear()
                clearFocus()
            }
            AppFunctions.SoftKeyboard.closeSoftKeyboard(
                this.currentFocus,
                this@TransactionStatusActivity
            )
        }

        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    searchString = s.toString()
                    getTransactionStatusPaging(tid, s.toString(), transactionStatus)
                    binding.imageButtonClear.visibility = View.VISIBLE
                } else {
                    searchString = ""
                    getTransactionStatusPaging(tid, "", transactionStatus)
                    binding.imageButtonClear.visibility = View.GONE
                }
            }
        })
    }

    private fun handleError(loadState: CombinedLoadStates) {
        val errorState = loadState.source.append as? LoadState.Error
            ?: loadState.source.prepend as? LoadState.Error
            ?: loadState.mediator?.refresh as? LoadState.Error

        errorState?.let {
            Toast.makeText(this@TransactionStatusActivity, it.error.message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun getTransactionStatusPaging(
        tid: String,
        searchKey: String,
        status: String
    ) {
//        AppFunctions.Dialogs.showLoadingDialog("Loading...", this@TransactionStatusActivity)
        lifecycleScope.launch {
            viewModel.getTransactionStatusPaging(tid, "", "", searchKey, status).collectLatest {
//                AppFunctions.Dialogs.closeLoadingDialog()
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

    override fun onPause() {
        super.onPause()

        AppFunctions.SoftKeyboard.closeSoftKeyboard(
            this.currentFocus,
            this@TransactionStatusActivity
        )
    }
}