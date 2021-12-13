package com.nabati.sfa.modul.takingorder.suggesttakingorder

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.product.WorkFlows

interface ISuggestTakingOrderView: IBaseView {

    fun onSuccessRequest(workFlows: WorkFlows?) {}

}