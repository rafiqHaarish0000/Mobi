package com.mobiversa.ezy2pay.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mobiversa.ezy2pay.R
import java.text.SimpleDateFormat
import java.util.*


internal val TAG = SearchFilterDialog::class.java.canonicalName

class SearchFilterDialog(val callback: SearchFilterCallBack) : DialogFragment(),
    View.OnClickListener {

    lateinit var fromDate: String
    lateinit var toDate: String

    override

    fun onResume() {
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

        view.findViewById<Button>(R.id.button_search).setOnClickListener(this)
        view.findViewById<Button>(R.id.button_cancel).setOnClickListener(this)
        val fromDate = view.findViewById<TextView>(R.id.textView_from_date)
        fromDate.setOnClickListener(this)
        val toDate = view.findViewById<TextView>(R.id.textView_to_date)
        toDate.setOnClickListener(this)

    }

    interface SearchFilterCallBack {
        fun onSearch()
        fun onCancel()
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
                callback.onSearch()
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
                toDate = newDate.timeInMillis.toString()
                try {
                    rootView.findViewById<TextView>(R.id.textView_to_date).text = SimpleDateFormat(
                        "dd-MM-yyyy",
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
        toDate.datePicker.maxDate = System.currentTimeMillis()
        toDate.show()
    }

    private fun openFromDate(rootView: View) {
        val newCalendar: Calendar = Calendar.getInstance()
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

                fromDate = newDate.timeInMillis.toString()

                try {
                    rootView.findViewById<TextView>(R.id.textView_from_date).text =
                        SimpleDateFormat(
                            "dd-MM-yyyy",
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
        fromDate.datePicker.maxDate = System.currentTimeMillis()
        fromDate.show()
    }


}