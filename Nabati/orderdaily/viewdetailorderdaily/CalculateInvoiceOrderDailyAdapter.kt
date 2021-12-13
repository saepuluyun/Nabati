package com.nabati.sfa.modul.orderdaily.viewdetailorderdaily

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.modul.promoactivity.DiscountAggregate
import kotlinx.android.synthetic.main.list_calculate_invoice.view.*

class CalculateInvoiceOrderDailyAdapter(context: Context) :
    BaseListAdapter<DiscountAggregate, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_calculate_invoice
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
    ) : BaseViewHolder<DiscountAggregate>(view, onItemClickListener) {

        override fun bind(item: DiscountAggregate?) {
            view.tvLabelInvoice.text = "Nett ${item?.level}"
            view.tvInvoiceValue.text = rupiahCurrency(item?.netTotal?.toFloat())

            val adapter = DiscountItemOrderDailyAdapter(context)
            view.rvDiscountItem.layoutManager = LinearLayoutManager(context)
            view.rvDiscountItem.setHasFixedSize(true)
            view.rvDiscountItem.adapter = adapter
            view.rvDiscountItem.isNestedScrollingEnabled = false
            adapter.addAll(item?.discountItems)
            adapter.setOnItemClickListener { view, position ->  }
        }

    }

}