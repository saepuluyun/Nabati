package com.nabati.sfa.modul.takingorder.suggesttakingorder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.product.Lines
import kotlinx.android.synthetic.main.list_direct_selling.view.*

class SuggestTakingOrderAdapter(context: Context) :
    BaseListAdapter<Lines, BaseViewHolder<*>>(context) {

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

    inner class ViewHolder internal constructor(
        val view: View,
        onItemClickListener: OnItemClickListener
    ) : BaseViewHolder<Lines>(view, onItemClickListener) {

        override fun bind(item: Lines?) {
            view.tvProductName.text = item?.product?.productName
            view.tvProductId.text = item?.product?.productId
            view.tvProductPrice.text =
                "${rupiahCurrency(item?.price)} / ${item?.uomPackage?.toUpperCase()}"
            view.tvProductQuantity.text = "${item?.qty}"
            view.tvProductType.text = item?.uomPackage?.toUpperCase()
        }

    }

}