package com.nabati.sfa.modul.orderdaily

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.product.Invoices
import com.nabati.sfa.modul.orderdaily.viewdetailorderdaily.CalculateInvoiceOrderDailyAdapter
import com.nabati.sfa.modul.scheduleoutlet.invoice.TaxesAdapter
import kotlinx.android.synthetic.main.list_view_order_daily.view.*

class ViewOrderDailyDirectSellingAdapter(context: Context) : BaseListAdapter<Invoices, BaseViewHolder<*>>(context) {

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
    ) : BaseViewHolder<Invoices>(view, onItemClickListener) {

        override fun bind(item: Invoices?) {
            view.tvOutletId.text = item?.outlet?.outletId
            view.tvOutletName.text = item?.outlet?.outletName
            view.tvGross.text = "${rupiahCurrency(item?.gross)}"

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

            view.tvType.text = "Direct Sales"

        }

    }
}