package com.mobiversa.ezy2pay.ui.receipt

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.dataModel.PrintReceiptRequestData
import com.mobiversa.ezy2pay.dataModel.PrintReceiptResponse
import com.mobiversa.ezy2pay.network.response.Country
import com.mobiversa.ezy2pay.ui.ezyMoto.EzyMotoViewModel
import com.mobiversa.ezy2pay.utils.*
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.print_receipt_fragment.view.*
import kotlinx.coroutines.launch

internal val TAG = PrintReceiptFragment::class.java.canonicalName

class PrintReceiptFragment : BaseFragment(), AdapterView.OnItemSelectedListener,
    View.OnClickListener {

    companion object {
        fun newInstance() = PrintReceiptFragment()
    }

    private var isAllFieldsMandatory: Boolean = true
    private lateinit var chkWhatsapp: AppCompatCheckBox
    private lateinit var viewModel: PrintReceiptViewModel
    private lateinit var motoViewModel: EzyMotoViewModel


    private val countryArray = ArrayList<String>()
    private lateinit var countryList: ArrayList<Country>
    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var countrySpinner: SearchableSpinner
    private lateinit var btnSendReceipt: AppCompatTextView

    private lateinit var edtEmailReceipt: EditText
    private lateinit var edtPhoneNumReceipt: EditText
    private lateinit var edtCountryCodeReceipt: EditText
    private lateinit var PrintReceiptLayout: LinearLayout

    //    private lateinit var PrintReceipt: ImageView
    private lateinit var printTVTxt: TextView

    private var spinnerPosition = 0

    private var service = ""
    private var trxId = ""
    private var amount = ""
    private var signatureStr = ""


    private var activityName = ""
    private var redirectName = ""
    protected var PRINTReceipt = false
    lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       // Log.i(TAG, "onCreateView: PrintReceiptFragment")

        rootView = inflater.inflate(R.layout.print_receipt_fragment, container, false)

        val repository = AppRepository.getInstance()
        val viewModelFactory = AppViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PrintReceiptViewModel::class.java)
        motoViewModel = ViewModelProvider(this).get(EzyMotoViewModel::class.java)

        service = requireArguments().getString(Fields.Service, "")
        trxId = requireArguments().getString(Fields.trxId, "")
        amount = requireArguments().getString(Fields.Amount, "")
        activityName = requireArguments().getString(Constants.ActivityName, "")
        redirectName = requireArguments().getString(Constants.Redirect, "")
        if (service.equals(Fields.TXN_REPRINT, true))
            signatureStr = requireArguments().getString(Fields.Signature, "")


        /* Vignesh Selvam
        * if isEzyWire is false allow email or phone only from ezy wire transaction
        * show both phone and email if isEzyWire is true
        * */

        // TODO: 1/11/2022
        /* Mohammed Rafiq
        * For all transaction in print receipt screen email and phone number is mandatory
        * */

//        isEzyWire = requireArguments().getBoolean(Constants.NavigationKey.IS_EZY_WIRE, false)
//        Log.i(TAG, "onCreateView: $isEzyWire")

        isAllFieldsMandatory =
            requireArguments().getBoolean(Constants.NavigationKey.ALL_FIELDS_MANDATORY, true)
        if (!isAllFieldsMandatory) {
            rootView.chk_whatsapp_receipt.isVisible = true
            rootView.relative_layout_or_divider.isVisible = true
            rootView.text_view_email_only_note.isVisible = false
        } else {
            rootView.chk_whatsapp_receipt.isVisible = false
            rootView.relative_layout_or_divider.isVisible = false
            rootView.text_view_email_only_note.isVisible = true
        }

        rootView.amount_txt.text = amount
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize(view)
//        editTextWatcher()

        val productParams = HashMap<String, String>()
        productParams[Fields.Service] = Fields.CountryList
        jsonCountryList(productParams)
    }

    private fun initialize(rootView: View) {
        countrySpinner = rootView.spinner_country_receipt
        edtEmailReceipt = rootView.edt_email_receipt
        edtPhoneNumReceipt = rootView.edt_phone_num_receipt
        chkWhatsapp = rootView.chk_whatsapp_receipt
        edtCountryCodeReceipt = rootView.edt_country_code_receipt
        btnSendReceipt = rootView.btn_receipt_slip
        PrintReceiptLayout = rootView.PrintReceiptLayout
//        PrintReceipt = rootView.PrintReceipt
        printTVTxt = rootView.printTVTxt
        btnSendReceipt.setOnClickListener(this)
        PrintReceiptLayout.setOnClickListener(this)

        PrintReceiptLayout.setBackgroundResource(R.color.white)
        printTVTxt.setTextColor(resources.getColor(R.color.grey))
        printTVTxt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.print_grey, 0, 0, 0)

        setTitle("PrintReceipt", false)
        if (activityName.equals(Constants.MainAct, true)) {
            (requireActivity() as MainActivity).supportActionBar?.title = "PrintReceipt"
            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            (requireActivity() as MainActivity).supportActionBar?.title = "PrintReceipt"
            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }


    private fun editTextWatcher() {

        edtEmailReceipt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) { // TODO Auto-generated method stub
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) { // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtEmailReceipt.text.toString().equals("", ignoreCase = true)) {
                    edtPhoneNumReceipt.isFocusable = false
                    edtPhoneNumReceipt.isCursorVisible = false
                    edtPhoneNumReceipt.isFocusableInTouchMode = false
                } else {
                    edtPhoneNumReceipt.isFocusable = true
                    edtPhoneNumReceipt.isCursorVisible = true
                    edtPhoneNumReceipt.isFocusableInTouchMode = true
                }
            }
        })

        edtPhoneNumReceipt.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (!edtPhoneNumReceipt.text.toString().equals("", ignoreCase = true)) {
                    edtEmailReceipt.isFocusable = false
                    edtEmailReceipt.isCursorVisible = false
                    edtEmailReceipt.isFocusableInTouchMode = false
                } else {
                    edtEmailReceipt.isFocusable = true
                    edtEmailReceipt.isCursorVisible = true
                    edtEmailReceipt.isFocusableInTouchMode = true
                }
            }
        })

    }

    private fun setUpCountrySpinner() {

        countryAdapter = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            countryArray
        )
        countrySpinner.adapter = countryAdapter
        countrySpinner.setTitle("Select Countries")
        countrySpinner.setPositiveButton("Done")
        countrySpinner.setSelection(spinnerPosition)

        countrySpinner.onItemSelectedListener = this
    }

    private fun jsonCountryList(countryParams: HashMap<String, String>) {
        showDialog("Processing...")
        motoViewModel.countryList(countryParams)
        motoViewModel.countryList.observe(viewLifecycleOwner, Observer {

            if (it.responseCode.equals("0000", true)) {
                countryArray.clear()
                countryList = it.responseData.country
                for ((index, value) in it.responseData.country.withIndex()) {
                    countryArray.add(value.countryName)
                    if (value.countryName.equals("Malaysia", false))
                        spinnerPosition = index
                }
                val gson = Gson()
                val json = gson.toJson(it.responseData)
                putSharedString(Constants.CountryResponse, json)
                setUpCountrySpinner()

            } else {
                setTitle("Home", true)
                if (activityName.equals(Constants.MainAct, true)) {
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                } else
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

                // TODO: 23-11-2021
                /*  Vignesh Selvam
                * Pop backstack removed
                *  replaced with navigation component
                *
                * */
//                fragmentManager?.popBackStack()
                if (!isAllFieldsMandatory) {
                    fragmentManager?.popBackStack()
                } else {
                    findNavController().navigateUp()
                }
            }
            cancelDialog()
        })

    }

    private fun jsonSendReceipt() {
//        showDialog("Loading")


        lifecycleScope.launch {

            AppFunctions.Dialogs.showLoadingDialog("Processing...", requireContext())
            val requestData = PrintReceiptRequestData(
                service = service,
                username = getSharedString(Constants.UserName),
                sessionId = getLoginResponse().sessionId,
                hostType = getLoginResponse().hostType,
                trxId = trxId
            )

            if (!isAllFieldsMandatory) {
                if (edtPhoneNumReceipt.text.isEmpty()) {
                    requestData.mobileNo = ""
                    requestData.email = edtEmailReceipt.text.toString()
                    requestData.whatsApp = getIsWhatsapp(false)
                } else {
                    requestData.whatsApp = getIsWhatsapp(chkWhatsapp.isChecked)
                    requestData.mobileNo =
                        edtCountryCodeReceipt.text.toString() + edtPhoneNumReceipt.text.toString()
                    requestData.email = edtEmailReceipt.text.toString()
                }
            } else {
                requestData.mobileNo =
                    edtCountryCodeReceipt.text.toString() + edtPhoneNumReceipt.text.toString()
                requestData.email = edtEmailReceipt.text.toString()
                requestData.whatsApp = getIsWhatsapp(false)
            }

            when (val response = viewModel.sendReceipt(requestData)) {
                is PrintReceiptResponse.Success -> {
                    shortToast(response.data.responseDescription)
                    if (PRINTReceipt) {
                        val gson = Gson()
                        val json = gson.toJson(response.data)
                        AppFunctions.Dialogs.closeLoadingDialog()
                        requireContext().startActivity(
                            Intent(
                                getActivity(),
                                PrinterActivity::class.java
                            ).putExtra("receiptData", json)
                        )
                    } else {
                        if (activityName.equals(Constants.MainAct, true)) {
                            (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(
                                false
                            )

                            // TODO: 23-11-2021
                            /*  Vignesh Selvam
                            * Pop backstack removed
                            * replaced with navigation component
                            *
                            * */

                            AppFunctions.Dialogs.closeLoadingDialog()
                            if (!isAllFieldsMandatory) {
                                fragmentManager?.popBackStack()
                            } else {
                                findNavController().navigateUp()
                            }


                        } else {
                            AppFunctions.Dialogs.closeLoadingDialog()
                            if (!isAllFieldsMandatory) {
                                fragmentManager?.popBackStack()
                            } else {
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
                is PrintReceiptResponse.Error -> {
                    AppFunctions.Dialogs.closeLoadingDialog()
                    shortToast(response.errorMessage)
                    if (!isAllFieldsMandatory) {
                        fragmentManager?.popBackStack()
                    } else {
                        findNavController().navigateUp()
                    }
                }
                is PrintReceiptResponse.Exception -> {
                    AppFunctions.Dialogs.closeLoadingDialog()
                    shortToast(response.exceptionMessage)
                    if (!isAllFieldsMandatory) {
                        fragmentManager?.popBackStack()
                    } else {
                        findNavController().navigateUp()
                    }
                }
            }
        }

        /* val paymentParams = HashMap<String, String>()
         paymentParams[Fields.Service] = service
         paymentParams[Fields.username] = getSharedString(Constants.UserName)
         paymentParams[Fields.sessionId] = getLoginResponse().sessionId
         paymentParams[Fields.HostType] = getLoginResponse().hostType
         paymentParams[Fields.trxId] = trxId

         if (edtPhoneNumReceipt.text.isEmpty()) {
             paymentParams[Fields.MobileNo] = ""
             paymentParams[Fields.email] = edtEmailReceipt.text.toString()
             paymentParams[Fields.WhatsApp] = getIsWhatsapp(false)
         } else {
             paymentParams[Fields.WhatsApp] = getIsWhatsapp(chkWhatsapp.isChecked)
             paymentParams[Fields.MobileNo] =
                 edtCountryCodeReceipt.text.toString() + edtPhoneNumReceipt.text.toString()
             paymentParams[Fields.email] = edtEmailReceipt.text.toString()
         }

         viewModel.getReceipt(paymentParams)
         viewModel.printReceiptData.observe(this, {
             cancelDialog()
             if (it.responseCode.equals("0000", true)) {
                 shortToast(it.responseDescription)

                 if (PRINTReceipt) {
                     val gson = Gson()
                     val json = gson.toJson(it)
                     requireContext().startActivity(
                         Intent(
                             getActivity(),
                             PrinterActivity::class.java
                         ).putExtra("receiptData", json)
                     )
                 } else {
                     if (activityName.equals(Constants.MainAct, true)) {
                         (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

                         // TODO: 23-11-2021
                         *//*  Vignesh Selvam
                        * Pop backstack removed
                        *  replaced with navigation component
                        *
                        * *//*
//                        fragmentManager?.popBackStack()
                        findNavController().navigateUp()
                    } else {
                        requireContext().startActivity(
                            Intent(
                                getActivity(),
                                MainActivity::class.java
                            )
                        )
                    }
                }


            } else {
                shortToast(it.responseDescription)
                requireContext().startActivity(Intent(getActivity(), MainActivity::class.java))
            }
        })*/
    }

    private fun getIsWhatsapp(whatsApp: Boolean): String {
        return if (whatsApp) "Yes" else "No"
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    @SuppressLint("SetTextI18n")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val spinner = parent as Spinner
        when (spinner.id) {

            R.id.spinner_country_receipt -> {
                edtCountryCodeReceipt.setText("" + countryList[position].phoneCode)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_receipt_slip -> {
                val emailStr = edtEmailReceipt.text.toString()
                val phoneStr = edtPhoneNumReceipt.text.toString()

                if (!isAllFieldsMandatory) {
                    if (emailStr.isEmpty() && phoneStr.isEmpty()) {
                        shortToast("Please enter Email id or Mobile Number")
                    } else if (phoneStr.isEmpty() && !isValidEmail(emailStr)) {
                        shortToast("Please enter Valid Email id")
                    } else
                        jsonSendReceipt()
                } else {
                    if (emailStr.isEmpty() || phoneStr.isEmpty()) {
                        shortToast("Please enter both Email id and Mobile Number")
                    } else if (!isValidEmail(emailStr)) {
                        shortToast("Please enter Valid Email id")
                    } else {
                        jsonSendReceipt()
                    }
                }
            }
            R.id.PrintReceiptLayout -> {
                if (PRINTReceipt) {
                    PRINTReceipt = false
                    PrintReceiptLayout.setBackgroundResource(R.color.lit_grey)
                    printTVTxt.setTextColor(resources.getColor(R.color.grey))
                    printTVTxt.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.print_grey,
                        0,
                        0,
                        0
                    )
                } else {
                    PRINTReceipt = true
                    PrintReceiptLayout.setBackgroundResource(R.color.colorPrimary)
                    printTVTxt.setTextColor(resources.getColor(R.color.colorPrimary))
                    printTVTxt.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.print_blue,
                        0,
                        0,
                        0
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTitle("PrintReceipt", false)
        if (activityName.equals(Constants.MainAct, true)) {
            (requireActivity() as MainActivity).supportActionBar?.title = "PrintReceipt"
            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        } else {
            (requireActivity() as MainActivity).supportActionBar?.title = "PrintReceipt"
            (requireActivity() as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (activityName.equals(Constants.MainAct, true)) {
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    if (redirectName.equals(Constants.History, true))
                        setTitle("Transactions", true)
                    else
                        setTitle("Home", true)
                } else {
                    setTitle("Home", true)
                    (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                if (!isAllFieldsMandatory) {
                    fragmentManager?.popBackStack()
                } else {
                    findNavController().navigateUp()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
