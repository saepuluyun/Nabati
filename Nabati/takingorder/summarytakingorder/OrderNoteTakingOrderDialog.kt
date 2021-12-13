package com.nabati.sfa.modul.takingorder.summarytakingorder

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.nabati.sfa.R
import kotlinx.android.synthetic.main.dialog_order_note.view.*

class OrderNoteDialog : DialogFragment() {

    private lateinit var mView: View
    lateinit var inflater: LayoutInflater

    companion object {

        private var listener: OnValueOrderNoteListener? = null
        private var orderNotes: String? = ""

        fun newInstance(orderNotes: String?, listener: OnValueOrderNoteListener?): OrderNoteDialog {
            val fragment = OrderNoteDialog()
            val bundle = Bundle()
            this.orderNotes = orderNotes
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
        mView = inflater.inflate(R.layout.dialog_order_note, null)
        onViewInit()
        initOnClickListener()
        return setupDialog(mView)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.ThemeOverlay_AppCompat_Dialog)
    }

    private fun onViewInit() {
        if (!orderNotes.isNullOrEmpty())
            mView.etOrderNote.setText(orderNotes)
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

        mView.btnCancel.setOnClickListener {
            dismiss()
        }

        mView.btnOk.setOnClickListener {
            if (mView.etOrderNote.text.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Order notes must be filled",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            listener?.onValueOrderNote(mView.etOrderNote.text.toString())
            dismiss()
        }
    }

}

interface OnValueOrderNoteListener {
    fun onValueOrderNote(value: String?)
}
