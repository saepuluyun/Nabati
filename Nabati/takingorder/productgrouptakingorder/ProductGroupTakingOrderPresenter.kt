package com.nabati.sfa.modul.takingorder.productgrouptakingorder

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.product.ProductBrand

class ProductGroupTakingOrderPresenter(context: Context, iView: IProductGroupTakingOrderView) :
    BasePresenter<IProductGroupTakingOrderView>(context, iView) {

    private var brandsList: MutableList<ProductBrand>? = mutableListOf()

    fun getDataProductGroup() {
        iView.onStartLoad()
        getFireStoreHelper().collection("brands")
            .addSnapshotListener { documentSnapshots, e ->
                if (e != null) {
                    iView.onError("Failure get data brands")
                    iView.onFinishLoad()
                } else
                    if (documentSnapshots != null) {
                        iView.onFinishLoad()
                        for (document in documentSnapshots) {
                            brandsList?.add(document.toObject(ProductBrand::class.java))
                        }
                        iView.onSuccessGetProductGroup(brandsList)
                    }
            }
    }
}