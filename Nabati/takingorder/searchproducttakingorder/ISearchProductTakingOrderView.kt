package com.nabati.sfa.modul.takingorder.searchproducttakingorder

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.product.Product
import com.nabati.sfa.model.product.ProductBrand
import com.nabati.sfa.model.product.ProductTakingOrder
import com.nabati.sfa.model.product.WorkFlows

interface ISearchProductTakingOrderView : IBaseView {

    fun onSuccessRequest(productTakingOrder: MutableList<ProductTakingOrder>?) {}

}