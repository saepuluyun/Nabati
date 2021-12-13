package com.nabati.sfa.modul.takingorder.promotion

import android.os.Bundle
import android.view.*
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.model.product.ValidPromoActivityModel
import com.nabati.sfa.model.product.WorkFlows
import com.nabati.sfa.modul.scheduleoutlet.promotion.AvailablePromoAdapter
import kotlinx.android.synthetic.main.fragment_direct_selling_promotion.view.*

class TakingOrderPromotionFragment : BaseFragment<TakingOrderPromotionPresenter>(),
    ITakingOrderPromotionView {

    lateinit var mView: View
    private var validPromoList: MutableList<ValidPromoActivityModel>? = mutableListOf()
    private var promoType: String? = ""
    private var workFlows: WorkFlows? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_direct_selling_promotion, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): TakingOrderPromotionPresenter {
        return TakingOrderPromotionPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onViewInit()
    }

    override fun onViewInit() {
        arguments?.let {
            val args = TakingOrderPromotionFragmentArgs.fromBundle(it)
            promoType = args.promoType
            workFlows = args.workFlows
        }

        promoType =
            if ("$promoType".contains("_")) promoType?.replace("_", " ") else promoType

        setupToolbar(promoType)

        val validPromo = workFlows?.validPromoActivity?.filter {
            val discountTypeValue = if (it.discountType.contains("_")) it.discountType.replace("_", " ") else it.discountType
            discountTypeValue.equals(promoType, true)
        }

        validPromoList = validPromo?.toMutableList()

        if (validPromoList.isNullOrEmpty()) {
            mView.tvEmptyPromo.visibility = View.VISIBLE
        } else {
            val adapter = AvailablePromoAdapter(requireContext())
            mView.rvAvailablePromo.layoutManager = LinearLayoutManager(requireContext())
            mView.rvAvailablePromo.setHasFixedSize(true)
            mView.rvAvailablePromo.isNestedScrollingEnabled = false
            mView.rvAvailablePromo.adapter = adapter
            adapter.addAll(validPromoList)
            adapter.setOnItemClickListener { view, position -> }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_direct_selling_promotion, menu)
        menu.findItem(R.id.action_refresh).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return when (id) {
            android.R.id.home -> {
                Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    override fun onError(message: String?) {
        showError(message)
    }
}