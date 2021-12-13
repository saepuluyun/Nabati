package com.nabati.sfa.modul.takingorder.productgrouptakingorder

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.model.product.ProductBrand
import com.nabati.sfa.model.product.ScheduledPlans
import com.nabati.sfa.modul.scheduleoutlet.productgroup.ProductGroupFragmentArgs
import kotlinx.android.synthetic.main.fragment_product_group.view.*

class ProductGroupTakingOrderFragment : BaseFragment<ProductGroupTakingOrderPresenter>(),
    IProductGroupTakingOrderView {

    lateinit var mView: View
    private var outlet: ScheduledPlans? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_product_group, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): ProductGroupTakingOrderPresenter {
        return ProductGroupTakingOrderPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar("Select Product Group")
        onViewInit()
        loadData()
    }

    override fun onViewInit() {
        arguments?.let {
            val args = ProductGroupFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
        }
    }

    private fun loadData() {
        presenter?.getDataProductGroup()
    }

    override fun onSuccessGetProductGroup(brands: MutableList<ProductBrand>?) {
        initDataProducts(brands)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_product_group, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return when (id) {
            android.R.id.home -> {
                Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
                true
            }
            R.id.action_search -> {
                val bundle = bundleOf(Pair("scheduled_plans", outlet))
                Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                    .navigate(R.id.action_to_product_search_taking_order, bundle)
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    override fun onStartLoad() {
        mView.pgProductGroup.visibility = View.VISIBLE
    }

    override fun onFinishLoad() {
        mView.pgProductGroup.visibility = View.GONE
    }

    private fun initDataProducts(productList: MutableList<ProductBrand>?) {
        if (!isAdded)
            return
        val adapter = ProductGroupTakingOrderAdapter(requireContext())
        mView.rvProductGroup.layoutManager = LinearLayoutManager(requireContext())
        mView.rvProductGroup.setHasFixedSize(true)
        mView.rvProductGroup.isNestedScrollingEnabled = false
        mView.rvProductGroup.adapter = adapter
        adapter.addAll(productList)

        adapter.setOnItemClickListener { _, position ->
            val bundle = bundleOf(Pair("product_group_taking_order", adapter.getItem(position).productBrandId), Pair("scheduled_plans", outlet))
            Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                .navigate(R.id.action_to_product_search_taking_order, bundle)
        }
    }

    override fun onError(message: String?) {
        showError(message)
    }

}