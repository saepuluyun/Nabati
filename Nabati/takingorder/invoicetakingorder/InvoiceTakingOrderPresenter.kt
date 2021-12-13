package com.nabati.sfa.modul.takingorder.invoicetakingorder

import android.content.Context
import com.nabati.sfa.R
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.CalculateInvoiceQuery
import com.nabati.sfa.model.customer.Customer
import com.nabati.sfa.model.product.WorkFlows
import com.nabati.sfa.modul.promoactivity.InvoiceWithCalculatedDiscount
import com.nabati.sfa.network.ResponseObserver
import com.nabati.sfa.network.tryCall
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class InvoiceTakingOrderPresenter(context: Context, iView: IInvoiceTakingOrderView) :
    BasePresenter<IInvoiceTakingOrderView>(context, iView) {

    fun getData() {
        iView.onStartLoad()

        getWorkFlowDocument().addSnapshotListener { documentSnapshots, e ->
            if (e != null) {
                iView.onError("Failure get data")
                iView.onFinishLoad()
            } else
                if (documentSnapshots != null) {
                    iView.onSuccessRequest(documentSnapshots?.toObject(WorkFlows::class.java))
                }
        }
    }

    private var customers: MutableList<Customer>? = mutableListOf()

    fun getDataCustomer(outletId: String?) {
        getFireStoreHelper().collection("customers").whereEqualTo("outletId", outletId)
            .addSnapshotListener { documentSnapshots, e ->
                if (e != null) {
                    iView.onError("Failure get data")
                    iView.onFinishLoad()
                } else
                    if (documentSnapshots != null) {
                        iView.onFinishLoad()
                        for (document in documentSnapshots) {
                            customers?.add(document.toObject(Customer::class.java))
                        }
                        iView.onSuccessGetDataCustomer(customers)
                    }
            }
    }

    fun calculateInvoice(invoiceQuery: CalculateInvoiceQuery) {
        if (!isNetworkAvailable()) {
            iView.onError(context?.resources?.getString(R.string.no_connection))
            iView.onFinishLoad()
            return
        }

        val request = tryCall(service?.calculateInvoice(invoiceQuery))
        request.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : ResponseObserver<InvoiceWithCalculatedDiscount?>(iView) {
                override fun onComplete() {
                    dismissAndDispose()
                }

                override fun onNext(result: InvoiceWithCalculatedDiscount?) {
                    iView.onSuccessCalculateInvoice(result)
                }

                override fun onError(e: Throwable) {
                    super.onError(e)
                    dismissAndDispose()
                }
            })


    }


}