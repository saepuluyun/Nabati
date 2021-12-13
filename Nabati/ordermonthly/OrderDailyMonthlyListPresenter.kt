package com.nabati.sfa.modul.ordermonthly

import android.content.Context
import com.nabati.sfa.base.BasePresenter
import com.nabati.sfa.model.orders.Orders

class OrderDailyMonthlyListPresenter(context: Context, iView: IOrderDailyMonthlyListView) :
    BasePresenter<IOrderDailyMonthlyListView>(context, iView) {

    private var orders: MutableList<Orders>? = mutableListOf()

    fun getDataOrders(salesmanId: String?) {
        orders?.clear()
        getFireStoreHelper().collection("orders").whereEqualTo("salesmanId", salesmanId)
            .addSnapshotListener { documentSnapshots, e ->
                if (e != null) {
                    iView.onError("Failure get data customer")
                    iView.onFinishLoad()
                } else
                    if (documentSnapshots != null) {
                        iView.onFinishLoad()
                        for (document in documentSnapshots) {
                            orders?.add(document.toObject(Orders::class.java))
                        }
                        iView.onSuccessGetDataOrders(orders)
                    }
            }
    }

}