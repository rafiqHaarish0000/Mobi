package com.mobiversa.ezy2pay.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobiversa.ezy2pay.databinding.ProductListDataItemBinding
import com.mobiversa.ezy2pay.network.response.ProductList
import com.mobiversa.ezy2pay.ui.home.HomeFragment

class ProductListAdapter(
    private val productList: List<ProductList>,
    private val context: Context,
    private val homeFragment: HomeFragment
) : RecyclerView.Adapter<ProductListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProductListViewHolder(
            ProductListDataItemBinding.inflate(
                layoutInflater,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productList.size
    }


    override fun onBindViewHolder(holder: ProductListViewHolder, position: Int) {
        val item = productList[position]
        item.let { product ->
            (holder as ProductListViewHolder).bind(item, context, homeFragment)
            holder.itemView.setOnClickListener {

                Log.e(TAG, "onBindViewHolder: Clicked", )
                if (product.isEnable)
                    homeFragment.productDetails(product.productName)
                else
                    Toast.makeText(
                        context, "You are not Subscribed for ${product.productName}",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }

    }

}

