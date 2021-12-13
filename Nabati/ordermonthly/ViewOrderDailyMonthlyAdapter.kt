package com.nabati.sfa.modul.ordermonthly

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.orderdaily.OrderDailyMonthly
import kotlinx.android.synthetic.main.list_view_order_monthly.view.*

class ViewOrderDailyMonthlyAdapter(context: Context) :
    BaseListAdapter<OrderDailyMonthly, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_view_order_monthly
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
    ) : BaseViewHolder<OrderDailyMonthly>(view, onItemClickListener) {

        override fun bind(item: OrderDailyMonthly?) {
            view.tvOutletId.text = item?.outletId
            view.tvOutletName.text = item?.outletName
            view.tvGrossThisMonth.text = "${rupiahCurrency(item?.grossThisMonth)}"
            view.tvDiscInvoiceThisMonth.text = "${rupiahCurrency(item?.discountInvoiceThisMonth)}"
            view.tvDiscPromoThisMonth.text = "${rupiahCurrency(item?.discountPromoThisMonth)}"
            view.tvDiscRegThisMonth.text = "${rupiahCurrency(item?.discountRegularThisMonth)}"
            view.tvFreeGoodThisMonth.text = "${rupiahCurrency(item?.freeGoodThisMonth)}"
            view.tvPpnThisMonth.text = "${rupiahCurrency(item?.taxValueThisMonth)}"
            view.tvNettThisMonth.text = "${rupiahCurrency(item?.nettPriceThisMonth)}"

            view.tvGrossLastMonth.text = "${rupiahCurrency(item?.grossLastMonth)}"
            view.tvDiscInoviceLastMonth.text = "${rupiahCurrency(item?.discountInvoiceLastMonth)}"
            view.tvDiscPromoLastMonth.text = "${rupiahCurrency(item?.discountPromoLastMonth)}"
            view.tvDiscRegLastMonth.text = "${rupiahCurrency(item?.discountRegularLastMonth)}"
            view.tvFreeGoodLastMonth.text = "${rupiahCurrency(item?.freeGoodLastMonth)}"
            view.tvPpnLastMonth.text = "${rupiahCurrency(item?.taxValueLastMonth)}"
            view.tvNettLastMonth.text = "${rupiahCurrency(item?.nettPriceLastMonth)}"

        }

    }
}