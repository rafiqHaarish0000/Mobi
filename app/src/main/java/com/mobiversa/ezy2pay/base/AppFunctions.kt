package com.mobiversa.ezy2pay.base

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.mobiversa.ezy2pay.BuildConfig
import com.mobiversa.ezy2pay.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

internal  val TAG = AppFunctions::class.java.canonicalName
object AppFunctions {

    private var loadingDialog: AlertDialog? = null
    private var isLoading: Boolean = false

    fun parseAmount(amount: String): String {
        try {
            val parsedAmount: String = String.format(
                "%s.%s",
                amount.substring(0, amount.length - 2),
                amount.substring(amount.length - 2)
            )
            val formatter = DecimalFormat("#,##0.00")
            return String.format(
                "RM %s", formatter.format(
                    parsedAmount.toDouble()
                )
            )
        }catch (e: Exception){
            e.printStackTrace()
            Log.e(TAG, "parseAmount: $amount" )

            return "RM 0.00"
        }
    }

    fun parseDate(
        outputDatePattern: String,
        inputDateFormatPattern: String = "yyyy-MM-dd HH:mm:ss",
        dateString: String
    ): String {
        val date = SimpleDateFormat(inputDateFormatPattern, Locale.getDefault()).parse(dateString)
        return SimpleDateFormat(outputDatePattern, Locale.getDefault()).format(date!!).toString()
    }

    object SoftKeyboard {
        fun closeSoftKeyboard(currentFocus: View?, context: Context) {
            currentFocus?.let { view ->
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun showSoftKeyboard(currentFocus: View?, context: Context) {
            currentFocus?.let { view ->
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(view, 0)
            }
        }
    }


    object Dialogs {
        fun showLoadingDialog(message: String, context: Context) {
            if (loadingDialog != null) {
                loadingDialog = null
            }

            val inflater: LayoutInflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.base_loading_layout, null)

            val builder = AlertDialog.Builder(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val textViewMessage = view.findViewById<TextView>(R.id.text_view_message)
                textViewMessage.text = message
                builder.setView(view)
            } else {
                builder.setMessage(message)
            }

            if (!BuildConfig.DEBUG) {
                builder.setCancelable(false)
            }

            builder.setOnCancelListener {
                isLoading = false
            }

            loadingDialog = builder.create()
            loadingDialog!!.show()
        }


        fun closeLoadingDialog() {
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
            loadingDialog = null
        }
    }

}