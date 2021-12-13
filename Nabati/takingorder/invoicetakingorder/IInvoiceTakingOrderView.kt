package com.nabati.sfa.modul.takingorder.invoicetakingorder

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.customer.Customer
import com.nabati.sfa.model.product.WorkFlows
import com.nabati.sfa.modul.promoactivity.InvoiceWithCalculatedDiscount

interface IInvoiceTakingOrderView: IBaseView {

    fun onSuccessRequest(workFlows: WorkFlows?) {}
    fun onSuccessGetDataCustomer(customers: MutableList<Customer>?) {}
    fun onSuccessCalculateInvoice(calculatedDiscount: InvoiceWithCalculatedDiscount?) {}

}