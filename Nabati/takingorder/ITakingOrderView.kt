package com.nabati.sfa.modul.takingorder

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.TransactionConfigs
import com.nabati.sfa.model.product.WorkFlows

interface ITakingOrderView: IBaseView {

    fun onSuccessRequest(workFlows: WorkFlows?) {}
    fun onSuccessGetTransactionConfigs(transactionConfigs: TransactionConfigs?) {}

}