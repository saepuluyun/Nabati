package com.nabati.sfa.modul.takingorder.inputproducttakingorder

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.helper.LoggerHelper
import com.nabati.sfa.model.product.WorkFlows


class InputProductTakingOrderPresenter(context: Context, iView: IInputProductTakingOrderView) :
    BasePresenter<IInputProductTakingOrderView>(context, iView) {

    fun inputProduct() {

        val workFlows = WorkFlows()
        workFlows.approvalId = "1234322"

        getWorkFlowDocument().set(workFlows)
            .addOnSuccessListener {
                LoggerHelper.error("Success")
            }

    }

}