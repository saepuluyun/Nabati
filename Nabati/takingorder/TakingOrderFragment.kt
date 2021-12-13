package com.nabati.sfa.modul.takingorder

import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.getDateSend
import com.nabati.sfa.extention.getSalesman
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.helper.DateHelper
import com.nabati.sfa.helper.SharedHelper
import com.nabati.sfa.model.TransactionConfigs
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrderViewModel
import com.nabati.sfa.model.product.*
import com.nabati.sfa.modul.scheduleoutlet.DiscountAndPromotionDialog
import com.nabati.sfa.modul.scheduleoutlet.OnValueDiscountAndPromotionListener
import com.nabati.sfa.modul.takingorder.inputproducttakingorder.InputProductTakingOrderDialog
import com.nabati.sfa.modul.takingorder.inputproducttakingorder.OnSaveInputProduct
import com.nabati.sfa.modul.takingorder.summarytakingorder.OnActionClickListener
import com.nabati.sfa.modul.takingorder.summarytakingorder.PlusOrderDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.android.synthetic.main.fragment_taking_order.view.*
import org.koin.android.architecture.ext.viewModel
import java.text.SimpleDateFormat
import java.util.*

class TakingOrderFragment : BaseFragment<TakingOrderPresenter>(), ITakingOrderView,
    OnSaveInputProduct, OnActionClickListener, OnValueDiscountAndPromotionListener {

    private lateinit var mView: View
    private val lastSearchViewModel by viewModel<InvoiceTakingOrderViewModel>()
    private val compositeDisposable = CompositeDisposable()

    private var workFlows: WorkFlows? = null
    private lateinit var adapter: TakingOrderAdapter
    private var outlet: ScheduledPlans? = null
    private var takingOrderData: MutableList<InvoiceTakingOrder>? = mutableListOf()

    private var CALENDAR = Calendar.getInstance()
    lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_taking_order, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): TakingOrderPresenter {
        return TakingOrderPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        setupToolbar("Taking Order")
        onViewInit()
        initOnClickListener()
    }

    private fun initAdapter() {
        if (!isAdded)
            return
        adapter = TakingOrderAdapter(requireActivity())
        mView.rvTakingOrder.layoutManager = LinearLayoutManager(requireContext())
        mView.rvTakingOrder.setHasFixedSize(true)
        mView.rvTakingOrder.adapter = adapter
        mView.rvTakingOrder.isNestedScrollingEnabled = false
    }

    override fun onViewInit() {
        arguments?.let {
            val args = TakingOrderFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
        }

        presenter?.getData()
        lastSearchViewModel.listenInvoiceTakingOrderResult().observe(requireActivity(), Observer {
            loadData(it.toMutableList())
        })

        initOnDeleteItemListener()

        val date = "${getDateSend(mView.context!!)}"
        mView.tvDateTakingOrder.text = date

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    validateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        if (shared?.valueFrom(SharedHelper.flagTransactionTO, false)!!) {
            val countryId =
                if (getSalesman(requireContext())?.countryId.isNullOrEmpty()) "ID" else getSalesman(
                    requireContext()
                )?.countryId
            presenter?.getTransactionConfigs(countryId)
        }
    }

    private fun initOnClickListener() {
        mView.fabOption.setOnClickListener {
            val dialog = PlusOrderDialog.newInstance(true, this)
            dialog.show(parentFragmentManager, dialog.tag)
        }

        attachScrollPagination(mView.llFloating, mView.rvTakingOrder)

        mView.ivAddDate.setOnClickListener {
            val dateSetListener = object : DatePickerDialog.OnDateSetListener {
                override fun onDateSet(
                    view: DatePicker, year: Int, monthOfYear: Int,
                    dayOfMonth: Int
                ) {
                    CALENDAR.set(Calendar.YEAR, year)
                    CALENDAR.set(Calendar.MONTH, monthOfYear)
                    CALENDAR.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDateView()
                }
            }

            DatePickerDialog(
                mView.context,
                dateSetListener,
                CALENDAR.get(Calendar.YEAR),
                CALENDAR.get(Calendar.MONTH),
                CALENDAR.get(Calendar.DAY_OF_MONTH) + 1
            ).show()

            mView.tvDateTakingOrder.text = DateHelper.dateFormatTomorrow()
            shared?.put(SharedHelper.dateSend, DateHelper.dateFormatTomorrow())
        }
    }

    override fun onSuccessRequest(workFlows: WorkFlows?) {
        this.workFlows = workFlows
    }

    private fun loadData(takingOrderData: MutableList<InvoiceTakingOrder>?) {
        this.takingOrderData = takingOrderData
        takingOrderData?.let {
            mView.rvTakingOrder.setItemViewCacheSize(it.size)
            adapter.updateData(it)
            if (it.isNotEmpty()) {
                var total = 0F
                for (takingOrder in it) {
                    total = total.plus(takingOrder.price?.times(takingOrder.qty!!)!!)
                }

                mView.tvTotalPrice.text = "${rupiahCurrency(total)}"
            } else {
                mView.tvTotalPrice.text = "0"
            }
        }

        adapter.setOnItemClickListener { view, position ->
            val dialog = InputProductTakingOrderDialog.newInstance(
                Gson().fromJson(adapter.getItem(position).product, ProductTakingOrder::class.java),
                adapter.getItem(position),
                takingOrderData,
                this
            )
            dialog.show(requireFragmentManager(), dialog.tag)
        }

    }

    private fun initOnDeleteItemListener() {
        val callback: ItemTouchHelper.SimpleCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                try {
                    val position = viewHolder.adapterPosition
                    val product =
                        Gson().fromJson(adapter.getItem(position).product, Product::class.java)
                    val dialogBuilder = AlertDialog.Builder(requireContext())
                    dialogBuilder.setMessage(
                        "Are you sure to delete product ${product.productName} ${adapter.getItem(
                            position
                        ).uomPackage?.toUpperCase(
                            Locale.getDefault()
                        )}?"
                    )
                        .setCancelable(false)
                        .setPositiveButton("YES") { dialog, id ->
                            removeItemTakingOrder(
                                "${product.productName}${product.productId}${adapter.getItem(
                                    position
                                ).uomPackage}"
                            )
                        }
                        .setNegativeButton("NO") { dialog, id ->
                            adapter.notifyDataSetChanged()
                            dialog.cancel()
                        }
                    val alert = dialogBuilder.create()
                    alert.setTitle("Confirm")
                    alert.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX / 4,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                    .addSwipeLeftActionIcon(R.drawable.ic_delete)
                    .addSwipeLeftLabel("Delete")
                    .setSwipeLeftLabelColor(Color.WHITE)
                    .create()
                    .decorate()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX / 4,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(mView.rvTakingOrder)
    }

    override fun onSaveProduct(invoices: InvoiceTakingOrder) {
        saveLastSearch(invoices)
    }

    private fun saveLastSearch(invoices: InvoiceTakingOrder) {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { lastSearchViewModel.saveInvoiceTakingOrder(invoices) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }

    private fun initDeleteInvoice() {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { lastSearchViewModel.deleteInvoiceTakingOrder() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }

    private fun removeItemTakingOrder(productId: String?) {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { lastSearchViewModel.deleteItem(productId) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_direct_selling, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return when (id) {
            android.R.id.home -> {
                validateBack()
                true
            }
            R.id.action_save -> {
                if (takingOrderData.isNullOrEmpty()) {
                    onError("Your cart is empty, please select product again")
                } else {
                    val countryId =
                        if (getSalesman(requireContext())?.countryId.isNullOrEmpty()) "ID" else getSalesman(
                            requireContext()
                        )?.countryId
                    presenter?.getTransactionConfigs(countryId)
                }

                true
            }
            R.id.action_delete -> {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setMessage("Are you sure to clear product request?")
                    .setCancelable(false)
                    .setPositiveButton("YES") { dialog, id ->
                        mView.tvTotalPrice.text = "0"
                        initDeleteInvoice()
                    }
                    .setNegativeButton("NO") { dialog, id ->
                        dialog.cancel()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Confirm")
                alert.show()
                true
            }
            R.id.action_add_to_cart -> {
                val bundle = bundleOf(Pair("scheduled_plans", outlet))
                Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                    .navigate(R.id.action_to_product_group_taking_order, bundle)
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    private fun validateBack() {
        if (!takingOrderData.isNullOrEmpty()) {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setMessage("Are you sure back to previous screen and delete existing product in your cart list?")
                .setCancelable(false)
                .setPositiveButton("YES") { dialog, id ->
                    initDeleteInvoice()

                    val bundle = bundleOf(Pair("scheduled_plans", outlet))
                    Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigate(R.id.action_back_to_recent_taking_order, bundle)
                }
                .setNegativeButton("NO") { dialog, id ->
                    dialog.cancel()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Confirm")
            alert.show()
        } else {
            initDeleteInvoice()

            val bundle = bundleOf(Pair("scheduled_plans", outlet))
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigate(R.id.action_back_to_recent_taking_order, bundle)
        }
    }

    override fun onSuccessGetTransactionConfigs(transactionConfigs: TransactionConfigs?) {
        if (!isAdded)
            return
        val bundle = bundleOf(
            Pair("scheduled_plans", outlet),
            Pair("transactionConfigs", transactionConfigs)
        )
        if (findNavController().currentDestination?.id == R.id.nav_product_taking_order)
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigate(R.id.action_to_invoice_taking_order, bundle)
    }

    override fun onError(message: String?) {
        showError(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        callback.remove()
    }

    override fun onTakePhoto() {

    }

    override fun onTakeNote() {

    }

    override fun onTakePriceDiscount() {
        val dialog = DiscountAndPromotionDialog.newInstance(workFlows?.validPromoActivity, this)
        dialog.show(requireFragmentManager(), dialog.tag)
    }

    override fun onValueDiscountAndPromotion(validPromo: String?) {
        val bundle = bundleOf(Pair("promo_type", validPromo), Pair("workFlows", workFlows))
        Navigation.findNavController(requireActivity(), R.id.navHostFragment)
            .navigate(R.id.action_to_taking_order_promo, bundle)
    }

    override fun onTakePrint() {
    }

    private fun updateDateView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mView.tvDateTakingOrder.setText(sdf.format(CALENDAR.getTime()))
    }
}