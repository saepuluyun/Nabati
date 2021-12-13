package com.nabati.sfa.modul.takingorder.searchproducttakingorder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.product.Product
import com.nabati.sfa.model.product.ProductTakingOrder
import kotlinx.android.synthetic.main.list_product.view.*

class SearchProductTakingOrderAdapter(context: Context) : BaseListAdapter<ProductTakingOrder, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_product
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ViewHolder(getView(parent, viewType), onItemClickListener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is ViewHolder) {
            super.onBindViewHolder(holder, position)
        }
    }

    inner class ViewHolder internal constructor(
        val view: View,
        onItemClickListener: OnItemClickListener
    ) : BaseViewHolder<ProductTakingOrder>(view, onItemClickListener) {

        override fun bind(item: ProductTakingOrder?) {
            view.tvProductId.text = item?.productId
            view.tvProductName.text = item?.productName
            view.tvProductPrice.text = "${rupiahCurrency(item?.price?.lrg)} / ${item?.unitsName?.lrg?.toUpperCase()}"
        }

    }

}