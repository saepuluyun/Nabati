package com.nabati.sfa.modul.orderdaily.viewdetailorderdaily

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.modul.promoactivity.DiscountItem
import kotlinx.android.synthetic.main.list_discount_item.view.*

class DiscountItemOrderDailyAdapter(context: Context) :
    BaseListAdapter<DiscountItem, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_discount_item
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
    ) : BaseViewHolder<DiscountItem>(view, onItemClickListener) {

        override fun bind(item: DiscountItem?) {
            view.tvLabelInvoice.text = item?.discountLabel
            view.tvLabelInvoiceValue.text = item?.discountValueLabel
            view.tvInvoiceValue.text = rupiahCurrency(item?.discountValue?.toFloat())
        }

    }

}