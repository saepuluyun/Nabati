package com.nabati.sfa.modul.takingorder.invoicetakingorder

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.*
import com.nabati.sfa.helper.DateHelper.dateFormatStringToTimestamp
import com.nabati.sfa.helper.SharedHelper
import com.nabati.sfa.model.CalculateInvoiceQuery
import com.nabati.sfa.model.TransactionConfigs
import com.nabati.sfa.model.customer.Customer
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrderViewModel
import com.nabati.sfa.model.local.uploadimage.UploadImage
import com.nabati.sfa.model.local.uploadimage.UploadViewModel
import com.nabati.sfa.model.product.*
import com.nabati.sfa.modul.promoactivity.InvoiceWithCalculatedDiscount
import com.nabati.sfa.modul.promoactivity.calculateDiscount
import com.nabati.sfa.modul.scheduleoutlet.invoice.CalculateInvoiceAdapter
import com.nabati.sfa.modul.scheduleoutlet.invoice.FreeGoodsAdapter
import com.nabati.sfa.modul.scheduleoutlet.invoice.InvoiceAdapter
import com.nabati.sfa.modul.scheduleoutlet.invoice.TaxesAdapter
import com.nabati.sfa.modul.signature.OnValueSignatureListener
import com.nabati.sfa.modul.signature.SignatureActivity
import com.nabati.sfa.modul.takingorder.TakingOrderAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_invoice_taking_order.view.*
import org.koin.android.architecture.ext.viewModel
import java.util.*

class InvoiceTakingOrderFragment : BaseFragment<InvoiceTakingOrderPresenter>(),
    IInvoiceTakingOrderView, OnValueSignatureListener {

    private lateinit var mView: View
    private val lastSearchViewModel by viewModel<InvoiceTakingOrderViewModel>()
    private val uploadViewModel by viewModel<UploadViewModel>()
    private var invoiceList: MutableList<InvoiceTakingOrder>? = mutableListOf()
    private val compositeDisposable = CompositeDisposable()
    private var outlet: ScheduledPlans? = null
    private var transactionConfigs: TransactionConfigs? = null
    private var workFlows: WorkFlows? = null
    private var adapter: TakingOrderAdapter? = null
    private var isSuccessAddSignature = false
    private var invoiceWithCalculatedDiscount: InvoiceWithCalculatedDiscount? = null
    lateinit var callback: OnBackPressedCallback

    private var orderId: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_invoice_taking_order, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): InvoiceTakingOrderPresenter {
        return InvoiceTakingOrderPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        onViewInit()
        initOnClickListener()
    }

    override fun onViewInit() {
        arguments?.let {
            val args = InvoiceTakingOrderFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
            transactionConfigs = args.transactionConfigs
        }

        setupToolbar(outlet?.outlet?.outletName)


        lastSearchViewModel.listenInvoiceTakingOrderResult().observe(requireActivity(), Observer {
            invoiceList = it.toMutableList()
        })

        presenter?.getData()

        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                        .navigateUp()
                shared?.remove(SharedHelper.flagTransactionTO)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        shared?.put(SharedHelper.flagTransactionTO, true)
    }

    private fun initAdapter() {
        adapter = TakingOrderAdapter(requireActivity())
        mView.rvConfirmInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvConfirmInvoice.setHasFixedSize(true)
        mView.rvConfirmInvoice.adapter = adapter
        mView.rvConfirmInvoice.isNestedScrollingEnabled = false
    }

    override fun onSuccessRequest(workFlows: WorkFlows?) {
        if (!isAdded)
            return
        this.workFlows = workFlows
//        presenter?.getDataCustomer(outlet?.outlet?.outletId)

        val query = CalculateInvoiceQuery()
        query.transactionType = "O"
        query.customer = mappingDataCustomer()
        query.salesman = getSalesman(requireContext())
        query.lines = generateInvoice().lines
        query.taxConfigs = getSalesmanOrganization(requireContext())?.taxes

        presenter?.calculateInvoice(query)

    }

    override fun onSuccessCalculateInvoice(calculatedDiscount: InvoiceWithCalculatedDiscount?) {
        invoiceWithCalculatedDiscount = calculatedDiscount

        generateOrderId()

        if (!calculatedDiscount?.lines.isNullOrEmpty()) {
            val listProductId: ArrayList<String> = arrayListOf()

            for (product in invoiceWithCalculatedDiscount?.lines!!) {
                listProductId.add("${product.product?.productId}")
            }

            val listSKU = listProductId.distinct()

            mView.tvTotalItem.text = "${listSKU.size}"
        }

        val adapterInvoices = InvoiceAdapter(requireActivity())
        mView.rvConfirmInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvConfirmInvoice.setHasFixedSize(true)
        mView.rvConfirmInvoice.adapter = adapterInvoices
        mView.rvConfirmInvoice.isNestedScrollingEnabled = false
        adapterInvoices.addAll(invoiceWithCalculatedDiscount?.lines)
        adapterInvoices.setOnItemClickListener { _, _ -> }

        val freeGoodAdapter = FreeGoodsAdapter(requireContext())
        mView.rvFreeGoodsInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvFreeGoodsInvoice.setHasFixedSize(true)
        mView.rvFreeGoodsInvoice.adapter = freeGoodAdapter
        mView.rvFreeGoodsInvoice.isNestedScrollingEnabled = false
        freeGoodAdapter.addAll(invoiceWithCalculatedDiscount?.freeGoods)
        freeGoodAdapter.setOnItemClickListener { _, _ -> }

        if (!invoiceWithCalculatedDiscount?.discountAggregate?.values.isNullOrEmpty()) {
            val adapter = CalculateInvoiceAdapter(requireContext())
            mView.rvCalculateDiscount.layoutManager = LinearLayoutManager(requireContext())
            mView.rvCalculateDiscount.setHasFixedSize(true)
            mView.rvCalculateDiscount.isNestedScrollingEnabled = false
            mView.rvCalculateDiscount.adapter = adapter
            adapter.addAll(ArrayList(invoiceWithCalculatedDiscount?.discountAggregate?.values!!))
            adapter.setOnItemClickListener { _, _ -> }
        }

        mView.tvGrossPrice.text =
            rupiahCurrency(invoiceWithCalculatedDiscount?.gross?.toFloat())

        val taxesAdapter = TaxesAdapter(requireContext())
        mView.rvTaxes.layoutManager = LinearLayoutManager(requireContext())
        mView.rvTaxes.setHasFixedSize(true)
        mView.rvTaxes.adapter = taxesAdapter
        mView.rvTaxes.isNestedScrollingEnabled = false
        taxesAdapter.addAll(invoiceWithCalculatedDiscount?.taxes)
        taxesAdapter.setOnItemClickListener { _, _ -> }

        mView.tvNetPrice.text =
            rupiahCurrency(invoiceWithCalculatedDiscount?.nettPrice?.toFloat())
    }

    override fun onSuccessGetDataCustomer(customers: MutableList<Customer>?) {
        if (!isAdded)
            return

        /*invoiceWithCalculatedDiscount = calculateDiscount(
            generateInvoice().orderId,
            generateInvoice().lines,
            getSalesman(requireContext()),
            mappingDataCustomer(),
            getSalesmanOrganization(requireContext()),
            workFlows?.validPromoActivity
        )

        val listProductId: ArrayList<String> = arrayListOf()

        for (product in invoiceWithCalculatedDiscount?.lines!!) {
            listProductId.add("${product.product?.productId}")
        }

        val listSKU = listProductId.distinct()

        mView.tvTotalItem.text = "${listSKU.size}"

        val adapterInvoices = InvoiceAdapter(requireActivity())
        mView.rvConfirmInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvConfirmInvoice.setHasFixedSize(true)
        mView.rvConfirmInvoice.adapter = adapterInvoices
        mView.rvConfirmInvoice.isNestedScrollingEnabled = false
        adapterInvoices.addAll(invoiceWithCalculatedDiscount?.lines)
        adapterInvoices.setOnItemClickListener { _, _ -> }

        val adapter = CalculateInvoiceAdapter(requireContext())
        mView.rvCalculateDiscount.layoutManager = LinearLayoutManager(requireContext())
        mView.rvCalculateDiscount.setHasFixedSize(true)
        mView.rvCalculateDiscount.isNestedScrollingEnabled = false
        mView.rvCalculateDiscount.adapter = adapter
        adapter.addAll(ArrayList(invoiceWithCalculatedDiscount?.discountAggregate?.values!!))
        adapter.setOnItemClickListener { _, _ -> }

        mView.tvGrossPrice.text =
            rupiahCurrency(invoiceWithCalculatedDiscount?.gross?.toFloat())

        val taxesAdapter = TaxesAdapter(requireContext())
        mView.rvTaxes.layoutManager = LinearLayoutManager(requireContext())
        mView.rvTaxes.setHasFixedSize(true)
        mView.rvTaxes.adapter = taxesAdapter
        mView.rvTaxes.isNestedScrollingEnabled = false
        taxesAdapter.addAll(invoiceWithCalculatedDiscount?.taxes)
        taxesAdapter.setOnItemClickListener { _, _ -> }

        mView.tvNetPrice.text =
            rupiahCurrency(invoiceWithCalculatedDiscount?.nettPrice?.toFloat())*/
    }

    private fun mappingDataCustomer(): Customer {
        return Customer(
            outlet?.outlet?.address,
            outlet?.outlet?.branchId,
            outlet?.outlet?.channelId,
            outlet?.outlet?.city,
            outlet?.outlet?.customerEmail,
            outlet?.outlet?.customerGroup1Id,
            outlet?.outlet?.customerGroup2Id,
            outlet?.outlet?.customerGroupId,
            outlet?.outlet?.customerName,
            outlet?.outlet?.identityCard,
            outlet?.outlet?.identityCardImage,
            outlet?.outlet?.latLong,
            outlet?.outlet?.notes,
            outlet?.outlet?.outletBarcode,
            outlet?.outlet?.outletId,
            outlet?.outlet?.outletImage,
            outlet?.outlet?.outletLocation,
            outlet?.outlet?.outletName,
            outlet?.outlet?.phoneNumber,
            outlet?.outlet?.postalCode,
            outlet?.outlet?.priceList,
            outlet?.outlet?.salesmanId,
            outlet?.outlet?.salesGroupId,
            outlet?.outlet?.taxId,
            outlet?.outlet?.textIdImage,
            outlet?.outlet?.termOfPayment,
            outlet?.outlet?.dateTimeInformation,
            outlet?.outlet?.workFlowsId,
            outlet?.outlet?.status
        )
    }

    private fun initOnClickListener() {
        mView.btnCancel.setOnClickListener {
            shared?.remove(SharedHelper.flagTransactionTO)
            Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
        }

        mView.btnOk.setOnClickListener {
            SignatureActivity.start(
                requireContext(),
                outlet?.outlet?.outletName,
                "TakingOrder",
                this
            )
        }

    }

    override fun onValueSignature(uri: Uri?, name: String) {
        isSuccessAddSignature = true
        val upload = UploadImage(
            uri?.toString(),
            name,
            "SignatureTakingOrder"
        )

        saveUpload(upload)
    }

    private fun saveUpload(upload: UploadImage) {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { uploadViewModel.saveUpload(upload) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
        compositeDisposable.add(disposable)
    }

    private fun generateInvoice(): Orders {
        val linesList: MutableList<Lines> = mutableListOf()
        for (invoice in invoiceList!!) {
            linesList.add(
                Lines(
                    invoice.price,
                    Gson().fromJson(invoice.product, Product::class.java),
                    invoice.qty,
                    invoice.total,
                    invoice.uom,
                    invoice.uomPackage,
                    invoice.discount
                )
            )
        }

        val date = "${getDateSend(mView.context!!)}"

        val invoice = Orders()
        invoice.discountPromoItems = mutableListOf()
        invoice.orderId = getOrderId(requireContext())
        invoice.dateSend = dateFormatStringToTimestamp(date)
        invoice.nettPrice = clearRp(mView.tvNetPrice.text.toString()).toFloat()
        invoice.lines = linesList
        invoice.outlet = outlet?.outlet
        invoice.gross = clearRp(mView.tvGrossPrice.text.toString()).toFloat()
        invoice.totalQty = mView.tvTotalItem.text.toString()

        return invoice
    }

    private fun generateOrderId() {
        orderId = if (invoiceWithCalculatedDiscount?.invoiceId == null) "" else invoiceWithCalculatedDiscount?.invoiceId
        shared?.put(SharedHelper.orderId, orderId)
    }

    private fun generateNewInvoice(): Orders {

        val date = "${getDateSend(mView.context!!)}"
        val invoice = Orders()
        invoice.orderId = invoiceWithCalculatedDiscount?.invoiceId
        invoice.dateSend = dateFormatStringToTimestamp(date)
        invoice.nettPrice = invoiceWithCalculatedDiscount?.nettPrice?.toFloat()
        invoice.lines = invoiceWithCalculatedDiscount?.lines?.toMutableList()
        invoice.outlet = outlet?.outlet
        invoice.taxBase = invoiceWithCalculatedDiscount?.taxBase
        invoice.taxTotal = invoiceWithCalculatedDiscount?.taxTotal
        invoice.totalQty = mView.tvTotalItem.text.toString()
        invoice.freeGoods = invoiceWithCalculatedDiscount?.freeGoods
        invoice.gross = invoiceWithCalculatedDiscount?.gross?.toFloat()
        invoice.discountAggregate =
            invoiceWithCalculatedDiscount?.discountAggregate?.values?.toMutableList()
        invoice.taxes = invoiceWithCalculatedDiscount?.taxes?.toMutableList()
        invoice.nettPrice = invoiceWithCalculatedDiscount?.nettPrice?.toFloat()
        return invoice
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                shared?.remove(SharedHelper.flagTransactionTO)
                Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (isSuccessAddSignature) {
            val bundle =
                bundleOf(
                    Pair("scheduled_plans", outlet),
                    Pair("invoicesTakingOrder", generateNewInvoice())
                )
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigate(R.id.action_to_summary_order_taking_order, bundle)
            isSuccessAddSignature = false
        }
    }

    override fun onStartLoad() {
        showLoadingDialog()
    }

    override fun onFinishLoad() {
        dismissLoadingDialog()
    }

    override fun onError(message: String?) {
        showError(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        callback.remove()
    }
}
