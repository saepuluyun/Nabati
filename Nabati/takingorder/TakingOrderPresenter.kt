package com.nabati.sfa.modul.takingorder

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.TransactionConfigs
import com.nabati.sfa.model.product.WorkFlows

class TakingOrderPresenter(context: Context, iView: ITakingOrderView) :
    BasePresenter<ITakingOrderView>(context, iView) {

    fun getData() {
        iView.onStartLoad()
        getWorkFlowDocument().addSnapshotListener { documentSnapshots, e ->
            if (e != null) {
                iView.onError("Failure get data")
                iView.onFinishLoad()
            } else
                if (documentSnapshots != null) {
                    iView.onFinishLoad()
                    iView.onSuccessRequest(documentSnapshots?.toObject(WorkFlows::class.java))
                }
        }
    }

    fun getTransactionConfigs(countryId: String?) {
        iView.onStartLoad()
        getFireStoreHelper().collection("transactionConfigs").document("$countryId")
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null) {
                    iView.onError("Failure get data")
                    iView.onFinishLoad()
                } else
                    if (documentSnapshot != null) {
                        iView.onFinishLoad()
                        iView.onSuccessGetTransactionConfigs(
                            documentSnapshot.toObject(
                                TransactionConfigs::class.java
                            )
                        )
                    }
            }
    }


}