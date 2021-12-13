package com.nabati.sfa.modul.takingorder.productgrouptakingorder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nabati.sfa.R
import com.nabati.sfa.base.adapter.BaseListAdapter
import com.nabati.sfa.base.adapter.BaseViewHolder
import com.nabati.sfa.model.product.ProductBrand
import kotlinx.android.synthetic.main.list_product_group.view.*

class ProductGroupTakingOrderAdapter(context: Context) : BaseListAdapter<ProductBrand, BaseViewHolder<*>>(context) {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemResourceLayout(viewType: Int): Int {
        return R.layout.list_product_group
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
    ) : BaseViewHolder<ProductBrand>(view, onItemClickListener) {

        override fun bind(item: ProductBrand?) {
            view.tvProductName.text = item?.productBrandName
        }

    }

}