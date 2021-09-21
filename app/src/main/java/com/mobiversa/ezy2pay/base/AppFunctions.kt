package com.mobiversa.ezy2pay.base

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.widget.TextView
import com.mobiversa.ezy2pay.R
import java.text.SimpleDateFormat
import java.util.*

object AppFunctions {

    private var loadingDialog: AlertDialog? = null

    fun parseAmount(amount: Float): String {
        return String.format("RM %.2f", amount)
    }

    fun parseDate(
        outputDatePattern: String,
        inputDateFormatPattern: String = "yyyy-MM-dd HH:mm:ss",
        dateString: String
    ): String {
        val date = SimpleDateFormat(inputDateFormatPattern, Locale.getDefault()).parse(dateString)
        return SimpleDateFormat(outputDatePattern, Locale.getDefault()).format(date!!).toString()
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
            builder.setCancelable(false)

            loadingDialog = builder.create()
            loadingDialog!!.show()
        }

        fun closeLoadingDialog() {
            if (loadingDialog != null) {
                loadingDialog!!.dismiss()
            }
            loadingDialog = null
        }
    }

}