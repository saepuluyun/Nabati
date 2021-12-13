package com.nabati.sfa.modul.takingorder.suggesttakingorder

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.helper.DateHelper.dateFormatTomorrow
import com.nabati.sfa.helper.LoggerHelper
import com.nabati.sfa.helper.SharedHelper
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrderViewModel
import com.nabati.sfa.model.product.ScheduledPlans
import com.nabati.sfa.model.product.WorkFlows
import com.nabati.sfa.modul.takingorder.inputproducttakingorder.OnSaveInputProduct
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_suggest_taking_order.view.*
import org.koin.android.architecture.ext.viewModel
import java.text.SimpleDateFormat
import java.util.*

class SuggestTakingOrderFragment : BaseFragment<SuggestTakingOrderPresenter>(),
    ISuggestTakingOrderView, OnSaveInputProduct {

    private lateinit var mView: View
    private val lastSearchViewModel by viewModel<InvoiceTakingOrderViewModel>()
    private val compositeDisposable = CompositeDisposable()
    private var outlet: ScheduledPlans? = null
    private lateinit var adapter: SuggestTakingOrderAdapter
    private var CALENDAR = Calendar.getInstance()
    private var invoiceTakingOrder: MutableList<InvoiceTakingOrder> = mutableListOf()
    lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_suggest_taking_order, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): SuggestTakingOrderPresenter {
        return SuggestTakingOrderPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewInit()
        setupToolbar("Recent Taking Order")
        initOnClickListener()
    }

    @SuppressLint("RestrictedApi")
    override fun onViewInit() {
        arguments?.let {
            val args = SuggestTakingOrderFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
        }

        lastSearchViewModel.listenInvoiceTakingOrderResult().observe(requireActivity(), Observer {
            invoiceTakingOrder = it.toMutableList()
        })

        presenter?.getData()
        mView.fabOption.visibility = View.GONE

        mView.tvDateTakingOrder.text = dateFormatTomorrow()
        shared?.put(SharedHelper.dateSend, dateFormatTomorrow())

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    validateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        if (shared?.valueFrom(SharedHelper.flagTransactionTO, false)!!) {
            val bundle = bundleOf(Pair("scheduled_plans", outlet))
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigate(R.id.action_to_taking_order, bundle)
        }
    }

    private fun initOnClickListener() {
        mView.fabOption.setOnClickListener {

        }

        mView.ivAddDate.setOnClickListener {
            val dateSetListener =
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    CALENDAR.set(Calendar.YEAR, year)
                    CALENDAR.set(Calendar.MONTH, monthOfYear)
                    CALENDAR.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateView()
                }

            DatePickerDialog(
                mView.context,
                dateSetListener,
                CALENDAR.get(Calendar.YEAR),
                CALENDAR.get(Calendar.MONTH),
                CALENDAR.get(Calendar.DAY_OF_MONTH) + 1
            ).show()

            mView.tvDateTakingOrder.text = dateFormatTomorrow()
            shared?.put(SharedHelper.dateSend, dateFormatTomorrow())
        }
    }

    override fun onSuccessRequest(workFlows: WorkFlows?) {
        if (!isAdded)
            return
        mView.rvDirectSelling.visibility = View.VISIBLE
        adapter = SuggestTakingOrderAdapter(requireContext())
        mView.rvDirectSelling.layoutManager = LinearLayoutManager(requireContext())
        mView.rvDirectSelling.setHasFixedSize(true)
        mView.rvDirectSelling.adapter = adapter
        mView.rvDirectSelling.isNestedScrollingEnabled = false

        val recentOrdersOutlet = workFlows?.recentOrders?.filter {
            it.outlet?.outletId?.equals(outlet?.outlet?.outletId, true)!!
        }
        if (!recentOrdersOutlet.isNullOrEmpty())
            adapter.addAll(recentOrdersOutlet.get(0).lines)
        else
            LoggerHelper.error("Empty product by outlet id")

    }

    override fun onSaveProduct(invoices: InvoiceTakingOrder) {
        saveLastSearch(invoices)
        val bundle = bundleOf(Pair("scheduled_plans", outlet))
        Navigation.findNavController(requireActivity(), R.id.navHostFragment)
            .navigate(R.id.action_to_taking_order, bundle)
    }

    private fun saveLastSearch(invoices: InvoiceTakingOrder) {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { lastSearchViewModel.saveInvoiceTakingOrder(invoices) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        callback.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_suggest_order, menu)
        menu.findItem(R.id.action_delete).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                validateBack()
                true
            }
            R.id.action_add_product -> {
                val bundle = bundleOf(Pair("scheduled_plans", outlet))
                Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                    .navigate(R.id.action_to_product_group_taking_order, bundle)
                true
            }
            R.id.action_to_cart -> {
                val bundle = bundleOf(Pair("scheduled_plans", outlet))
                Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                    .navigate(R.id.action_to_taking_order, bundle)
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    private fun validateBack() {
        if (invoiceTakingOrder.isNotEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setMessage("Going back will reset your Cart, are you sure?")
                .setCancelable(false)
                .setPositiveButton("YES") { dialog, id ->
                    initDeleteInvoice()
                    Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigateUp()
                    dialog.cancel()
                }
                .setNegativeButton("NO") { dialog, id ->
                    dialog.cancel()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Confirm")
            alert.show()
        } else
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigateUp()
    }

    override fun onError(message: String?) {
        showError(message)
    }

    override fun onStartLoad() {

    }

    override fun onFinishLoad() {

    }

    private fun updateDateView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mView.tvDateTakingOrder.text = sdf.format(CALENDAR.time)
    }

    private fun initDeleteInvoice() {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { lastSearchViewModel.deleteInvoiceTakingOrder() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }
}
