package com.nabati.sfa.modul.takingorder.summarytakingorder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.product.FreeGoodProduct
import kotlinx.android.synthetic.main.list_direct_selling.view.*

class SummaryFreeGoodsAdapter(context: Context) :
    BaseListAdapter<FreeGoodProduct, BaseViewHolder<*>>(context) {

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
    ) : BaseViewHolder<FreeGoodProduct>(view, onItemClickListener) {

        override fun bind(item: FreeGoodProduct?) {
            view.tvProductName.text = "* ${item?.productName}"
            view.tvProductId.text = item?.productId
            if (item?.price!! > 0) {
                view.tvProductPrice.text = rupiahCurrency(item?.price)
                view.tvProductPrice.visibility = View.VISIBLE
            } else {
                view.tvProductPrice.visibility = View.GONE
            }
            view.tvProductQuantity.text = "${item?.qty}"
            view.tvProductType.text = item?.uomPackage?.toUpperCase()
        }

    }

}