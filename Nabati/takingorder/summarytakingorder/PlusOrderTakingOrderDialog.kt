package com.nabati.sfa.modul.takingorder.summarytakingorder

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.nabati.sfa.R
import kotlinx.android.synthetic.main.dialog_plus_order.view.*

class PlusOrderDialog : DialogFragment() {

    private lateinit var mView: View
    lateinit var inflater: LayoutInflater

    companion object {

        lateinit var listener: OnActionClickListener
        private var isFromTakingOrder: Boolean? = false

        fun newInstance(isFromTakingOrder: Boolean?, listener: OnActionClickListener): PlusOrderDialog {
            val fragment = PlusOrderDialog()
            val bundle = Bundle()
            this.isFromTakingOrder = isFromTakingOrder
            this.listener = listener
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inflater = LayoutInflater.from(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mView = inflater.inflate(R.layout.dialog_plus_taking_order, null)
        onViewInit()
        initOnClickListener()
        return setupDialog(mView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppCompat_Dialog)
    }

    private fun onViewInit() {
        if (isFromTakingOrder == true) {
            mView.llTakePhoto.visibility = View.GONE
            mView.llNotes.visibility = View.GONE
            mView.llPrint.visibility = View.GONE
        } else {
            mView.llTakePhoto.visibility = View.VISIBLE
            mView.llNotes.visibility = View.VISIBLE
            mView.llPrint.visibility = View.VISIBLE
        }
    }

    private fun setupDialog(view: View): Dialog {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(view)
        dialog.window?.attributes?.windowAnimations = R.style.ProgressDialog
        dialog.window?.setBackgroundDrawableResource(R.color.nb_black_transparent)
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    private fun initOnClickListener() {

        mView.llTakePhoto.setOnClickListener {
            listener.onTakePhoto()
            dismiss()
        }

        mView.llNotes.setOnClickListener {
            listener.onTakeNote()
            dismiss()
        }

        mView.llPriceAndDiscount.setOnClickListener {
            listener.onTakePriceDiscount()
            dismiss()
        }

        mView.llPrint.setOnClickListener {
            listener.onTakePrint()
            dismiss()
        }
    }

}

interface OnActionClickListener {

    fun onTakePhoto()
    fun onTakeNote()
    fun onTakePriceDiscount()
    fun onTakePrint()

}
