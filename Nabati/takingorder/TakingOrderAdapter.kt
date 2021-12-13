package com.nabati.sfa.modul.takingorder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.local.invoice.InvoiceDirectSelling
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.product.Product
import kotlinx.android.synthetic.main.list_direct_selling.view.*

class TakingOrderAdapter(context: Context) :
    BaseListAdapter<InvoiceTakingOrder, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_direct_selling
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ViewHolder(getView(parent, viewType), onItemClickListener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is ViewHolder) {
            super.onBindViewHolder(holder, position)
        }
    }

    fun updateData(data: MutableList<InvoiceTakingOrder>) {
        this.items.clear()
        this.items.addAll(data)
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(
        val view: View,
        onItemClickListener: OnItemClickListener
    ) : BaseViewHolder<InvoiceTakingOrder>(view, onItemClickListener) {

        override fun bind(item: InvoiceTakingOrder?) {
            val product = Gson().fromJson(item?.product, Product::class.java)
            view.tvProductName.text = product.productName
            view.tvProductId.text = product.productId
            view.tvProductPrice.text =
                "${rupiahCurrency(item?.price)} / ${item?.uomPackage?.toUpperCase()}"
            view.tvProductQuantity.text = "${item?.qty}"
            view.tvProductType.text = item?.uomPackage?.toUpperCase()
        }

    }

}