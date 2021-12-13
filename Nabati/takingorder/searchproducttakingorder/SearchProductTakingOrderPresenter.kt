package com.nabati.sfa.modul.takingorder.searchproducttakingorder

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.product.ProductTakingOrder

class SearchProductTakingOrderPresenter(context: Context, iView: ISearchProductTakingOrderView) :
    BasePresenter<ISearchProductTakingOrderView>(context, iView) {

    private var brandsList: MutableList<ProductTakingOrder>? = mutableListOf()

    fun getData() {
        iView.onStartLoad()
        getFireStoreHelper().collection("products")
            .addSnapshotListener { documentSnapshots, e ->
                if (e != null) {
                    iView.onError("Failure get data products")
                    iView.onFinishLoad()
                } else
                    if (documentSnapshots != null) {
                        iView.onFinishLoad()
                        for (document in documentSnapshots) {
                            brandsList?.add(document.toObject(ProductTakingOrder::class.java))
                        }
                        iView.onSuccessRequest(brandsList)
                    }
            }

    }

}