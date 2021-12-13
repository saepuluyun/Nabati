package com.nabati.sfa.modul.takingorder.inputproducttakingorder

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import com.google.gson.Gson
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseDialog
import com.nabati.sfa.extention.clearRp
import com.nabati.sfa.extention.getDateSend
import com.nabati.sfa.extention.rupiahCurrency
import com.nabati.sfa.helper.DateHelper.dateFormatStringToDate
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.product.ProductTakingOrder
import kotlinx.android.synthetic.main.dialog_input_product.view.*
import java.util.*

class InputProductTakingOrderDialog : BaseDialog<InputProductTakingOrderPresenter>(),
    IInputProductTakingOrderView {

    private lateinit var mView: View
    lateinit var inflater: LayoutInflater
    private var productType: String? = ""
    private var productPrice: Float? = 0F
    private var productUom: String? = ""

    companion object {
        private var product: ProductTakingOrder? = null
        private var listener: OnSaveInputProduct? = null
        private var invoiceTakingOrder: InvoiceTakingOrder? = null
        private var invoiceTakingOrderList: MutableList<InvoiceTakingOrder>? = mutableListOf()

        fun newInstance(
            product: ProductTakingOrder?,
            invoiceTakingOrderList: MutableList<InvoiceTakingOrder>?,
            listener: OnSaveInputProduct?
        ): InputProductTakingOrderDialog {
            val fragment = InputProductTakingOrderDialog()
            val bundle = Bundle()
            this.product = product
            this.listener = listener
            this.invoiceTakingOrderList = invoiceTakingOrderList
            this.invoiceTakingOrder = null
            fragment.arguments = bundle
            return fragment
        }

        fun newInstance(
            product: ProductTakingOrder?,
            invoiceTakingOrder: InvoiceTakingOrder?,
            invoiceTakingOrderList: MutableList<InvoiceTakingOrder>?,
            listener: OnSaveInputProduct?
        ): InputProductTakingOrderDialog {
            val fragment = InputProductTakingOrderDialog()
            val bundle = Bundle()
            this.product = product
            this.invoiceTakingOrderList = invoiceTakingOrderList
            this.invoiceTakingOrder = invoiceTakingOrder
            this.listener = listener
            fragment.arguments = bundle
            return fragment
        }

    }

    override fun attachPresenter(): InputProductTakingOrderPresenter {
        return InputProductTakingOrderPresenter(requireContext(), this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inflater = LayoutInflater.from(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mView = inflater.inflate(R.layout.dialog_input_product, null)
        onViewInit()
        initOnClickListener()
        return setupDialog(mView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppCompat_Dialog)
    }

    private fun setupDialog(view: View): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.attributes?.windowAnimations = R.style.ProgressDialog
        dialog.window?.setBackgroundDrawableResource(R.color.nb_black_transparent)
        val width = (resources.displayMetrics.widthPixels * 0.80).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onViewInit() {
        mView.llDisplayStock.visibility = View.GONE

        if (product?.availableUoms.isNullOrEmpty()) {
            productType = "sml"
            productPrice = product?.price?.sml
            productUom = product?.unitsName?.sml
        } else {
            productType = product?.availableUoms?.get(0)?.toLowerCase(Locale.getDefault())
            mView.tvProductType.text = productType?.toUpperCase(Locale.getDefault())
            when (productType) {
                "sml" -> {
                    productPrice = product?.price?.sml
                    productUom = product?.unitsName?.sml
                }
                "med" -> {
                    productPrice = product?.price?.med
                    productUom = product?.unitsName?.med
                }
                "lrg" -> {
                    productPrice = product?.price?.lrg
                    productUom = product?.unitsName?.lrg
                }
            }
        }

        mView.tvProductName.text = product?.productName
        mView.tvProductId.text = product?.productId

        if (invoiceTakingOrder != null) {
            mView.tvTotalPrice.text =
                "${rupiahCurrency(invoiceTakingOrder?.qty!! * invoiceTakingOrder?.price!!)}"
            mView.tvProductType.text = invoiceTakingOrder?.uomPackage?.toUpperCase(Locale.getDefault())
            mView.etQuantity.setText("${invoiceTakingOrder?.qty}")
            productType = "${invoiceTakingOrder?.uomPackage}"
            productPrice = invoiceTakingOrder?.price
            productUom = invoiceTakingOrder?.uom
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initOnClickListener() {

        mView.llSelectType.setOnClickListener {
            val popup = PopupMenu(requireContext(), it)
            for (uom in product?.availableUoms!!) {
                popup.menu.add(uom.toUpperCase(Locale.getDefault()))
            }
            popup.setOnMenuItemClickListener { item ->
                for (uom in product?.availableUoms!!) {
                    if (uom.equals("${item.title}", true)) {
                        mView.tvProductType.text = item.title

                        when (uom.toLowerCase(Locale.getDefault())) {
                            "sml" -> {
                                productType = "sml"
                                productPrice = product?.price?.sml
                                productUom = product?.unitsName?.sml

                                mView.etQuantity.setText("")
                                mView.tvTotalPrice.text = "0"
                            }
                            "med" -> {
                                productType = "med"
                                productPrice = product?.price?.med
                                productUom = product?.unitsName?.med

                                mView.etQuantity.setText("")
                                mView.tvTotalPrice.text = "0"
                            }
                            "lrg" -> {
                                productType = "lrg"
                                productPrice = product?.price?.lrg
                                productUom = product?.unitsName?.lrg

                                mView.etQuantity.setText("")
                                mView.tvTotalPrice.text = "0"
                            }
                        }
                    }
                }
                true
            }
            popup.show()
        }

        mView.etQuantity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(value: Editable?) {
                if ((value.toString().length == 1) and value.toString().startsWith("0")) {
                    value?.clear()
                    mView.etQuantity.text = value
                    mView.tvTotalPrice.text = "0"
                } else {
                    if (value?.isNotEmpty()!!) {
                        when (productType) {
                            "lrg" -> {
                                mView.tvTotalPrice.text =
                                    "${rupiahCurrency(
                                        value.toString().toInt() * product?.price?.lrg!!
                                    )}"
                            }
                            "med" -> {
                                mView.tvTotalPrice.text =
                                    "${rupiahCurrency(
                                        value.toString().toInt() * product?.price?.med!!
                                    )}"
                            }
                            "sml" -> {
                                mView.tvTotalPrice.text =
                                    "${rupiahCurrency(
                                        value.toString().toInt() * product?.price?.sml!!
                                    )}"
                            }
                        }
                    } else {
                        mView.tvTotalPrice.text = "0"
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        mView.btnCancel.setOnClickListener {
            dismiss()
        }

        mView.btnSave.setOnClickListener {
            val validateProductUom = invoiceTakingOrderList?.filter {
                it.productNameId.equals("${product?.productName}${product?.productId}$productType", true)
            }

            if (!validateProductUom.isNullOrEmpty()) {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setMessage("Are you sure you want to update this item?")
                    .setCancelable(false)
                    .setPositiveButton("YES") { dialog, id ->
                        onSaveProductTakingOrder()
                    }
                    .setNegativeButton("NO") { dialog, id ->
                        dialog.cancel()
                        dismiss()
                    }
                val alert = dialogBuilder.create()
                alert.setTitle("Confirm")
                alert.show()
            } else {
                onSaveProductTakingOrder()
            }
        }

    }

    private fun onSaveProductTakingOrder() {

        val qty = mView.etQuantity.text.toString()
        val total = clearRp(mView.tvTotalPrice.text.toString())

        if (qty.isEmpty()) {
            onError("Quantity is empty")
            return
        }

        val date = "${getDateSend(mView.context!!)}"

        listener?.onSaveProduct(
            InvoiceTakingOrder(
                "${product?.productName}${product?.productId}$productType",
                product?.productId,
                dateFormatStringToDate(date),
                productPrice,
                Gson().toJson(product),
                qty.toInt(),
                total.toFloat(),
                productUom,
                productType,
                ""
            )
        )
        dismiss()
    }

    override fun onError(message: String?) {
        showError(message)
    }

}

interface OnSaveInputProduct {
    fun onSaveProduct(invoices: InvoiceTakingOrder)
}
