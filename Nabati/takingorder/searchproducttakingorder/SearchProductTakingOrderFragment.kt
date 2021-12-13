package com.nabati.sfa.modul.takingorder.searchproducttakingorder

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.nabati.sfa.R
import com.nabati.sfa.base.BaseFragment
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrder
import com.nabati.sfa.model.local.invoicetakingorder.InvoiceTakingOrderViewModel
import com.nabati.sfa.model.product.ProductTakingOrder
import com.nabati.sfa.model.product.ScheduledPlans
import com.nabati.sfa.modul.takingorder.inputproducttakingorder.InputProductTakingOrderDialog
import com.nabati.sfa.modul.takingorder.inputproducttakingorder.OnSaveInputProduct
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search_product.view.*
import org.koin.android.architecture.ext.viewModel

class SearchProductTakingOrderFragment : BaseFragment<SearchProductTakingOrderPresenter>(),
    ISearchProductTakingOrderView,
    OnSaveInputProduct {

    lateinit var mView: View
    private var adapter: SearchProductTakingOrderAdapter? = null
    private var productBrandId: String? = ""
    private var searchView: SearchView? = null
    private var productList: MutableList<ProductTakingOrder>? = mutableListOf()
    private var invoiceTakingOrderList: MutableList<InvoiceTakingOrder>? = mutableListOf()
    private val lastSearchViewModel by viewModel<InvoiceTakingOrderViewModel>()
    private val compositeDisposable = CompositeDisposable()
    private var outlet: ScheduledPlans? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_search_product, container, false)
        setHasOptionsMenu(true)
        return mView
    }

    override fun attachPresenter(): SearchProductTakingOrderPresenter {
        return SearchProductTakingOrderPresenter(requireContext(), this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar("Select Product")
        initAdapter()
        initOnClickListener()
        onViewInit()
    }

    override fun onViewInit() {
        arguments?.let {
            val args = SearchProductTakingOrderFragmentArgs.fromBundle(it)
            outlet = args.scheduledPlans
            productBrandId = args.productGroupTakingOrder
        }
        presenter?.getData()

        lastSearchViewModel.listenInvoiceTakingOrderResult().observe(requireActivity(), Observer {
            invoiceTakingOrderList = it.toMutableList()
        })

    }

    override fun onSuccessRequest(productTakingOrder: MutableList<ProductTakingOrder>?) {
        productList = productTakingOrder
        initDataProducts(productTakingOrder)
    }

    private fun initOnClickListener() {

    }

    private fun initAdapter() {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_product_search, menu)
        val searchManager =
            requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu.findItem(R.id.action_search)?.actionView as SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView?.maxWidth = Int.MAX_VALUE

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (!productList?.isNullOrEmpty()!!) {
                    if (query?.length!! > 2) {
                        val productGroups = productList?.filter {
                            it.productName?.contains(
                                query,
                                true
                            )!! or (it.productId?.contains(query, true)!!)
                        }
                        initDataProducts(productGroups?.toMutableList())
                    } else if (query.isEmpty()) {
                        initDataProducts(productList)
                    }
                }
                return false
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                Navigation.findNavController(requireActivity(), R.id.navHostFragment).navigateUp()
                true
            }
            R.id.action_search -> {
                true
            }
            else -> return NavigationUI.onNavDestinationSelected(
                item, view?.findNavController()!!
            ) || super.onOptionsItemSelected(item)
        }
    }

    private fun initDataProducts(products: MutableList<ProductTakingOrder>?) {
        if (!isAdded)
            return
        adapter = SearchProductTakingOrderAdapter(requireContext())
        mView.rvSearchProduct.layoutManager = LinearLayoutManager(requireContext())
        mView.rvSearchProduct.setHasFixedSize(true)
        mView.rvSearchProduct.adapter = adapter
        mView.rvSearchProduct.setItemViewCacheSize(products?.size!!)
        mView.rvSearchProduct.isNestedScrollingEnabled = false

        if (productBrandId != null) {
            val searchProducts = products?.filter {
                it.productBrandId.equals(productBrandId, true)
            }
            if (searchProducts.isNullOrEmpty()) {
                mView.tvEmptyData.visibility = View.VISIBLE
            } else
                mView.tvEmptyData.visibility = View.GONE
            adapter?.addAll(searchProducts?.toMutableList())
        } else {
            if (products.isNullOrEmpty()) {
                mView.tvEmptyData.visibility = View.VISIBLE
            } else
                mView.tvEmptyData.visibility = View.GONE
            adapter?.addAll(products)
        }

        adapter?.setOnItemClickListener { view, position ->
            val dialog = InputProductTakingOrderDialog.newInstance(
                adapter?.getItem(position),
                invoiceTakingOrderList,
                this
            )
            dialog.show(requireFragmentManager(), dialog.tag)
        }

    }

    override fun onSaveProduct(invoices: InvoiceTakingOrder) {
        val bundle = bundleOf(Pair("scheduled_plans", outlet))
        Navigation.findNavController(requireActivity(), R.id.navHostFragment)
            .navigate(R.id.action_to_taking_order, bundle)
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

    override fun onError(message: String?) {
        showError(message)
    }

    override fun onStartLoad() {
        mView.pbDirectSelling.visibility = View.VISIBLE
    }

    override fun onFinishLoad() {
        mView.pbDirectSelling.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}
