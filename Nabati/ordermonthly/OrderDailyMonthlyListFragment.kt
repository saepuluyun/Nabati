package com.nabati.sfa.modul.ordermonthly

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.getSalesman
import com.nabati.sfa.helper.DateHelper
import com.nabati.sfa.helper.DateHelper.timestampToDateMonthYear
import com.nabati.sfa.model.orderdaily.OrderDailyMonthly
import com.nabati.sfa.model.orders.Orders
import com.nabati.sfa.model.product.Salesman
import kotlinx.android.synthetic.main.fragment_order_daily_monthly_list.view.*

class OrderDailyMonthlyListFragment : BaseFragment<OrderDailyMonthlyListPresenter>(),
    IOrderDailyMonthlyListView {

    private lateinit var mView: View
    private var salesman: Salesman? = null
    lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_order_daily_monthly_list, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): OrderDailyMonthlyListPresenter {
        return OrderDailyMonthlyListPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewInit()
        setupToolbar(getString(R.string.menu_view_order_monthly))
        initOnClickListener()
    }

    private fun initOnClickListener() {

    }

    override fun onViewInit() {
        salesman = getSalesman(requireContext())
        presenter?.getDataOrders(salesman?.salesmanId)

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSuccessGetDataOrders(orders: MutableList<Orders>?) {
        if (!isAdded)
            return
        if (!orders?.isNullOrEmpty()!!) {
            val ordersThisMonth = orders.filter {
                (timestampToDateMonthYear(it.orderDate) == DateHelper.dateFormatTodayMonthYear()) and (it.orderType.equals(
                    "direct-sale"
                ))
            }
            val orderThisMonthValue: MutableList<OrderDailyMonthly>? = mutableListOf()

            if (!ordersThisMonth.isNullOrEmpty()) {
                for (order in ordersThisMonth) {
                    orderThisMonthValue?.add(
                        OrderDailyMonthly(
                            orderDate = order.orderDate,
                            outletId = order.outletId,
                            outletName = order.customerName,
                            discountInvoiceLastMonth = 0F,
                            discountPromoLastMonth = 0F,
                            discountRegularLastMonth = 0F,
                            freeGoodLastMonth = 0F,
                            grossLastMonth = 0F,
                            nettPriceLastMonth = 0F,
                            taxValueLastMonth = 0F,
                            discountInvoiceThisMonth = order.discountInvoice,
                            discountPromoThisMonth = order.discountPromoTotal,
                            discountRegularThisMonth = order.discountRegular,
                            freeGoodThisMonth = order.discountFreegood,
                            grossThisMonth = order.gross,
                            nettPriceThisMonth = order.nettPrice,
                            taxValueThisMonth = order.taxTotal
                        )
                    )
                }
            }

            val ordersLastMonth = orders.filter {
                (timestampToDateMonthYear(it.orderDate) == DateHelper.dateFormatLastMonthYear()) and (it.orderType.equals(
                    "direct-sale"
                ))
            }

            if (!ordersLastMonth.isNullOrEmpty()) {
                for (order in ordersLastMonth) {
                    orderThisMonthValue?.add(
                        OrderDailyMonthly(
                            orderDate = order.orderDate,
                            outletId = order.outletId,
                            outletName = order.customerName,
                            discountInvoiceLastMonth = order.discountInvoice,
                            discountPromoLastMonth = order.discountPromoTotal,
                            discountRegularLastMonth = order.discountRegular,
                            freeGoodLastMonth = order.discountFreegood,
                            grossLastMonth = order.gross,
                            nettPriceLastMonth = order.nettPrice,
                            taxValueLastMonth = order.taxTotal,
                            discountInvoiceThisMonth = 0F,
                            discountPromoThisMonth = 0F,
                            discountRegularThisMonth = 0F,
                            freeGoodThisMonth = 0F,
                            grossThisMonth = 0F,
                            nettPriceThisMonth = 0F,
                            taxValueThisMonth = 0F
                        )
                    )
                }
            }

            val sumOfOrder =
                orderThisMonthValue?.groupBy {
                    it.outletId
                }?.values?.map {
                    it.reduce { acc, item ->
                        OrderDailyMonthly(
                            item.orderDate,
                            item.outletId,
                            item.outletName,
                            acc.discountInvoiceLastMonth?.plus(item.discountInvoiceLastMonth!!),
                            acc.discountPromoLastMonth?.plus(item.discountPromoLastMonth!!),
                            acc.discountRegularLastMonth?.plus(item.discountRegularLastMonth!!),
                            acc.freeGoodLastMonth?.plus(item.freeGoodLastMonth!!),
                            acc.grossLastMonth?.plus(item.grossLastMonth!!),
                            acc.nettPriceLastMonth?.plus(item.nettPriceLastMonth!!),
                            acc.taxValueLastMonth?.plus(item.taxValueLastMonth!!),
                            acc.discountInvoiceThisMonth?.plus(item.discountInvoiceThisMonth!!),
                            acc.discountPromoThisMonth?.plus(item.discountPromoThisMonth!!),
                            acc.discountRegularThisMonth?.plus(item.discountRegularThisMonth!!),
                            acc.freeGoodThisMonth?.plus(item.freeGoodThisMonth!!),
                            acc.grossThisMonth?.plus(item.grossThisMonth!!),
                            acc.nettPriceThisMonth?.plus(item.nettPriceThisMonth!!),
                            acc.taxValueThisMonth?.plus(item.taxValueThisMonth!!)
                        )
                    }
                }

            if (!sumOfOrder.isNullOrEmpty()) {
                mView.tvEmptyData.visibility = View.GONE
                val adapter = ViewOrderDailyMonthlyAdapter(requireContext())
                mView.rvOrderDailyMonthly.layoutManager = LinearLayoutManager(requireContext())
                mView.rvOrderDailyMonthly.setHasFixedSize(true)
                mView.rvOrderDailyMonthly.isNestedScrollingEnabled = false
                mView.rvOrderDailyMonthly.adapter = adapter
                adapter.addAll(sumOfOrder)
                adapter.setOnItemClickListener { _, _ -> }

                onFinishLoad()
            } else {
                mView.tvEmptyData.visibility = View.VISIBLE
            }
        } else
            mView.tvEmptyData.visibility = View.VISIBLE
    }

    override fun onError(message: String?) {
        showError(message)
    }

    override fun onStartLoad() {
        mView.pbLoading.visibility = View.VISIBLE
    }

    override fun onFinishLoad() {
        mView.pbLoading.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_customer, menu)

        menu.findItem(R.id.action_add_customer).isVisible = false
        menu.findItem(R.id.action_save_customer).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback.remove()
    }

}