package com.nabati.sfa.modul.ordermonthly

import com.nabati.sfa.base.IBaseView
import com.nabati.sfa.model.customer.Customer
import com.nabati.sfa.model.journey.SalesmanJourneys
import com.nabati.sfa.model.orders.Orders
import com.nabati.sfa.model.product.Salesman
import com.nabati.sfa.model.product.WorkFlows

interface IOrderDailyMonthlyListView : IBaseView {

    fun onSuccessGetDataOrders(orders: MutableList<Orders>?) {}

}