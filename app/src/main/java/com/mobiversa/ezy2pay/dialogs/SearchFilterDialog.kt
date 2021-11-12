package com.mobiversa.ezy2pay.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mobiversa.ezy2pay.R
import java.text.SimpleDateFormat
import java.util.*


internal val TAG = SearchFilterDialog::class.java.canonicalName

class SearchFilterDialog(val callback: SearchFilterCallBack) : DialogFragment(),
    View.OnClickListener {

    private lateinit var radioTransactionStatus: RadioGroup
    private var fromDate: Long = 0L
    private var toDate: Long = 0L

    private lateinit var textViewFromDate: TextView
    private lateinit var textViewToDate: TextView

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioTransactionStatus = view.findViewById(R.id.radio_group_transaction_status)
        view.findViewById<Button>(R.id.button_search).setOnClickListener(this)
        view.findViewById<Button>(R.id.button_cancel).setOnClickListener(this)
        textViewFromDate = view.findViewById(R.id.textView_from_date)
        textViewFromDate.apply {
            text = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Calendar.getInstance().timeInMillis)
            setOnClickListener(this@SearchFilterDialog)
        }
        textViewToDate = view.findViewById(R.id.textView_to_date)
        textViewToDate.apply {
            text = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(Calendar.getInstance().timeInMillis)
            setOnClickListener(this@SearchFilterDialog)
        }

    }


    companion object {
        fun newInstance(callback: SearchFilterCallBack): SearchFilterDialog {
            return SearchFilterDialog(callback)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.button_cancel -> {
                callback.onCancel()
                dismiss()
            }
            R.id.button_search -> {

                val status = when (radioTransactionStatus.checkedRadioButtonId) {
                    R.id.radio_pending -> "P"
                    R.id.radio_success -> "S"
                    R.id.radio_failure -> "F"
                    R.id.radio_not_in_use -> "N"
                    else -> ""
                }

                val fromDate = textViewFromDate.text.toString()
                val toDate = textViewToDate.text.toString()
                callback.onSearch(fromDate = fromDate, toDate = toDate, status = status)

//                if (fromDate == 0L && toDate == 0L) {
//                    val transactionStatus: String =
//
//                    callback.onSearchWithTransaction(transactionStatus)
//                } else if (fromDate != 0L && toDate != 0L) {
//                    val transactionStatus: String =
//                        when (radioTransactionStatus.checkedRadioButtonId) {
//                            R.id.radio_pending -> "P"
//                            R.id.radio_success -> "S"
//                            R.id.radio_failure -> "F"
//                            R.id.radio_not_in_use -> "NO STATUS"
//                            else -> ""
//                        }
//                    callback.onSearch(fromDate, toDate, transactionStatus)
//                } else {
//                    Toast.makeText(
//                        requireContext(),
//                        "Both From date and To date required",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
//
//                if (fromDate == 0L || toDate == 0L) {
//                    Toast.makeText(
//                        requireContext(),
//                        "Both From date and To date required",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
                dismiss()
            }
            R.id.textView_from_date -> {
                openFromDate(v)
            }
            R.id.textView_to_date -> {
                openToDate(v)
            }
        }
    }

    private fun openToDate(rootView: View) {
        val newCalendar: Calendar = Calendar.getInstance()


        val toDate = DatePickerDialog(
            requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                Log.i(
                    TAG,
                    "openToDate: from date --> ${newDate.get(Calendar.DATE)}/${newDate.get(Calendar.MONTH)}/${
                        newDate.get(Calendar.YEAR)
                    }"
                )
                toDate = newDate.timeInMillis
                try {
                    rootView.findViewById<TextView>(R.id.textView_to_date).text = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).format(newDate.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
        toDate.setTitle("Select to date")

        if (fromDate != 0L)
            toDate.datePicker.minDate = fromDate

        toDate.datePicker.maxDate = System.currentTimeMillis()
        toDate.show()
    }

    private fun openFromDate(rootView: View) {
        val newCalendar: Calendar = Calendar.getInstance()

        if (toDate != 0L)
            newCalendar.timeInMillis = toDate

        val fromDate = DatePickerDialog(
            requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                val newDate: Calendar = Calendar.getInstance()
                newDate.set(year, monthOfYear, dayOfMonth)
                Log.i(
                    TAG,
                    "openToDate: from date --> ${newDate.get(Calendar.DATE)}/${newDate.get(Calendar.MONTH)}/${
                        newDate.get(Calendar.YEAR)
                    }"
                )

                fromDate = newDate.timeInMillis

                try {
                    rootView.findViewById<TextView>(R.id.textView_from_date).text =
                        SimpleDateFormat(
                            "yyyy-MM-dd",
                            Locale.getDefault()
                        ).format(newDate.time)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            newCalendar.get(Calendar.YEAR),
            newCalendar.get(Calendar.MONTH),
            newCalendar.get(Calendar.DAY_OF_MONTH)
        )
        fromDate.setTitle("Select from date")

        if (toDate != 0L)
            fromDate.datePicker.maxDate = toDate
        else
            fromDate.datePicker.maxDate = System.currentTimeMillis()
        fromDate.show()
    }


    interface SearchFilterCallBack {
        fun onSearch(fromDate: String, toDate: String, status: String)
        fun onCancel()
        fun onSearchWithTransaction(status: String)
    }
}