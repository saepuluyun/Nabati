package com.nabati.sfa.modul.orderdaily

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.getSalesman
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.helper.DateHelper
import com.nabati.sfa.helper.DateHelper.timestampToDate
import com.nabati.sfa.model.orders.Orders
import com.nabati.sfa.model.product.Salesman
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_order_daily_list.view.*

class OrderDailyListFragment : BaseFragment<OrderDailyListPresenter>(),
    IOrderDailyListView {

    private lateinit var mView: View
    private var orders: MutableList<Orders>? = mutableListOf()
    private var salesman: Salesman? = null
    private val compositeDisposable = CompositeDisposable()
    private var totalDirectSelling: Float = 0F
    private var totalTakingOrder: Float = 0F
    private var totalCash: Float = 0F
    private var totalTransfer: Float = 0F
    private var totalGiro: Float = 0F
    private var totalCreditNote: Float = 0F
    private var found: Boolean = false
    lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_order_daily_list, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): OrderDailyListPresenter {
        return OrderDailyListPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewInit()
        setupToolbar(getString(R.string.menu_view_order_daily))
        initOnClickListener()
    }

    private fun initOnClickListener() {

    }

    override fun onViewInit() {
        salesman = getSalesman(requireContext())
        presenter?.getDataWorkFlows(salesman?.salesmanId)
        totalTakingOrder = 0F
        totalDirectSelling = 0F
        totalCash = 0F
        totalTransfer = 0F
        totalCreditNote = 0F
        totalGiro = 0F

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
        if (!orders?.isNullOrEmpty()!!) {
            val ordersByDate = orders.filter {
                val date = timestampToDate(it.orderDate)
                val date2 = DateHelper.dateFormatToday()
                date == date2
            }

            if (!ordersByDate.isNullOrEmpty()) {
                if (!this.orders.isNullOrEmpty()) {
                    this.orders?.clear()
                    this.orders = ordersByDate.toMutableList()
                } else
                    this.orders = ordersByDate.toMutableList()

                initDataOutlet()
            } else {
                mView.tvEmptyData.visibility = View.VISIBLE
            }
        } else
            mView.tvEmptyData.visibility = View.VISIBLE
    }

    private fun initDataOutlet() {
        if (!isAdded)
            return

        if (!orders.isNullOrEmpty()) {

            for (order in orders!!) {
                totalCash += order.cash!!
                totalTransfer += order.transfer!!
                totalGiro += order.giro!!
                totalCreditNote += order.creditNote!!
                if (order.orderType == "taking-order") {
                    totalTakingOrder += order.nettPrice!!
                }
                if (order.orderType == "direct-sale") {
                    totalDirectSelling += order.nettPrice!!
                }
            }

            mView.tvCash.text = rupiahCurrency(totalCash)
            mView.tvTransfer.text = rupiahCurrency(totalTransfer)
            mView.tvGiro.text = rupiahCurrency(totalGiro)
            mView.tvCreditNote.text = rupiahCurrency(totalCreditNote)
            mView.tvTotalDirectSales.text = rupiahCurrency(totalDirectSelling)
            mView.tvTotalTakingOrder.text = rupiahCurrency(totalTakingOrder)

            val adapterTakingOrder = ViewOrderDailyTakingOrderAdapter(requireContext())
            mView.rvOrderDailyTakingOrder.layoutManager = LinearLayoutManager(requireContext())
            mView.rvOrderDailyTakingOrder.setHasFixedSize(true)
            mView.rvOrderDailyTakingOrder.isNestedScrollingEnabled = false
            mView.rvOrderDailyTakingOrder.adapter = adapterTakingOrder
            adapterTakingOrder.addAll(orders)
            adapterTakingOrder.setOnItemClickListener { _, position ->
                val bundle = bundleOf(
                    Pair("orders", adapterTakingOrder.getItem(position)),
                    Pair("from", adapterTakingOrder.getItem(position).orderType)
                )
                Navigation.findNavController(
                    requireActivity(),
                    R.id.navHostFragment
                )
                    .navigate(
                        R.id.action_to_view_detail_order_daily,
                        bundle
                    )
            }
        } else
            mView.tvEmptyData.visibility = View.VISIBLE
        onFinishLoad()
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
        compositeDisposable.clear()
        callback.remove()
    }

}