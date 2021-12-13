package com.nabati.sfa.modul.takingorder.productgrouptakingorder
import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.product.ProductBrand

interface IProductGroupTakingOrderView: IBaseView {

    fun onSuccessGetProductGroup(brands: MutableList<ProductBrand>?) {}

}