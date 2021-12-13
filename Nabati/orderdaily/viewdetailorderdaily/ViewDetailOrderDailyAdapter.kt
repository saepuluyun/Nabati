package com.nabati.sfa.modul.orderdaily.viewdetailorderdaily

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.product.Lines
import kotlinx.android.synthetic.main.list_invoice.view.*

class ViewDetailOrderDailyAdapter(context: Context) :
    BaseListAdapter<Lines, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_invoice
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return ViewHolder(getView(parent, viewType), onItemClickListener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        if (holder is ViewDetailOrderDailyAdapter.ViewHolder) {
            super.onBindViewHolder(holder, position)
        }
    }

    inner class ViewHolder internal constructor(
        val view: View,
        onItemClickListener: OnItemClickListener
    ) : BaseViewHolder<Lines>(view, onItemClickListener) {

        override fun bind(item: Lines?) {
            view.tvProductName.text = item?.product?.productName
            view.tvProductPrice.text = rupiahCurrency(item?.price)
            view.tvUom.text = "${item?.qty} ${item?.uomPackage?.toUpperCase()}"
            if (item?.calculatedPromoText.isNullOrEmpty()) {
                view.tvDiscount.visibility = View.GONE
            } else {
                view.tvDiscount.visibility = View.VISIBLE
                view.tvDiscount.text = item?.calculatedPromoText
            }
        }

    }

}