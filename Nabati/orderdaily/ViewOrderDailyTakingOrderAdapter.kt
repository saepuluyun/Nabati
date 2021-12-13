package com.nabati.sfa.modul.orderdaily

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.orders.Orders
import com.nabati.sfa.modul.orderdaily.viewdetailorderdaily.CalculateInvoiceOrderDailyAdapter
import com.nabati.sfa.modul.scheduleoutlet.invoice.TaxesAdapter
import kotlinx.android.synthetic.main.list_view_order_daily.view.*

class ViewOrderDailyTakingOrderAdapter(context: Context) :
    BaseListAdapter<Orders, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_view_order_daily
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
    ) : BaseViewHolder<Orders>(view, onItemClickListener) {

        override fun bind(item: Orders?) {
            view.tvOutletId.text = item?.outletId
            view.tvOutletName.text = item?.customerName
            view.tvGross.text = rupiahCurrency(item?.subTotal)

            val taxesAdapter = TaxesAdapter(context)
            view.rvTaxes.layoutManager = LinearLayoutManager(context)
            view.rvTaxes.setHasFixedSize(true)
            view.rvTaxes.adapter = taxesAdapter
            view.rvTaxes.isNestedScrollingEnabled = false
            taxesAdapter.addAll(item?.taxes)
            taxesAdapter.setOnItemClickListener { _, _ -> }

            view.tvNetPrice.text = "${rupiahCurrency(item?.nettPrice)}"

            val adapter = CalculateInvoiceOrderDailyAdapter(context)
            view.rvCalculateDiscount.layoutManager = LinearLayoutManager(context)
            view.rvCalculateDiscount.setHasFixedSize(true)
            view.rvCalculateDiscount.isNestedScrollingEnabled = false
            view.rvCalculateDiscount.adapter = adapter
            adapter.addAll(item?.discountAggregate)
            adapter.setOnItemClickListener { _, _ -> }

            view.tvType.text = if (item?.orderType == "taking-order") "Taking Order" else "Direct Sales"
        }

    }
}