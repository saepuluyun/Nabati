package com.nabati.sfa.modul.takingorder.summarytakingorder

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.journey.SalesmanJourneyTracks
import com.nabati.sfa.model.product.WorkFlows

interface ISummaryOrderTakingOrderView: IBaseView {

    fun onSuccessGetData(workFlows: WorkFlows?) {}
    fun onSuccessGetDataJourneyTrack(salesmanJourneyTracks: SalesmanJourneyTracks?) {}
}