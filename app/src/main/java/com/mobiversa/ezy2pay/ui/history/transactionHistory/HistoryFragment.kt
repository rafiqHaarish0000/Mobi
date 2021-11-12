package com.mobiversa.ezy2pay.ui.history.transactionHistory

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.adapter.transactionHistory.TransactionHistoryAdapter
import com.mobiversa.ezy2pay.base.AppFunctions
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.dataModel.*
import com.mobiversa.ezy2pay.network.ApiService
import com.mobiversa.ezy2pay.network.response.ForSettlement
import com.mobiversa.ezy2pay.network.response.TransactionHistoryData
import com.mobiversa.ezy2pay.network.response.TransactionHistoryResponseData
import com.mobiversa.ezy2pay.network.response.VoidHistoryModel
import com.mobiversa.ezy2pay.ui.history.SwipeToVoidCallback
import com.mobiversa.ezy2pay.ui.history.transactionHistoryDetails.HistoryDetailFragment
import com.mobiversa.ezy2pay.ui.history.transactionStatus.TransactionStatusActivity
import com.mobiversa.ezy2pay.utils.*
import com.mobiversa.ezy2pay.utils.Constants.Companion.UserName
import com.mobiversa.ezy2pay.utils.Fields.Companion.PREAUTH
import com.mobiversa.ezy2pay.utils.PreferenceHelper.get
import de.adorsys.android.finger.Finger
import de.adorsys.android.finger.FingerListener
import kotlinx.android.synthetic.main.activity_show_notification_acknowledgement.*
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*
import kotlinx.android.synthetic.main.recycler_ezylink_transaction_list_item.view.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.Executor
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

internal val TAG = HistoryFragment::class.java.canonicalName

class HistoryFragment : BaseFragment(), View.OnClickListener, FingerListener {


    private var loadMoreTransactionHistoryRequestData: TransactionHistoryRequestData? = null
    private var currentTransactionHistoryPageNumber = 1

    private var serviceType = 1 // 1 is Transaction History and 2 is Pre Auth Transaction

    private var position: Int? = null

    private var selectedProductFilter = ""
    private lateinit var historyViewModel: HistoryViewModel
    var transactionType = Fields.ALL
    private lateinit var imageButtonProductFilter: ImageButton
    private lateinit var trxTypeAdapter: ArrayAdapter<String>
    private var historyList = ArrayList<ForSettlement>()

    private lateinit var transactionHistoryAdapter: TransactionHistoryAdapter
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historySearch: SearchView
    private lateinit var btnSettlementHistory: RelativeLayout
    private lateinit var fabImage: ImageView
    private lateinit var historyData: ForSettlement
    private var historyType: String = ""

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // TODO: 20-09-2021  
    /* Vignesh Selvam
    *
    * Transactions Status Button added
    *
    * Display transaction button for EzyMoto
    *
    * */
    private lateinit var fabTransactionStatus: FloatingActionButton


    val requestData = HashMap<String, String>()
    private lateinit var customPrefs: SharedPreferences


    private val transactionHistoryRecyclerViewOnScrollListener =
        object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && serviceType != 2) {
                    loadMoreTransactionHistory()
                }
            }
        }

    private val transactionHistoryCallback =
        object : TransactionHistoryAdapter.TransactionHistoryInterface {
            override fun onTransactionClicked(historyData: ForSettlement, bundle: Bundle) {
                bundle.putSerializable(Constants.NavigationKey.TRANSACTION_HISTORY_KEY, historyData)
                findNavController().navigate(
                    R.id.action_navigation_history_to_historyDetailFragment2,
                    bundle
                )
            }
        }

    private val biometricPromptCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)
            Toast.makeText(
                requireContext(),
                "Authentication Failed", Toast.LENGTH_SHORT
            )
                .show()
        }

        override fun onAuthenticationSucceeded(
            result: BiometricPrompt.AuthenticationResult
        ) {
            super.onAuthenticationSucceeded(result)
            Toast.makeText(
                requireContext(),
                "Authentication succeeded!", Toast.LENGTH_SHORT
            )
                .show()

            requestData.clear()
            requestData[Fields.biomerticKey] = Fields.Success
            requestData[Fields.username] = getSharedString(UserName)
            jsonVoidTransaction("Void", historyData, requestData)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Toast.makeText(
                requireContext(), "Authentication Failed",
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun loadMoreTransactionHistory() {
        loadMoreTransactionHistoryRequestData?.let {
            currentTransactionHistoryPageNumber++
            requestTransactionHistoryData(it)
        }
    }


    // Fragment Navigation
    private val historyDetailFragment = HistoryDetailFragment()
    val bundle = Bundle()
    private lateinit var finger: Finger

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProvider(
                this@HistoryFragment,
                AppViewModelFactory(AppRepository.getInstance())
            ).get(HistoryViewModel::class.java)
        val rootView = inflater.inflate(R.layout.fragment_history, container, false)

        initialize(rootView)
//        val textView: TextView = root.findViewById(R.id.text_dashboard)
//        historyViewModel.text.observe(this, Observer {
//            textView.text = it
//        })

        showLog("TId ", getLoginResponse().tid)
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun initialize(rootView: View) {
        setTitle("Transactions", true)
        (activity as MainActivity).supportActionBar?.show()

        customPrefs = PreferenceHelper.customPrefs(requireContext(), "REMEMBER")

        checkAndRequestPermissions()

        imageButtonProductFilter = rootView.image_button_filter_product
        historyRecyclerView = rootView.recycler_view_history_list
        historySearch = rootView.history_search
        btnSettlementHistory = rootView.button_settlement_history
        fabImage = rootView.image_view_fab

        // TODO: 20-09-2021
        /*  Vignesh Selvam
        *
        * fabTransactionStatus button added for ezymoto transaction status
        * */
        fabTransactionStatus = rootView.floating_action_button_transaction_status

        btnSettlementHistory.setOnClickListener(this)
        fabTransactionStatus.setOnClickListener(this)
        imageButtonProductFilter.setOnClickListener(this)

        btnSettlementHistory.visibility = View.GONE
        fabTransactionStatus.visibility = View.GONE

        finger = Finger(this.requireContext())
        searchView()

        transactionHistoryAdapter = TransactionHistoryAdapter(
            requireContext(),
            callback = transactionHistoryCallback
        )
        historyRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.xxhdpi_10).toInt()
                )
            )
            adapter = transactionHistoryAdapter
            addOnScrollListener(transactionHistoryRecyclerViewOnScrollListener)
        }

        enableVoidOption()

//        trxTypeAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, getTrxList())
//        imageButtonProductFilter.adapter = trxTypeAdapter
//        imageButtonProductFilter.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parentView: AdapterView<*>?,
//                    selectedItemView: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    showLog("Selected", getTrxList()[position])
//                    // To make text clear
//                    (parentView?.getChildAt(0) as TextView?)?.setTextColor(0xFFFFFF)
//                    transactionType = when (getTrxList()[position]) {
//                        "All Transaction" -> {
//                            Fields.ALL
//                        }
//                        Constants.EzyMoto, Constants.EzySplit -> "MOTO"
//                        Fields.EZYWIRE -> Fields.CARD
//                        else -> getTrxList()[position]
//                    }
//                    if (getTrxList()[position].equals(PREAUTH, ignoreCase = true)) {
//                        if (getProductList()[1].isEnable)
//                            preAuthTransHistory(Fields.CARD)
//                        else
//                            preAuthTransHistory(Fields.EZYMOTO)
//                    } else {
////                    reset page number to load from first
//                        transactionHistoryAdapter.clearDataset()
//                        currentTransactionHistoryPageNumber = 1
//                        transactionHistory("Initial")
//                    }
//                }
//
//                override fun onNothingSelected(parentView: AdapterView<*>?) {
//                    // your code here
//                }
//            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, biometricPromptCallback)
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authentication required")
            .setSubtitle("Authenticate void using your biometric credential")
            .setNegativeButtonText("Cancel")
            .build()


//        historyViewModel.transactionHistoryList.removeObserver(dataObserver)
//        historyViewModel.transactionHistoryList.observe(viewLifecycleOwner, dataObserver)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).supportActionBar?.show()
        setTitle("Transactions", true)

//        transactionType = Fields.ALL
        currentTransactionHistoryPageNumber = 1
        transactionHistory("On Resume")
    }

    private fun transactionHistory(s: String) {
        Log.i(TAG, "transactionHistory: $s")
        serviceType = 1
//        val historyParam = HashMap<String, String>()
//
//        historyParam[Fields.username] = customPrefs[UserName]!!
//        historyParam[Fields.sessionId] = getLoginResponse().sessionId
//        historyParam[Fields.MerchantId] = getLoginResponse().merchantId
//        historyParam[Fields.HostType] = getLoginResponse().hostType
//        historyParam[Fields.TRX_TYPE] = transactionType
//
//        if (transactionType.equals(Fields.GRABPAY, ignoreCase = true)) {
//            historyParam[Fields.tid] = getLoginResponse().gpayTid
//            historyParam[Fields.Service] = Fields.TRX_HISTORY
//        } else {
//            historyParam[Fields.Service] = Fields.TRX_HISTORY
//            historyParam[Fields.tid] = getTidValue()
//            /*when {
//                getLoginResponse().tid.isNotEmpty() -> {
//                    historyParam[Fields.tid] = getLoginResponse().tid
//                }
//                getLoginResponse().motoTid.isNotEmpty() -> {
//                    historyParam[Fields.tid] = getLoginResponse().motoTid
//                }
//                getLoginResponse().ezypassTid.isNotEmpty() -> {
//                    historyParam[Fields.tid] = getLoginResponse().ezypassTid
//                }
//                getLoginResponse().ezyrecTid.isNotEmpty() -> {
//                    historyParam[Fields.tid] = getLoginResponse().ezyrecTid
//                }
//            }*/
//        }
//
//        if (!getLoginResponse().type.equals(Constants.Normal, true)) {
//            historyParam[Fields.liteMid] = getLoginResponse().liteMid
//            historyParam[Fields.Service] = Fields.LITE_TXN_HISTORY
//        }
//
//        historyParam[Fields.Type] = getLoginResponse().type.uppercase(Locale.ROOT)

        val transactionHistoryRequestData = TransactionHistoryRequestData(
            username = customPrefs[UserName]!!,
            sessionId = getLoginResponse().sessionId,
            merchantId = getLoginResponse().merchantId,
            hostType = getLoginResponse().hostType,
            trxType = transactionType,
            service = Fields.TRX_HISTORY,
            type = getLoginResponse().type.uppercase(Locale.ROOT)
        )

//        transactionHistoryRequestData.tid =
//            if (transactionType.equals(Fields.GRABPAY, ignoreCase = true)) {
//                getLoginResponse().gpayTid
//            } else {
//                getTidValue()
//            }

        transactionHistoryRequestData.tid = getTidByTransactionType(transactionType)


        if (!getLoginResponse().type.equals(Constants.Normal, true)) {
            transactionHistoryRequestData.liteMid = getLoginResponse().liteMid
            transactionHistoryRequestData.service = Fields.LITE_TXN_HISTORY
        }

        loadMoreTransactionHistoryRequestData = transactionHistoryRequestData

        transactionHistoryAdapter.setServiceType(Fields.TRX_HISTORY)
        requestTransactionHistoryData(transactionHistoryRequestData)

//        requestTransactionHistoryData(historyParam)
    }

    private fun preAuthTransHistory(historyType: String) {
        serviceType = 2
        Log.i(TAG, "preAuthTransHistory: historyType $historyType")
        transactionType = historyType

//        val historyParam = HashMap<String, String>()
//
//        historyParam[Fields.sessionId] = getLoginResponse().sessionId
//        historyParam[Fields.MerchantId] = getLoginResponse().merchantId
//        historyParam[Fields.HostType] = getLoginResponse().hostType
//        historyParam[Fields.TRX_TYPE] = transactionType
//        historyParam[Fields.Service] = Fields.PERAUTHHIST
//
//        if (historyType.equals(Fields.EZYMOTO, true)) {
//            historyParam[Fields.tid] = getLoginResponse().motoTid
//        } else {
//            historyParam[Fields.tid] = getLoginResponse().tid
//        }

        val transactionHistoryRequestData = TransactionHistoryRequestData(
            sessionId = getLoginResponse().sessionId,
            merchantId = getLoginResponse().merchantId,
            hostType = getLoginResponse().hostType,
            trxType = transactionType,
            service = Fields.PERAUTHHIST,
        )
//        transactionHistoryRequestData.tid = if (historyType.equals(Fields.EZYMOTO, true)) {
//            getLoginResponse().motoTid
//        } else {
//            getLoginResponse().tid
//        }
        transactionHistoryRequestData.tid = getTidByTransactionType(transactionType)
        loadMoreTransactionHistoryRequestData = transactionHistoryRequestData

        transactionHistoryAdapter.setServiceType(Fields.PERAUTHHIST)
        requestTransactionHistoryData(transactionHistoryRequestData)

//        requestTransactionHistoryData(historyParam)
    }

    private fun getTidByTransactionType(transactionType: String): String {
        return when (transactionType) {

            Fields.CARD -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }
                    else -> {
                        ""
                    }
                }
            }
            Fields.CASH -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }
                    getLoginResponse().ezypassTid.isNotEmpty() -> {
                        getLoginResponse().ezypassTid
                    }
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }
                    else -> {
                        ""
                    }
                }
            }
            Fields.EZYWIRE -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }
                    else -> {
                        ""
                    }
                }
            }
            Fields.PREAUTH -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.EZYMOTO -> {
                when {
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }
                    else -> {
                        ""
                    }
                }
            }
            Fields.ALL -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }

                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }

                    getLoginResponse().ezypassTid.isNotEmpty() -> {
                        getLoginResponse().ezypassTid
                    }

                    getLoginResponse().ezyrecTid.isNotEmpty() -> {
                        getLoginResponse().ezyrecTid
                    }

                    getLoginResponse().ezysplitTid.isNotEmpty() -> {
                        getLoginResponse().ezysplitTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.BOOST -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }

                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }

                    getLoginResponse().ezypassTid.isNotEmpty() -> {
                        getLoginResponse().ezypassTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.Moto -> {
                when {
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }

                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.EZYPASS -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().ezypassTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.EZYREC -> {
                when {
                    getLoginResponse().tid.isNotEmpty() -> {
                        getLoginResponse().tid
                    }

                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.GRABPAY -> {
                when {
                    getLoginResponse().gpayTid.isNotEmpty() -> {
                        getLoginResponse().gpayTid
                    }

                    getLoginResponse().gpayOnlineTid.isNotEmpty() -> {
                        getLoginResponse().gpayOnlineTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.FPX -> {
                when {
                    getLoginResponse().motoTid.isNotEmpty() -> {
                        getLoginResponse().motoTid
                    }
                    else -> {
                        ""
                    }
                }
            }

            Fields.EZYSPLIT -> {
                when {
                    getLoginResponse().ezysplitTid.isNotEmpty() -> {
                        getLoginResponse().ezysplitTid
                    }
                    else -> {
                        ""
                    }
                }
            }
            else -> {
                ""
            }
        }
    }

    private fun requestTransactionHistoryData(transactionHistoryRequestData: TransactionHistoryRequestData) {


        lifecycleScope.launch {

            if (currentTransactionHistoryPageNumber == 1) {
                Log.i(TAG, "requestTransactionHistoryData: show loading")
                AppFunctions.Dialogs.showLoadingDialog("Loading History...", requireContext())
            }

            transactionHistoryRequestData.pageNo = currentTransactionHistoryPageNumber.toString()

            when (val response =
                historyViewModel.getTransactionHistoryData(transactionHistoryRequestData)) {
                is TransactionHistoryResponse.Response -> {
                    historyList = response.data
                    listTransactionHistoryData(response.data)
                }
                is TransactionHistoryResponse.NoRowAvailable -> {
                    if (historyList.isEmpty()) {
                        textview_state_message.text = response.message
                        textview_state_message.isVisible = true
//                        shortToast(response.message)
                    }
                    btnSettlementVisibility(transactionType)
                }
                is TransactionHistoryResponse.Error -> {
                    showErrorMessage(response.errorMessage)
                }
                is TransactionHistoryResponse.Exception -> {
                    showExceptionMessage(response.exceptionMessage)
                }
            }
            AppFunctions.Dialogs.closeLoadingDialog()
        }
    }

    private fun showSettlementsButton() {
        when (transactionType) {
            Fields.ALL -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
            Fields.CARD -> {
//                if (completedCount > 0) {
//                    btnSettlementHistory.visibility = View.VISIBLE
//                } else {
//                    btnSettlementHistory.visibility = View.GONE
//                }
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
//            Fields.Moto -> {
//                Log.i(TAG, "btnSettlementVisibility: trxType -> $trxType")
//                btnSettlementHistory.visibility = View.VISIBLE
//                Glide
//                    .with(requireContext())
//                    .load(R.drawable.ezylink_meter_with_bg_60_px)
//                    .circleCrop()
//                    .into(fabImage)
//
//            }

            Fields.Moto, Fields.EZYREC, Fields.EZYSPLIT, Fields.EZYPASS -> {
                if (transactionType == Fields.Moto) {
                    fabTransactionStatus.visibility = View.VISIBLE
                } else {
                    fabTransactionStatus.visibility = View.GONE
                }

                if (!getLoginResponse().hostType.equals("P", true)) {
                    btnSettlementHistory.visibility = View.GONE
                }
            }

            Fields.BOOST, Fields.GRABPAY, Fields.CASH, PREAUTH -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
            else -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
        }
    }

    private fun showExceptionMessage(response: String) {
        if (currentTransactionHistoryPageNumber == 0) {
            textview_state_message.text = response
            textview_state_message.isVisible = true
//            shortToast(response)
        }

    }

    private fun showErrorMessage(response: String) {
        if (currentTransactionHistoryPageNumber == 0) {
            textview_state_message.text = response
            textview_state_message.isVisible = true
//            shortToast(response)
        }
    }

    @Deprecated("Will be removed in future update")
    private fun historyObserveData(it: TransactionHistoryResponseData?) {

        Log.e(TAG, "historyObserveData: trxType --> $transactionType")
        closeLoadingDialog()
        if (it != null) {
            var count = 0
            var completedCount = 0
            closeLoadingDialog()
            if (it.responseCode.equals(Constants.Network.RESPONSE_SUCCESS, true)) {
                showLog("Auth", transactionType)

                Log.i(TAG, "historyObserveData: before filter -> $it")

                val forSettlementsFilteredData = it.responseData.forSettlement?.filter {
                    !it.status.equals(
                        "PENDING",
                        ignoreCase = true
                    )
                }
                val preAuthFilteredData = it.responseData.preAuthorization?.filter {
                    !it.status.equals(
                        "PENDING",
                        ignoreCase = true
                    )
                }

                it.responseData.forSettlement = forSettlementsFilteredData
                it.responseData.preAuthorization = preAuthFilteredData

                Log.i(TAG, "historyObserveData: after filter $it")

                if (transactionType.equals(PREAUTH, true)) {
                    val authSize = it.responseData.preAuthorization?.size ?: 0
                    if (authSize > 0) {
                        historyList.clear()
                        it.responseData.preAuthorization?.let { it1 -> historyList.addAll(it1) }
                        transactionHistoryAdapter.notifyDataSetChanged()
                        count = 0

                        for (historyData in historyList) {
                            showLog("History", historyData.status)
                            if (historyData.status.equals("PENDING", false)) {
                                count++
                            }
                            if (historyData.status.equals("E", false)) {
                                completedCount++
                            }
                        }

                        showLog("History", "" + completedCount)
                    } else {
                        historyList.clear()
                        transactionHistoryAdapter.notifyDataSetChanged()
                        shortToast("No Records Found")
                    }
                } else {
                    if (it.responseData.forSettlement != null) {
                        if (it.responseData.forSettlement!!.isNotEmpty()) {
                            historyList.clear()
                            historyList.addAll(it.responseData.forSettlement!!)
                            transactionHistoryAdapter.notifyDataSetChanged()

                            for (historyData in historyList) {
                                if (historyData.status.equals("PENDING", false)) {
                                    count++
                                }
                                if (historyData.status.equals("COMPLETED", false)) {
                                    completedCount++
                                }
                            }

                            showLog("History", "" + completedCount)
                        } else {
                            historyList.clear()
                            transactionHistoryAdapter.notifyDataSetChanged()
                            shortToast("No Records Found")
                        }
                    }
                }

//                btnSettlementVisibility(transactionType, count, completedCount, historyData)
                textview_state_message.isVisible = false


            } else {
                textview_state_message.text = it.responseDescription
                textview_state_message.isVisible = true
//                shortToast(it.responseDescription)
            }
        }
    }

    @Deprecated("Will be removed in future update")
    private fun showTransactionList(response: TransactionHistoryData) {
        textview_state_message?.apply {
            isVisible = false
        }
        response.let {

            var count = 0
            var completedCount = 0

            showLog("Auth", transactionType)

            Log.i(TAG, "historyObserveData: before filter -> $it")

            val forSettlementsFilteredData = it.forSettlement?.filter { forSettlement ->
                !forSettlement.status.equals(
                    "PENDING",
                    ignoreCase = true
                )
            }

            val preAuthFilteredData = it.preAuthorization?.filter { preAuthorization ->
                !preAuthorization.status.equals(
                    "PENDING",
                    ignoreCase = true
                )
            }

            it.forSettlement = forSettlementsFilteredData
            it.preAuthorization = preAuthFilteredData

            Log.i(TAG, "historyObserveData: after filter $it")

            if (transactionType.equals(PREAUTH, true)) {
                val authSize = it.preAuthorization?.size ?: 0
                if (authSize > 0) {
                    historyList.clear()
                    it.preAuthorization?.let { it1 -> historyList.addAll(it1) }
//                    transactionHistoryAdapter.updateDataset(it.preAuthorization, 2)
                    count = 0

                    for (historyData in historyList) {
                        showLog("History", historyData.status)
                        if (historyData.status.equals("PENDING", false)) {
                            count++
                        }
                        if (historyData.status.equals("E", false)) {
                            completedCount++
                        }
                    }

                    showLog("History", "" + completedCount)
                } else {
                    historyList.clear()
//                    transactionHistoryAdapter.notifyDataSetChanged()
                    shortToast("No Records Found")
                }
            } else {
                if (it.forSettlement != null) {
                    if (it.forSettlement!!.isNotEmpty()) {
                        historyList.clear()
                        historyList.addAll(it.forSettlement!!)
//                        transactionHistoryAdapter.updateDataset(it.forSettlement, 1)

                        for (historyData in historyList) {
                            if (historyData.status.equals("PENDING", false)) {
                                count++
                            }
                            if (historyData.status.equals("COMPLETED", false)) {
                                completedCount++
                            }
                        }

                        showLog("History", "" + completedCount)
                    } else {
                        historyList.clear()
//                        transactionHistoryAdapter.notifyDataSetChanged()
                        shortToast("No Records Found")
                    }
                }
            }

//            btnSettlementVisibility(transactionType, count, completedCount, historyData)

            if (historyList.isEmpty()) {
                showErrorMessage("No Records Found")
            }
//            if (it.responseCode.equals(Constants.Network.RESPONSE_SUCCESS, true)) {
//
//            } else {
//                textview_state_message.text = it.responseDescription
//                textview_state_message.isVisible = true
//                shortToast(it.responseDescription)
//            }

        }
    }

    private fun listTransactionHistoryData(transactionHistoryDataSet: ArrayList<ForSettlement>) {
        textview_state_message?.apply {
            isVisible = false
        }

        var count = 0
        var completedCount = 0

        transactionHistoryAdapter.updateDataset(newDataSet = transactionHistoryDataSet)

        for (item in transactionHistoryDataSet) {
            if (item.status.equals("PENDING", false)) {
                count++
            }
            if (item.status.equals("COMPLETED", false)) {
                completedCount++
            }
        }

        Log.i(TAG, "listTransactionHistoryData: count -> $count / completed -> $completedCount")

//        btnSettlementVisibility(transactionType, count, completedCount, transactionHistoryDataSet)
        btnSettlementVisibility(transactionType)
    }

    // Show hide Settlement Button in History page
    private fun btnSettlementVisibility(
        trxType: String
    ) {
        showLog("Service", trxType)

//        if (pendingCount > 0) {
//            showDialog(
//                title = "",
//                description = "You have $pendingCount pending transaction(s), kindly complete.",
//                activity = requireActivity()
//            )
//        }

//        if (transactionHistoryDataSet.size <= 0) {
//            btnSettlementHistory.visibility = View.GONE
//        }

        when (trxType) {
            Fields.ALL -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
            Fields.CARD -> {
//                if (completedCount > 0) {
//                    btnSettlementHistory.visibility = View.VISIBLE
//                } else {
//                    btnSettlementHistory.visibility = View.GONE
//                }
                btnSettlementHistory.visibility = View.VISIBLE
                fabTransactionStatus.visibility = View.GONE
            }
//            Fields.Moto -> {
//                Log.i(TAG, "btnSettlementVisibility: trxType -> $trxType")
//                btnSettlementHistory.visibility = View.VISIBLE
//                Glide
//                    .with(requireContext())
//                    .load(R.drawable.ezylink_meter_with_bg_60_px)
//                    .circleCrop()
//                    .into(fabImage)
//
//            }

            Fields.Moto, Fields.EZYREC, Fields.EZYSPLIT, Fields.EZYPASS -> {
                if (trxType == Fields.Moto) {
                    fabTransactionStatus.visibility = View.VISIBLE
                } else {
                    fabTransactionStatus.visibility = View.GONE
                }

                if (getLoginResponse().hostType.equals("P", true)) {

                    Glide
                        .with(requireContext())
                        .load(R.drawable.ic_coin)
                        .circleCrop()
                        .into(fabImage)

                    btnSettlementHistory.visibility = View.VISIBLE
                } else {
                    btnSettlementHistory.visibility = View.GONE
                }
            }

            Fields.BOOST, Fields.GRABPAY, Fields.CASH, PREAUTH -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
            else -> {
                btnSettlementHistory.visibility = View.GONE
                fabTransactionStatus.visibility = View.GONE
            }
        }
    }

//    private fun requestTransactionHistoryData(historyParam: HashMap<String, String>) {
//        showLoadingDialog(message = "Loading History...")
//
//        historyParam[Fields.PAGE_NUMBER] = "1"
//
//        transactionType = historyParam[Fields.Service]!!w
//        val apiResponse = ApiService.serviceRequest()
//        apiResponse.getTransactionHistory(historyParam).enqueue(object :
//            Callback<TransactionHistoryResponseData> {
//            override fun onFailure(call: Call<TransactionHistoryResponseData>, t: Throwable) {
//                closeLoadingDialog()
//            }
//
//            override fun onResponse(
//                call: Call<TransactionHistoryResponseData>,
//                response: Response<TransactionHistoryResponseData>
//            ) {
//                closeLoadingDialog()
//                if (response.isSuccessful) {
//                    historyObserveData(response.body()!!)
//                }
//            }
//        })
//    }


    // Have to study about Observer and work
    private fun jsonTransactionHistory(historyParam: HashMap<String, String>) {
//        showDialog("Loading History...")

        transactionType = historyParam[Fields.Service]!!
//        historyViewModel.getTransactionHistory(historyParam)
//        historyViewModel.transactionHistoryList.observe(
//            viewLifecycleOwner,
//            Observer {
//                cancelDialog()
//                historyObserveData(it)
//            }
//        )
    }

//    private val dataObserver = Observer<TransactionHistoryResponseData> { data ->
//        Log.i(TAG, "Observer Data -> $data ")
//        if (data != null) {
//            historyObserveData(data)
//        }
//    }


    override fun onStop() {
        super.onStop()
//        historyViewModel.transactionHistoryList.removeObservers(this)
    }

    private fun getTrxList(): ArrayList<String> {

        Log.i(TAG, "getTrxList: ${getProductList()}")

        val histList = ArrayList<String>()
        histList.add(Fields.ALL_TRANSACTION)

        for (data in getProductList()) {
            if (data.isEnable) {
                if (data.historyName == Constants.MobiCash) {
                    histList.add(Fields.FPX)
                }
                histList.add(data.historyName)
            }
        }
        return histList
    }

    private fun getAvailableProductCharArray(): Array<out CharSequence?> {
        return getTrxList().toArray(arrayOfNulls<CharSequence>(getTrxList().size))
    }

    private fun enableVoidOption() {
        val swipeToDeleteCallback = object : SwipeToVoidCallback(context) {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.bindingAdapterPosition
                return if (transactionHistoryAdapter.getData().size > position) {
                    val item = transactionHistoryAdapter.getData()[position]
                    return if (item.status.equals("PENDING", true) ||
                        item.txnType.equals(Fields.FPX, true) ||
                        item.status.equals("REFUND", true) ||
                        item.status.equals("VOID", false) ||
                        item.status.equals("VOID", false)
                    )
                        makeMovementFlags(0, 0)
                    else
                        makeMovementFlags(0, ItemTouchHelper.LEFT)
                } else
                    makeMovementFlags(0, 0)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, i: Int) {
                // i = 4 = delete
                // i = 8 =  mark read
                showLog("Direction", " $i")
                if (i == 4) {
                    val dataPosition = viewHolder.bindingAdapterPosition
                    val item = transactionHistoryAdapter.getData()[dataPosition]
                    position = dataPosition
                    transactionHistoryAdapter.notifyItemRemoved(position!!)
                    if (item.txnType.equals(Fields.CASH)) {
                        requestData.clear()
                        jsonVoidTransaction("Void", item, requestData)
                    } else
                        showPasswordPrompt(item)
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(historyRecyclerView)
    }

    private fun showPasswordPrompt(item: ForSettlement) {
        lateinit var mAlertDialog: AlertDialog

        val inflater = requireActivity().layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_void_validate, null)
        val etUsername = alertLayout.findViewById<View>(R.id.username_edt_void) as EditText
        val etPassword = alertLayout.findViewById<View>(R.id.password_edt_void) as EditText
        val btnVoid = alertLayout.findViewById<View>(R.id.btn_alert_void) as Button
        val btnCancel = alertLayout.findViewById<View>(R.id.btn_alert_cancel) as Button
        val btnFinger = alertLayout.findViewById<View>(R.id.btn_alert_finger) as Button
        val savedName = getSharedString(UserName)
        etUsername.setText(savedName)
        etUsername.isEnabled = false
        etPassword.isEnabled = true
        etPassword.isFocusable = true
        etPassword.requestFocus()
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(false)

        btnVoid.setOnClickListener {
            val textPassword = etPassword.text.toString()
            if (!textPassword.equals("", ignoreCase = true)) {
                mAlertDialog.dismiss()
//                val requestVal = HashMap<String, String>()
//                requestVal[Fields.Service] = Fields.VALIDATE_VOID
                requestData[Fields.username] = getSharedString(UserName)
                requestData[Fields.password] = textPassword
//                jsonUserValidation(requestVal, item)
                jsonVoidTransaction("Void", item, requestData)
            } else {
                shortToast(Constants.ENTER_PASSWORD)
            }
        }

        btnFinger.setOnClickListener {
            historyData = item
            showFingerAuthenticationDialog()
            mAlertDialog.dismiss()
        }

        btnCancel.setOnClickListener {
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
        // startActivity(new Intent(GoogleMaps.this, VoidAuthActivity.class).putExtra("trensID", transId));
    }

    private fun showFingerAuthenticationDialog() {
        Log.i(TAG, "showFingerAuthenticationDialog: ")
        biometricPrompt.authenticate(promptInfo)
        /*  Vignesh Selvam
        * Old method will be removed in the future update
        * */

//        finger.showDialog(
//            requireActivity(),
//            Triple(
//                // title
//                getString(R.string.text_fingerprint),
//                // subtitle
//                null,
//                // description
//                null
//            )
//        )
    }

    @Deprecated("Will be removed in future update")
    override fun onFingerprintAuthenticationFailure(errorMessage: String, errorCode: Int) {
        Log.i(TAG, "onFingerprintAuthenticationFailure: ")
        showLog("Finger", " Failure")
        shortToast(errorMessage)
        finger.subscribe(this)
    }

    @Deprecated("Will be removed in future update")
    override fun onFingerprintAuthenticationSuccess() {
        Log.i(TAG, "onFingerprintAuthenticationSuccess: ")
        finger.subscribe(this)
        requestData.clear()
        requestData[Fields.biomerticKey] = Fields.Success
        requestData[Fields.username] = getSharedString(UserName)
        jsonVoidTransaction("Void", historyData, requestData)
    }
    
    @Deprecated("Will be removed in future update")
    private fun jsonUserValidation(
        userValidateParam: HashMap<String, String>,
        item: ForSettlement
    ) {
        showDialog("Validating...")
        historyViewModel.getUserVerification(userValidateParam)
        historyViewModel.userVerification.observe(
            this,
            Observer {
                cancelDialog()
                if (it.responseCode.equals("0000", true)) {
                    showLog("Void Test", it.responseDescription)
//                jsonVoidTransaction("Void", item)
                }
            }
        )
    }

    private fun jsonVoidTransaction(
        status: String,
        historyData: ForSettlement,
        requestData: HashMap<String, String>
    ) {
        var pathStr = "mobiapr19"
        val requestVal = requestData

        Log.e(TAG, "jsonVoidTransaction: txnType -> ${historyData.txnType}")

        when (historyData.txnType) {
            (Fields.CASH) -> {
                requestVal[Fields.Service] = Fields.CASH_CANCEL
                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.trxId] = historyData.txnId
            }
            (Fields.BOOST) -> {
                when {
                    status.equals("Yes, Cancel", false) -> {
                        requestVal[Fields.Service] = Fields.BOOST_STATUS
                        requestData[Fields.username] = getSharedString(UserName)
                        requestVal[Fields.transactionStatus] = Fields.VOID
                    }
                    status.equals("Yes, Complete", false) -> {
                        requestVal[Fields.Service] = Fields.BOOST_STATUS
                        requestData[Fields.username] = getSharedString(UserName)
                        requestVal[Fields.transactionStatus] = Fields.COMPLETED
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.BOOST_VOID
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.mid] = getLoginResponse().mid
                requestVal[Fields.AID] = historyData.aidResponse
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.InvoiceId] = historyData.invoiceId ?: ""
            }
            (Fields.GRABPAY) -> {
                pathStr = "grabpay"
                when {
                    status.equals("Yes", false) -> {
                        requestVal[Fields.Service] = Fields.GPAY_CANCEL
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.GPAY_REFUND
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.tid] = getLoginResponse().tid
                requestVal[Fields.mid] = getLoginResponse().mid
                requestVal[Fields.AID] = historyData.aidResponse
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.InvoiceId] = historyData.invoiceId ?: ""
            }
            (Fields.PRE_AUTH) -> {

                when {
                    status.equals("Complete", true) -> {
                        requestVal[Fields.Service] = Fields.PRE_AUTH_SALE
//                        requestData[Fields.username] = getSharedString(UserName)
                    }
                    status.equals("Cancel", true) -> {
                        requestVal[Fields.Service] = Fields.PRE_AUTH_VOID
//                        requestData[Fields.username] = getSharedString(UserName)
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                if (getLoginResponse().tid.isNotEmpty()) {
                    requestVal[Fields.tid] = getLoginResponse().tid
                } else {
                    requestVal[Fields.tid] = getLoginResponse().motoTid
                }
            }
            Fields.AUTHSALE -> {
//                if (historyData!!.txnType.equals(Fields.AUTHSALE, ignoreCase = true)) {
//                    requestVal[Fields.Service] = Fields.EZYSPLIT_VOID
//                } else {
//                    requestVal[Fields.Service] = Fields.VOID
//                }
                requestVal[Fields.Service] = Fields.EZYSPLIT_VOID

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                requestVal[Fields.tid] = getLoginResponse().tid
            }
            else -> { // Ezywire

                when {
                    status.equals("Complete", false) -> {
                        requestVal[Fields.Service] = Fields.SALE_ACK
                    }
                    status.equals("Cancel", false) -> {
                        requestVal[Fields.Service] = Fields.VOID
                    }
                    else -> {
                        requestVal[Fields.Service] = Fields.VOID
                    }
                }

                requestVal[Fields.sessionId] = getLoginResponse().sessionId
                requestVal[Fields.trxId] = historyData.txnId
                requestVal[Fields.HostType] = getLoginResponse().hostType
                requestVal[Fields.MerchantId] = getLoginResponse().merchantId
                requestVal[Fields.tid] = getLoginResponse().tid
            }
        }

        Log.e(TAG, "jsonVoidTransaction: request value $requestData")

//        showDialog("Processing...")
        AppFunctions.Dialogs.showLoadingDialog("Processing...", requireContext())
        val apiResponse = ApiService.serviceRequest()
        apiResponse.setVoidTransaction(pathStr, requestVal)
            .enqueue(object : Callback<VoidHistoryModel> {
                override fun onFailure(call: Call<VoidHistoryModel>, t: Throwable) {
                    AppFunctions.Dialogs.closeLoadingDialog()
                }

                override fun onResponse(
                    call: Call<VoidHistoryModel>,
                    response: Response<VoidHistoryModel>
                ) {
                    AppFunctions.Dialogs.closeLoadingDialog()
                    if (response.isSuccessful) {
                        shortToast(response.body()!!.responseDescription)
                    }

                    if (serviceType == 1) {
                        currentTransactionHistoryPageNumber = 1
                        transactionHistory("Void Transaction")
                    } else {
                        currentTransactionHistoryPageNumber = 1
                        preAuthTransHistory(transactionType)
                    }
                }
            })

//        historyViewModel.setVoidHistory(pathStr, requestVal)
//        historyViewModel.setVoidHistory.observe(this, Observer {
//            cancelDialog()
//            if (it.responseCode.equals("0000", true)) {
//                shortToast(it.responseDescription)
//                transactionHistory()
//            } else
//                shortToast(it.responseDescription)
//
//        })
    }

    private fun processSettlements() {
//        showDialog("Validating...")

//        val reqParam = HashMap<String, String>()
//        reqParam[Fields.Service] = Fields.SETTLEMENT
//        reqParam[Fields.sessionId] = getLoginResponse().sessionId
//        reqParam[Fields.HostType] = getLoginResponse().hostType
//        reqParam[Fields.MerchantId] = getLoginResponse().merchantId
//        when (transactionType) {
//            Constants.EzyMoto -> {
//                reqParam[Fields.tid] = getLoginResponse().motoTid
//            }
//            Fields.EZYPASS -> {
//                reqParam[Fields.tid] = getLoginResponse().ezypassTid
//            }
//            Fields.EZYREC -> {
//                reqParam[Fields.tid] = getLoginResponse().ezyrecTid
//            }
//            Fields.EZYSPLIT -> {
//                reqParam[Fields.tid] = getLoginResponse().ezysplitTid
//            }
//            else -> {
//                reqParam[Fields.tid] = getLoginResponse().tid
//            }
//        }


        val requestData = SettlementsDataRequestData(
            service = Fields.SETTLEMENT,
            sessionId = getLoginResponse().sessionId,
            hostType = getLoginResponse().hostType,
            merchantId = getLoginResponse().merchantId,
            tid = when (transactionType) {
                Constants.EzyMoto -> {
                    getLoginResponse().motoTid
                }
                Fields.EZYPASS -> {
                    getLoginResponse().ezypassTid
                }
                Fields.EZYREC -> {
                    getLoginResponse().ezyrecTid
                }
                Fields.EZYSPLIT -> {
                    getLoginResponse().ezysplitTid
                }
                else -> {
                    getLoginResponse().tid
                }
            }
        )

        lifecycleScope.launch {
            AppFunctions.Dialogs.showLoadingDialog("Procession...", requireContext())
            when (val response = historyViewModel.makeSettlement(requestData)) {
                is SettlementsResponse.Response -> {
                    shortToast(response.message)
                    AppFunctions.Dialogs.closeLoadingDialog()
                    transactionHistory("On Settlement done")
                }

                is SettlementsResponse.Exception -> {
                    shortToast(response.exceptionMessage)
                    AppFunctions.Dialogs.closeLoadingDialog()
                    transactionHistory("On Settlement done")
                }
            }
        }


//        historyViewModel.getSettlement(reqParam)

//        historyViewModel.settlementData.observe(
//            this,
//            {
//                cancelDialog()
//                if (it.responseCode.equals("0000", true)) {
//                    transactionHistory("3")
//                }
//                shortToast(it.responseDescription)
//            }
//        )
    }

    // Search Option
    private fun searchView() {
        historySearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                showLog("Query", "" + query)
                transactionHistoryAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                transactionHistoryAdapter.filter.filter(newText)
                return false
            }
        })
    }

    fun addFragment(data: ForSettlement, bundle: Bundle) {
        bundle.putSerializable("History", data)
        bundle.putString(Fields.TRX_TYPE, transactionType)

//        findNavController().navigate(
//            R.id.action_navigation_history_to_historyDetailFragment2,
//            bundle
//        )

        addFragment(historyDetailFragment, bundle, "History")
    }

    @Deprecated("Cancel option for pending transaction is removed")
    fun showAlert(forSettlement: ForSettlement) {

        showLog("Trx_type", "" + forSettlement.txnType)
        var descriptionStr = "Do you want to COMPLETE this transaction?"
        var positiveStr = "Complete"
        var negativeStr = "Dismiss"
        var neutralStr = "Cancel"
        var titleStr = ""

        titleStr = when {
            forSettlement.txnType.equals(Fields.PRE_AUTH, false) -> {
                PREAUTH
            }
            else -> {
                forSettlement.txnType.toString()
            }
        }

        when {
            forSettlement.txnType.equals(Fields.BOOST, false) -> {
                descriptionStr = "Do you want to COMPLETE this transaction?"
                neutralStr = "Yes"
                positiveStr = "Yes, Complete"
                negativeStr = "No, Close"
            }
            forSettlement.txnType.equals(Fields.GRABPAY, false) -> {
                descriptionStr = "Do you want to CANCEL this transaction?"

                positiveStr = "Yes, Cancel"
                negativeStr = "No, Close"
            }
            else -> {
                descriptionStr = "Do you want to COMPLETE this transaction?"

                neutralStr = "Cancel"

                positiveStr = "Yes, Complete"
                negativeStr = "No, Close"
            }
        }
        // Initialize a new instance of
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle(titleStr)
        builder.setMessage(descriptionStr)

        builder.setPositiveButton(positiveStr) { dialog, which ->

            // TODO: 11-08-2021
            /*  Vignesh Selvam
            * Enabled Coroutine for new api call */

            when (forSettlement.tid) {
                //  Online Grab pay
                getLoginResponse().gpayOnlineTid -> {
                    cancelOnlineGrabPay(forSettlement)
                }
                //  OR code grab pay
                else -> {
                    requestData.clear()
                    jsonVoidTransaction(positiveStr, forSettlement, requestData)
                }
            }


/*            if (forSettlement.tid == getLoginResponse().gpayOnlineTid) {
                cancelOnlineGrabPay(forSettlement)
            }
            if (forSettlement.tid == getLoginResponse().gpayTid) {
                requestData.clear()
                jsonVoidTransaction(positiveStr, forSettlement, requestData)
            }*/

            dialog.dismiss()
        }

        builder.setNegativeButton(negativeStr) { dialog, which ->
            dialog.dismiss()
        }

//         Disabled the neutral button which is same as the positive button

//        builder.setNeutralButton(neutralStr) { dialog, which ->
//            requestData.clear()
//            jsonVoidTransaction(neutralStr, forSettlement, requestData)
//            dialog.dismiss()
//        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun cancelOnlineGrabPay(forSettlement: ForSettlement) {
        showDialog("Processing...")
        val requestData = NGrabPayRequestData(
            sessionId = getLoginResponse().sessionId,
            service = Constants.ApiService.N_GRAB_PAY_ONE_TIME_REFUND,
            partnerTxID = forSettlement.rrn!!,
            description = Constants.Common.GRAB_PAY_REFUND_DESCRIPTION
        )

        lifecycleScope.launch {
            val pathStr = "grabpay"
            historyViewModel.cancelPendingTransaction(pathStr, requestData).let {
                when (it) {
                    is NGrabPayResponse.Success -> {
                        // show success message
                        showAlertMessage(
                            title = requireContext().getString(R.string.message),
                            message = it.data.responseDescription,
                            isCancellable = false,
                            positiveButtonText = requireContext().getString(R.string.OK),
                            onPositiveButton = DialogInterface.OnClickListener { dialogInterface, i ->
                                dialogInterface.dismiss()
                            }
                        )
                    }
                    is NGrabPayResponse.Error -> {
                        shortToast(text = it.errorMessage)
                    }
                    is NGrabPayResponse.Exception -> {
                        shortToast(text = it.exceptionMessage)
                    }
                }
                cancelDialog()
            }
        }
    }

    private fun showAuthPrompt() {
        lateinit var mAlertDialog: AlertDialog

        val inflater = requireActivity().layoutInflater
        val alertLayout: View = inflater.inflate(R.layout.alert_ezyauth, null)
        val digitalImg = alertLayout.findViewById<View>(R.id.ezydigital_img) as ImageView
        val ezywireImg = alertLayout.findViewById<View>(R.id.ezywire_img) as ImageView
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
        alert.setView(alertLayout)
        alert.setCancelable(true)

        if (getProductList()[1].isEnable)
            ezywireImg.setImageResource(R.drawable.ezyauth)
        else
            ezywireImg.setImageResource(R.drawable.auth_wire_disable)

        if (getProductList()[0].isEnable)
            digitalImg.setImageResource(R.drawable.auth_digital_enable)
        else
            digitalImg.setImageResource(R.drawable.auth_digital_disable)

        ezywireImg.setOnClickListener {
            if (getProductList()[1].isEnable) {
                preAuthTransHistory(Fields.CARD)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[1].productName}")
            }
            mAlertDialog.dismiss()
        }

        digitalImg.setOnClickListener {
            if (getProductList()[0].isEnable) {
                preAuthTransHistory(Fields.EZYMOTO)
            } else {
                shortToast("You are not Subscribed for ${getProductList()[0].productName}")
            }
            mAlertDialog.dismiss()
        }

        mAlertDialog = alert.create()
        mAlertDialog.show()
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        menu.clear()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            android.R.id.home -> {
//                (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
//                    false
//                )
//                requireActivity().supportFragmentManager.popBackStack()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_settlement_history -> {
                processSettlements()
            }
            R.id.floating_action_button_transaction_status -> {
                val intent = Intent(requireActivity(), TransactionStatusActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra(Constants.NavigationKey.TID, getLoginResponse().motoTid)
                requireContext().startActivity(intent)
            }
            R.id.image_button_filter_product -> {
                showProductFilterDialog()
            }
        }
    }

    private fun showProductFilterDialog() {


        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose a product")
        builder.setItems(
            getAvailableProductCharArray()
        ) { dialog, position ->
            showLog("Selected", getTrxList()[position])

            historyList.clear()
            transactionHistoryAdapter.clearDataset()
            currentTransactionHistoryPageNumber = 1

            transactionType = when (getTrxList()[position]) {
                Fields.ALL_TRANSACTION -> Fields.ALL
                Constants.EzyMoto, Constants.EzySplit -> Fields.Moto
                Fields.EZYWIRE -> Fields.CARD
                else -> getTrxList()[position]
            }


            if (getTrxList()[position].equals(PREAUTH, ignoreCase = true)) {
                if (getProductList()[1].isEnable)
                    preAuthTransHistory(Fields.CARD)
                else
                    preAuthTransHistory(Fields.EZYMOTO)
            } else {

//                    reset page number to load from first
                transactionHistory("on Transaction Type Change")
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
