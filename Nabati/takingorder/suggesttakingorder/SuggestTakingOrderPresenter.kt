package com.nabati.sfa.modul.takingorder.suggesttakingorder

import android.content.Context
import android.util.Log
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.extention.getWorkflowId
import com.nabati.sfa.model.product.WorkFlows

class SuggestTakingOrderPresenter(context: Context, iView: ISuggestTakingOrderView) :
    BasePresenter<ISuggestTakingOrderView>(context, iView) {

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

}