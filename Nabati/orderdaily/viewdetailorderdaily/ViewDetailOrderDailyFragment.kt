package com.nabati.sfa.modul.orderdaily.viewdetailorderdaily

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.model.orders.Orders
import com.nabati.sfa.model.product.Lines
import com.nabati.sfa.modul.promoactivity.DiscountAggregate
import com.nabati.sfa.modul.scheduleoutlet.invoice.TaxesAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_view_detail_order_daily.view.*

class ViewDetailOrderDailyFragment : BaseFragment<ViewDetailOrderDailyPresenter>(),
    IViewDetailOrderDailyView {

    private lateinit var mView: View
    private val compositeDisposable = CompositeDisposable()
    private var order: Orders? = null
    private var lines: MutableList<Lines>? = null
    private var from: String? = ""
    lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_view_detail_order_daily, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): ViewDetailOrderDailyPresenter {
        return ViewDetailOrderDailyPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewInit()
        setupToolbar(order?.customerName)
    }

    override fun onViewInit() {
        arguments?.let {
            val args = ViewDetailOrderDailyFragmentArgs.fromBundle(it)
            order = args.orders
            from = args.from
            if (from == "direct-sale") {
                lines = order?.lines
                initAdapterListProduct(lines)
                iniAdapterCalculate(order?.discountAggregate)
                initDataInvoice(order)
            } else if (from == "taking-order") {
                lines = order?.lines
                initAdapterListProduct(lines)
                iniAdapterCalculate(order?.discountAggregate)
                initDataOrder(order)
            }
            onFinishLoad()
        }

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        mView.tvType.text =
            if (order?.orderType == "taking-order") "Taking Order" else "Direct Sales"

    }

    private fun iniAdapterCalculate(discountAggregate: MutableList<DiscountAggregate>?) {
        val adapter = CalculateInvoiceOrderDailyAdapter(requireContext())
        mView.rvCalculateDiscount.layoutManager = LinearLayoutManager(requireContext())
        mView.rvCalculateDiscount.setHasFixedSize(true)
        mView.rvCalculateDiscount.isNestedScrollingEnabled = false
        mView.rvCalculateDiscount.adapter = adapter
        adapter.addAll(discountAggregate)
        adapter.setOnItemClickListener { _, _ -> }
    }

    private fun initDataOrder(order: Orders?) {
        mView.tvTotalItem.text = "${order?.lines?.size}"
        mView.tvGrossPrice.text = rupiahCurrency(order?.subTotal)

        val taxesAdapter = TaxesAdapter(requireContext())
        mView.rvTaxes.layoutManager = LinearLayoutManager(requireContext())
        mView.rvTaxes.setHasFixedSize(true)
        mView.rvTaxes.adapter = taxesAdapter
        mView.rvTaxes.isNestedScrollingEnabled = false
        taxesAdapter.addAll(order?.taxes)
        taxesAdapter.setOnItemClickListener { _, _ -> }

        mView.tvNetPrice.text = rupiahCurrency(order?.nettPrice)
        mView.llCreditNotePayment.visibility = View.GONE
        mView.llCashPayment.visibility = View.GONE
        mView.llTransferPayment.visibility = View.GONE
        mView.llMoneyChanges.visibility = View.GONE
    }

    private fun initDataInvoice(orders: Orders?) {
        mView.tvTotalItem.text = "${orders?.lines?.size}"
        mView.tvGrossPrice.text = rupiahCurrency(orders?.subTotal)

        val taxesAdapter = TaxesAdapter(requireContext())
        mView.rvTaxes.layoutManager = LinearLayoutManager(requireContext())
        mView.rvTaxes.setHasFixedSize(true)
        mView.rvTaxes.adapter = taxesAdapter
        mView.rvTaxes.isNestedScrollingEnabled = false
        taxesAdapter.addAll(orders?.taxes)
        taxesAdapter.setOnItemClickListener { _, _ -> }

        mView.tvNetPrice.text = rupiahCurrency(orders?.nettPrice)
        mView.tvCreditNotePrice.text = rupiahCurrency(orders?.creditNote)
        mView.tvCashPrice.text = rupiahCurrency(orders?.cash)
        mView.tvTransferPrice.text = rupiahCurrency(orders?.transfer)
        mView.llCreditNotePayment.visibility = View.VISIBLE
        mView.llCashPayment.visibility = View.VISIBLE
        mView.llTransferPayment.visibility = View.VISIBLE
        mView.llMoneyChanges.visibility = View.VISIBLE
    }

    private fun initAdapterListProduct(lines: MutableList<Lines>?) {
        val adapter = ViewDetailOrderDailyAdapter(requireActivity())
        mView.rvDetailInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvDetailInvoice.setHasFixedSize(true)
        mView.rvDetailInvoice.adapter = adapter
        mView.rvDetailInvoice.isNestedScrollingEnabled = false
        adapter.addAll(lines)
        adapter.setOnItemClickListener { _, _ -> }

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
