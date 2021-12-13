package com.nabati.sfa.modul.takingorder.summarytakingorder

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.journey.SalesmanJourneyTracks
import com.nabati.sfa.model.product.WorkFlows

class SummaryOrderTakingOrderPresenter(context: Context, iView: ISummaryOrderTakingOrderView) :
    BasePresenter<ISummaryOrderTakingOrderView>(context, iView) {

    fun getData() {
        iView.onStartLoad()
        getWorkFlowDocument().addSnapshotListener { documentSnapshots, e ->
            if (e != null) {
                iView.onError("Failure get data")
                iView.onFinishLoad()
            } else
                if (documentSnapshots != null) {
                    iView.onFinishLoad()
                    iView.onSuccessGetData(documentSnapshots?.toObject(WorkFlows::class.java))
                }
        }
    }

    fun getJourneyTrack() {
        getSalesmanJourneyTracks().addSnapshotListener { documentSnapshots, e ->
            if (e != null) {
                iView.onError("Failure get data journey tracks")
            } else
                if (documentSnapshots != null) {
                    iView.onFinishLoad()
                    iView.onSuccessGetDataJourneyTrack(
                        documentSnapshots?.toObject(
                            SalesmanJourneyTracks::class.java
                        )
                    )
                }
        }
    }
}