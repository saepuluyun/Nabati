package com.nabati.sfa.modul.takingorder.summarytakingorder

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.SetOptions
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.extention.getDateSend
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.helper.DataHelper
import com.nabati.sfa.helper.DateHelper
import com.nabati.sfa.helper.SharedHelper
import com.nabati.sfa.model.journey.SalesmanJourneyTracks
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrderViewModel
import com.nabati.sfa.model.local.uploadimage.UploadImage
import com.nabati.sfa.model.local.uploadimage.UploadViewModel
import com.nabati.sfa.model.product.Orders
import com.nabati.sfa.model.product.ScheduledPlans
import com.nabati.sfa.model.product.WorkFlows
import com.nabati.sfa.modul.printinvoice.PrintInvoiceActivity
import com.nabati.sfa.modul.scheduleoutlet.DiscountAndPromotionDialog
import com.nabati.sfa.modul.scheduleoutlet.OnValueDiscountAndPromotionListener
import com.nabati.sfa.modul.takephoto.OnValueImageCapture
import com.nabati.sfa.modul.takephoto.TakePhotoActivity
import com.nabati.sfa.modul.takingorder.suggesttakingorder.SuggestTakingOrderAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_summary_order_taking_order.view.*
import org.koin.android.architecture.ext.viewModel
import java.text.SimpleDateFormat
import java.util.*

class SummaryOrderTakingOrderFragment : BaseFragment<SummaryOrderTakingOrderPresenter>(),
    ISummaryOrderTakingOrderView,
    OnValueOrderNoteListener, OnActionClickListener, OnValueImageCapture,
    OnValueDiscountAndPromotionListener {

    private lateinit var mView: View
    lateinit var callback: OnBackPressedCallback
    private var outlet: ScheduledPlans? = null
    private var newInvoices: Orders? = null
    private var orderNotes: String? = ""
    private var workFlows: WorkFlows? = null
    private var isSuccessSaveInvoice = false
    private var isHasTakePhoto = false
    private var isHasUpdateNote = false
    private var isHasSaved = false
    private var salesmanJourneyTracks: SalesmanJourneyTracks? = null
    private val uploadViewModel by viewModel<UploadViewModel>()
    private val compositeDisposable = CompositeDisposable()

    private val lastSearchViewModel by viewModel<InvoiceTakingOrderViewModel>()

    private var invoices: Orders? = null

    private var CALENDAR = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_summary_order_taking_order, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar("Summary Taking Order")
        onViewInit()
        initOnClickListener()
    }

    override fun attachPresenter(): SummaryOrderTakingOrderPresenter {
        return SummaryOrderTakingOrderPresenter(requireContext(), this)
    }

    override fun onViewInit() {
        arguments?.let {
            val args = SummaryOrderTakingOrderFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
            newInvoices = args.invoicesTakingOrder

            invoices = newInvoices
        }

        presenter?.getData()
        if (!isAdded)
            return
        val adapter = SuggestTakingOrderAdapter(requireContext())
        mView.rvSummaryOrder.layoutManager = LinearLayoutManager(requireContext())
        mView.rvSummaryOrder.setHasFixedSize(true)
        mView.rvSummaryOrder.adapter = adapter
        mView.rvSummaryOrder.isNestedScrollingEnabled = false
        adapter.addAll(newInvoices?.lines)
        adapter.setOnItemClickListener { _, _ -> }

        /*val freeGoodAdapter = SummaryFreeGoodsAdapter(requireContext())
        mView.rvFreeGoodsInvoice.layoutManager = LinearLayoutManager(requireContext())
        mView.rvFreeGoodsInvoice.setHasFixedSize(true)
        mView.rvFreeGoodsInvoice.adapter = freeGoodAdapter
        mView.rvFreeGoodsInvoice.isNestedScrollingEnabled = false
        freeGoodAdapter.addAll(newInvoices?.freeGoods)
        freeGoodAdapter.setOnItemClickListener { _, _ -> }*/

        mView.tvTotalPrice.text = "${rupiahCurrency(newInvoices?.nettPrice)}"

        val date = "${getDateSend(mView.context!!)}"
        mView.tvDateTakingOrder.setText(date)
    }

    private fun initOnClickListener() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isAdded)
                    onValidateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), callback)

        mView.fabOption.setOnClickListener {
            val dialog = PlusOrderDialog.newInstance(false, this)
            dialog.show(parentFragmentManager, dialog.tag)
        }

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

            mView.tvDateTakingOrder.setText(DateHelper.dateFormatTomorrow())
            shared?.put(SharedHelper.dateSend, DateHelper.dateFormatTomorrow())
        }

        mView.tvSave.setOnClickListener {
            if (isSuccessSaveInvoice) {
                onValidateBack()
            } else
                onSaveInvoice()
            mView.tvSave.setText(R.string.finish)
        }

        mView.ivBack.setOnClickListener {
            val isFlagPrintInvoice = shared?.valueFrom(SharedHelper.flagPrintInvoice, false)
            if (isFlagPrintInvoice!! and isHasTakePhoto and isHasUpdateNote)
                return@setOnClickListener

            onValidateBack()
        }

    }

    private fun onValidateBack() {
        val isFlagPrintInvoice = shared?.valueFrom(SharedHelper.flagPrintInvoice, false)
        if (!isFlagPrintInvoice!! and !isHasTakePhoto and !isHasUpdateNote) {
            onError("Please complete your transaction process by taking a photo, note and print your invoice")
            return
        }
        if (!isFlagPrintInvoice) {
            onError("Please complete your transaction process print your invoice")
            return
        }
        if (!isHasTakePhoto) {
            onError("Please complete your transaction process by taking a photo")
            return
        }
        if (!isHasUpdateNote) {
            onError("Please complete your transaction process by taking note")
            return
        }

        initDeleteInvoice()

        val bundle = bundleOf(
            Pair("scheduled_plans", outlet),
            Pair("journeys_tracks", salesmanJourneyTracks),
            Pair("isFromSummary", true)
        )
        Navigation.findNavController(requireActivity(), R.id.navHostFragment)
            .navigate(R.id.action_to_outlet, bundle)

        shared?.put(SharedHelper.isFromSummary, true)
        shared?.remove(SharedHelper.flagPrintInvoice)
    }

    override fun onSuccessGetData(workFlows: WorkFlows?) {
        this.workFlows = workFlows
        if (!isHasSaved)
            onSaveInvoice()
    }

    override fun onSuccessGetDataJourneyTrack(salesmanJourneyTracks: SalesmanJourneyTracks?) {
        this.salesmanJourneyTracks = salesmanJourneyTracks
    }

    override fun onValueOrderNote(value: String?) {
        orderNotes = value
        showLoadingDialog()
        updateNote()
    }

    override fun onValueImage(uri: Uri?, from: String, name: String) {
        if (uri == null && from == "" && name == "") {
            showError("Image Not Found")
        } else {
            if (from == "TakingOrder") {
                val upload = UploadImage(
                    uri!!.toString(),
                    name,
                    "TakingOrder"
                )
                saveUpload(upload)
                isHasTakePhoto = true
            }
        }
    }

    private fun saveUpload(upload: UploadImage) {
        val disposable = Observable.just(true)
            .observeOn(Schedulers.io())
            .doOnNext { uploadViewModel.saveUpload(upload) }
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

    private fun updateNote() {
        newInvoices?.notes = orderNotes

        val invoice = hashMapOf("orders" to generateInvoice())

        DataHelper.getDataWorkFlow(requireContext()).set(invoice, SetOptions.merge())
            .addOnSuccessListener {}
            .addOnFailureListener {}

        dismissLoadingDialog()
        onError("Your note has been updated")
        isHasUpdateNote = true
    }

    private fun generateInvoice(): MutableList<Orders> {
        val invoicesTakingOrder = workFlows?.orders

        val it: MutableIterator<Orders>? = invoicesTakingOrder?.iterator()
        while (it?.hasNext()!!) {
            val s: Orders = it?.next()
            if (s.orderId == newInvoices?.orderId) {
                it?.remove()
            }
        }

        invoicesTakingOrder.addAll(0, listOf(this.newInvoices!!))

        return invoicesTakingOrder
    }

    private fun onSaveInvoice() {
        showLoadingDialog()
        val invoice = hashMapOf("orders" to generateInvoice())

        DataHelper.getDataWorkFlow(requireContext()).set(invoice, SetOptions.merge())
            .addOnSuccessListener {}
            .addOnFailureListener {}
        dismissLoadingDialog()
        isSuccessSaveInvoice = true
        isHasSaved = true

        shared?.remove(SharedHelper.flagTransactionTO)
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
        callback.remove()
    }

    override fun onTakePhoto() {
        TakePhotoActivity.setOnCaptureImageListener(this)
        val intent = Intent(getActivity(), TakePhotoActivity::class.java)
        intent.putExtra("From", "TakingOrder")
        intent.putExtra("Data", newInvoices?.orderId)
        intent.putExtra("Uuid", shared?.valueFrom(SharedHelper.orderId, ""))

        getActivity()?.startActivity(intent)
    }

    override fun onTakeNote() {
        val dialog = OrderNoteDialog.newInstance(orderNotes, this)
        dialog.show(requireFragmentManager(), dialog.tag)
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
        if (isSuccessSaveInvoice) {
            PrintInvoiceActivity.start(requireContext(), outlet, "TakingOrder")
        } else {
            onError("Please Save Before Print Invoice")
        }
    }

    private fun updateDateView() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        mView.tvDateTakingOrder.setText(sdf.format(CALENDAR.getTime()))
    }
}
