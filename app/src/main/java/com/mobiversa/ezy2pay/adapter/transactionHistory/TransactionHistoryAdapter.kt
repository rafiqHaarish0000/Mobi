package com.mobiversa.ezy2pay.adapter.transactionHistory

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.databinding.HistoryListItemBinding
import com.mobiversa.ezy2pay.network.response.ForSettlement
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.DateFormatter
import com.mobiversa.ezy2pay.utils.Fields
import kotlinx.android.synthetic.main.list_history.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TransactionHistoryAdapter(
    val context: Context,
    private val callback: TransactionHistoryInterface
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

    private var transactionType = Fields.ALL

    private var flag = 1 // 1 - for settlements, 2 pre auth transaction


    private var trxHistory = Fields.TRX_HISTORY
    private var historyList: ArrayList<ForSettlement> = ArrayList()
    private var filterHistoryList = historyList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TransactionHistoryViewHolder(
            HistoryListItemBinding.inflate(layoutInflater),
            callback
        )
    }

    override fun getItemCount(): Int {
        return filterHistoryList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = filterHistoryList[position]
        (holder as TransactionHistoryViewHolder).bind(
            context,
            item
        )
    }

    fun getData(): ArrayList<ForSettlement> {
        return filterHistoryList
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                filterHistoryList = if (charString.isEmpty()) {
                    historyList
                } else {
                    val filteredList: ArrayList<ForSettlement> = ArrayList()
                    for (forSettlement in historyList) {
                        if (forSettlement.invoiceId != null) {
                            if (forSettlement.invoiceId.lowercase(Locale.getDefault())
                                    .startsWith(charString.lowercase(Locale.getDefault()))
                            ) {
                                filteredList.add(forSettlement)
                            }
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filterHistoryList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                if (filterResults.values != null) {
                    filterHistoryList = filterResults.values as ArrayList<ForSettlement>
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class TransactionHistoryViewHolder(
        private val binding: HistoryListItemBinding,
        private val callback: TransactionHistoryInterface
    ) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(context: Context, historyData: ForSettlement) {
            binding.historyItem = historyData

//        14-Nov-2019
            //Aug 5, 2019 6:49:06 PM
            var dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss", Locale.US)
            val date = dateFormat.parse("${historyData.date} ${historyData.time}")
            dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val today_date = dateFormat.format(Calendar.getInstance().time)
            val dateStr = dateFormat.format(date!!)
            binding.root.txt_date.text = dateStr
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val timeStr = timeFormat.format(date)
            binding.root.txt_time.text = timeStr

            if (trxHistory.equals(Fields.PERAUTHHIST, ignoreCase = true)) {
                val totalDays =
                    DateFormatter.getDaysCount(
                        dateFormat.parse(dateStr)!!,
                        dateFormat.parse(today_date)!!
                    )
                binding.root.txt_days.text = "$totalDays Days"
                binding.root.days_linear.visibility = View.VISIBLE
            } else {
                binding.root.days_linear.visibility = View.GONE
            }

            val amount: String =
                (historyData.amount?.substring(0, historyData.amount.length - 2) ?: "0") +
                        "." + (historyData.amount?.substring(historyData.amount.length - 2) ?: 0)
            val formatter = DecimalFormat("#,##0.00")
            val cost =
                (if (historyData.currency == null) "RM " else historyData.currency.toString() + " ") + formatter.format(
                    amount.toDouble()
                )

            binding.txtAmountHistory.text = cost


            if (historyData.status.equals("PENDING", true) || historyData.status.equals(
                    "D",
                    true
                )
            ) {
                binding.statusBg.setBackgroundResource(R.drawable.rect_pending)
                binding.timelineView.setBackgroundColor(Color.parseColor("#faa107"))
                binding.txtStatusHistory.text = Constants.TransactionHistoryStatus.PENDING
            } else {
                binding.txtStatusHistory.text = Constants.TransactionHistoryStatus.COMPLETED
                binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                binding.timelineView.setBackgroundColor(Color.parseColor("#52de97"))
            }


            historyData.cardType?.let {
                when {
                    historyData.cardType.lowercase(Locale.getDefault()).equals("master", true) -> {
                        binding.historyLogoImg.setImageResource(R.drawable.master)
                    }
                    historyData.cardType.lowercase(Locale.getDefault()).equals("visa", true) -> {
                        binding.historyLogoImg.setImageResource(R.drawable.visaa)
                    }
                    historyData.cardType.lowercase(Locale.getDefault())
                        .equals("unionpay", true) -> {
                        binding.historyLogoImg.setImageResource(R.drawable.union_pay_logo)
                    }
                }
                binding.prodNameTxt.text = "EZYWIRE"
            }


            historyData.txnType?.let { it ->
                binding.prodNameTxt.text = it
                when (it.uppercase(Locale.getDefault())) {
                    Fields.CASH -> {
                        if (historyData.status.equals("CASH CANCELLED", true)) {
                            binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.void_red))
                            binding.statusBg.setBackgroundResource(R.drawable.rect_void)
                            binding.txtStatusHistory.text = "Cash Cancelled"
                        } else {
                            binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.completed))
                            binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                            binding.txtStatusHistory.text = "Cash Sale"
                        }
                        binding.historyLogoImg.setImageResource(R.drawable.cash_list_icon)
                        binding.txtRrnHistory.visibility = View.GONE
                        binding.txtAuthcodeHistory.visibility = View.GONE
                        binding.prodNameTxt.text = "CASH"
                    }
                    Fields.FPX -> {
                        binding.txtRrnHistory.visibility = View.VISIBLE
                        binding.txtAuthcodeHistory.visibility = View.VISIBLE
                        binding.txtStanHistory.visibility = View.GONE

                        if (transactionType == Fields.ALL) {
                            binding.historyLogoImg.setImageResource(R.drawable.fpx_logo)
                        } else {
                            Glide.with(context)
                                .load(historyData.latitude) // image url
                                .into(binding.historyLogoImg)  // imageview object
                        }
                    }
                    Fields.GRABPAY -> {
                        when {
                            historyData.status.equals("PENDING", true) -> {
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.pending))
                                binding.statusBg.setBackgroundResource(R.drawable.rect_pending)
                                binding.txtStatusHistory.text = "PENDING"
                            }
                            historyData.status.equals("REFUND", true) -> {
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.pending))
                                binding.statusBg.setBackgroundResource(R.drawable.rect_pending)
                                binding.txtStatusHistory.text = "REFUND"
                            }
                            else -> {
                                binding.txtStatusHistory.text = historyData.status
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.completed))
                                binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                            }
                        }
                        binding.historyLogoImg.setImageResource(R.drawable.grab_pay_list)
                        binding.txtRrnHistory.visibility = View.VISIBLE
                        binding.txtAuthcodeHistory.visibility = View.GONE
                    }
                    Fields.BOOST -> {
                        binding.txtRrnHistory.visibility = View.VISIBLE
                        binding.txtAuthcodeHistory.visibility = View.VISIBLE
                        when {
                            historyData.status.equals("VOID", true) -> {
                                binding.txtStatusHistory.text = "VOID"
                                binding.statusBg.setBackgroundResource(R.drawable.rect_void)
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.void_red))
                            }
                            historyData.status.equals("COMPLETED", true) -> {
                                binding.txtStatusHistory.text =
                                    Constants.TransactionHistoryStatus.COMPLETED
                                binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.completed))
                            }
                            historyData.status.equals("SETTLED", true) -> {
                                binding.txtStatusHistory.text = historyData.status
                                binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.completed))
                            }
                            historyData.status.equals("PENDING", true) -> {
                                binding.txtStatusHistory.text =
                                    Constants.TransactionHistoryStatus.PENDING
                                binding.txtRrnHistory.visibility = View.GONE
                                binding.statusBg.setBackgroundResource(R.drawable.rect_pending)
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.pending))
                            }
                        }
                        binding.historyLogoImg.setImageResource(R.drawable.ic_boost)
                    }
                    Fields.PREAUTH -> {

                        Log.i(TAG, "bind: ${Fields.PREAUTH}")
                        when (historyData.status) {
                            "D" -> {
                                binding.timelineView.setBackgroundColor(context.resources.getColor(R.color.pending))
                                binding.txtStatusHistory.text =
                                    Constants.TransactionHistoryStatus.PENDING
                                binding.statusBg.setBackgroundResource(R.drawable.rect_pending)
                            }
                            "E" -> {
                                Log.i(TAG, "bind: ${historyData.status}")
                                binding.txtStatusHistory.text =
                                    Constants.TransactionHistoryStatus.COMPLETED
                                binding.statusBg.setBackgroundResource(R.drawable.rect_complete)
                            }
                            else -> {
                            }
                        }
                    }
                    else -> {
                        binding.txtRrnHistory.visibility = View.VISIBLE
                        binding.txtAuthcodeHistory.visibility = View.VISIBLE
                        historyData.cardType?.let {
                            when (it.lowercase(Locale.getDefault())) {
                                "master" -> {
                                    binding.historyLogoImg.setImageResource(R.drawable.master)
                                }
                                "visa" -> {
                                    binding.historyLogoImg.setImageResource(R.drawable.visaa)
                                }
                                else -> {
                                    binding.historyLogoImg.setImageResource(R.drawable.union_pay_logo)
                                }
                            }
                        }

                    }
                }
            }


            binding.root.list_history_relative.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(Constants.Amount, cost)
                bundle.putString(Constants.Date, binding.txtDate.text.toString())
                val status = binding.txtStatusHistory.text.toString()

                if (status.equals("Completed", true)
                    || status.equals("Cash Sale", true)
                    || status.equals("E", true)
                ) {
                    callback.onTransactionClicked(historyData = historyData, bundle = bundle)
//                    fragment.addFragment(historyData, bundle = bundle)
                }

                // TODO: 31-08-2021
                /*  Vignesh Selvam
                * Cancel option for pending transaction is removed
                * methoed will be removed in the future update
                * */
                /* if (status.equals("PENDING", false)) {
                     fragment.showAlert(historyData)
                 } */
            }

            binding.txtRrnHistory.visibility = View.GONE
            binding.txtStanHistory.visibility = View.GONE
            binding.prodNameTxt.text = historyData.txnType
        }
    }


    interface TransactionHistoryInterface {
        fun onTransactionClicked(historyData: ForSettlement, bundle: Bundle)
    }


//    fun updateDataset(data: List<ForSettlement>?, flag: Int) {
//        Log.e(TAG, "updateDataset: $data")
//        if (this.flag != flag) {
//            val lastPosition = if (this.historyList.size > 0) {
//                this.historyList.size - 1
//            } else {
//                0
//            }
//            this.historyList = data as ArrayList<ForSettlement>
//            notifyItemChanged(lastPosition)
//            this.filterHistoryList = this.historyList
//        } else {
//            val lastPosition = if (this.historyList.size > 0) {
//                this.historyList.size - 1
//            } else {
//                0
//            }
//            this.historyList.addAll(data as ArrayList<ForSettlement>)
//            notifyItemChanged(lastPosition)
//            this.filterHistoryList = this.historyList
//        }
//    }


    fun updateDataset(transactionType: String, newDataSet: ArrayList<ForSettlement>) {
        this.transactionType = transactionType
        val lastPosition = if (this.historyList.size > 0) {
            this.historyList.size - 1
        } else {
            0
        }
        this.historyList.addAll(newDataSet as ArrayList<ForSettlement>)
        notifyItemChanged(lastPosition)
        this.filterHistoryList = this.historyList
    }

    fun setTransactionType() {

    }

    fun clearDataset() {
        this.historyList.clear()
        this.filterHistoryList.clear()
        notifyDataSetChanged()
    }

    fun setServiceType(trxHistory: String) {
        this.trxHistory = trxHistory
    }
}

