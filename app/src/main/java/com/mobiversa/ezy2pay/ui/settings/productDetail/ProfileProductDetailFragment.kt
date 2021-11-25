package com.mobiversa.ezy2pay.ui.settings.productDetail

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.mobiversa.ezy2pay.MainActivity
import com.mobiversa.ezy2pay.R
import com.mobiversa.ezy2pay.base.BaseFragment
import com.mobiversa.ezy2pay.utils.Constants
import com.mobiversa.ezy2pay.utils.Constants.Companion.EzyMoto
import com.mobiversa.ezy2pay.utils.Constants.Companion.Product
import kotlinx.android.synthetic.main.fragment_profile_product_detail.view.*

/**
 * A simple [Fragment] subclass.
 */
class ProfileProductDetailFragment : BaseFragment() {

    var deviceId = ""
    var tidStr = ""
    var ezyAuthEnabled = ""
    var expiryDateStr = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_profile_product_detail, container, false)

        // Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Detail", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        initialize(requireArguments().getString(Product), rootView)
        return rootView
    }

    @SuppressLint("SetTextI18n")
    private fun initialize(productName: String?, rootView: View) {
        getProductList().find { it.productName.equals(productName, ignoreCase = true) }?.let { productDetail ->
            rootView.prof_product_img.setImageResource(productDetail.productImage)
            rootView.txt_prod_name.text = productDetail.productName
        }
        when (productName) {
            Constants.Ezywire -> {
                rootView.prod_desc.text = "EZYWIRE is a Mobile Point of Sale \n" +
                    "Terminal for merchants who \n" +
                    "want accept card payments."
                deviceId = getProductResponse().deviceId
                tidStr = getProductResponse().tid
                ezyAuthEnabled = getProductResponse().enableEzywire
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.EzyMoto -> {
                rootView.txt_prod_name.text = Constants.EzyMoto
                if (getLoginResponse().auth3DS.equals("Yes", true)) {
                    rootView.prof_product_img.setImageResource(R.drawable.ezylink_blue_icon)
                } else
                    rootView.prof_product_img.setImageResource(R.drawable.ezymoto_blue_icon)
                deviceId = getProductResponse().motoMid
                tidStr = getProductResponse().motoTid
                ezyAuthEnabled = getProductResponse().enableMoto
                expiryDateStr = getProductResponse().ezypassDeviceExpiry

                rootView.prod_desc.text = EzyMoto + " enables merchants to \n" +
                    "accept cashless and contactless \n" +
                    "payments without the merchant \n" +
                    "having a website."
            }
            Constants.EzyRec -> {
                rootView.prod_desc.text = "Automate periodic payments so you do not have to chasepayments. Save your time and effort and stay focused on what matters."
                deviceId = getProductResponse().ezyrecMid
                tidStr = getProductResponse().ezyrecTid
                ezyAuthEnabled = getProductResponse().enableEzyrec
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.EzyAuth -> {
                rootView.prod_desc.text = "Stay Safe and Secure by \n" +
                    "accepting authorised Payments \n" +
                    "from your customer and never \n" +
                    "lose another dime from fraud. \n" +
                    "Stay on top of your game with \n" +
                    "EZYAUTH by minimizing risk."
                deviceId = getProductResponse().motoMid
                tidStr = getProductResponse().motoTid
                ezyAuthEnabled = getProductResponse().enableMoto
                expiryDateStr = getProductResponse().ezypassDeviceExpiry
            }
            Constants.Boost -> {
                rootView.prof_product_img.setImageResource(R.drawable.boost)
                rootView.prod_desc.text = ""
                deviceId = getProductResponse().mid
                tidStr = getProductResponse().tid
                ezyAuthEnabled = "YES"
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.GrabPay -> {
                rootView.prod_desc.text = ""
                deviceId = getProductResponse().mid
                tidStr = getProductResponse().tid
                ezyAuthEnabled = "YES"
                expiryDateStr = getProductResponse().deviceExpiry
            }
            Constants.MobiPass -> {
                rootView.prod_desc.text = ""
                deviceId = getProductResponse().ezypassMid
                tidStr = getProductResponse().ezypassTid
                ezyAuthEnabled = getProductResponse().enableEzypass
                expiryDateStr = getProductResponse().ezypassDeviceExpiry
            }
            Constants.EzySplit -> {
                rootView.prod_desc.text = ""
                deviceId = ""
                tidStr = ""
                ezyAuthEnabled = ""
                expiryDateStr = ""
            }
            Constants.MobiCash -> {
                rootView.prod_desc.text = ""
                deviceId = ""
                tidStr = ""
                ezyAuthEnabled = ""
                expiryDateStr = ""
            }
        }

        rootView.txt_prod_detail_device_id.text = deviceId
        rootView.txt_prod_detail_tid.text = tidStr
        rootView.txt_prod_detail_ezyauth_enabled.text = ezyAuthEnabled
        rootView.txt_prod_detail_expiry_date.text = expiryDateStr
    }

    override fun onResume() {
        super.onResume()
        // Basic Setup
        (activity as MainActivity).supportActionBar?.title = "Product Details"
        setTitle("Product Detail", false)
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = activity as? MainActivity
        return when (item.itemId) {
            android.R.id.home -> {
                setTitle("Product Details", true)
                (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
                fragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
