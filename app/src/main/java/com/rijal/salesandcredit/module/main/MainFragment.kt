package com.rijal.salesandcredit.module.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentMainBinding
import com.rijal.salesandcredit.helpers.SearchEnum
import com.rijal.salesandcredit.model.UiModel

class MainFragment : Fragment() {
    lateinit var parent: DetailActivity
    lateinit var binding: FragmentMainBinding
    private lateinit var menuAdapter: EasyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Dashboard")

        if (parent.openedFragment.isNotEmpty()) {
            handleRedirect(parent.openedFragment)
            parent.openedFragment = ""
        }
        initView()
    }

    private fun handleRedirect(openedFragment: String) {
        when (openedFragment) {
            SearchEnum.ITEM.name -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToAddItemFragment(-1))
            SearchEnum.PERSON.name -> findNavController().navigate(MainFragmentDirections.actionMainFragmentToAddPersonFragment(-1))
        }
    }

    private fun initView() {
        val data = getMenuList()
        menuAdapter = object : EasyAdapter(R.layout.item_menu, data) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)

                val selectedData = data[position]
                val ivLogo = holder.itemView.findViewById<ImageView>(R.id.iv_logo)

                Glide.with(parent)
                    .asBitmap()
                    .load(selectedData.imageId)
                    .into(ivLogo)

                holder.itemView.setOnClickListener { selectedData.func.invoke() }
            }
        }
        binding.rvMain.adapter = menuAdapter
    }

    private fun getMenuList(): ArrayList<UiModel.DashboardMenu> {
        return arrayListOf(
            UiModel.DashboardMenu("Perdagangan", R.drawable.ic_cart) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToTransactionListSales())
            },
            UiModel.DashboardMenu("Suntikan Modal", R.drawable.ic_funding) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToCapitalInjectionListFragment())
            },
            UiModel.DashboardMenu("Nasabah", R.drawable.ic_baseline_person_24) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToPersonListFragment())
            },
            UiModel.DashboardMenu("Barang", R.drawable.ic_groceries) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToListItemFragment())
            },
            UiModel.DashboardMenu("Biaya", R.drawable.ic_money_bag) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToCostListFragment())
            },
            UiModel.DashboardMenu("Tabungan", R.drawable.ic_savings) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToSavingFragment())
            },
            UiModel.DashboardMenu("Laporan", R.drawable.ic_profit_report) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToReportFragment())
            },
            UiModel.DashboardMenu("Kalkulator Zakat", R.drawable.ic_zakat) {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToZakatFragment())
            },
            UiModel.DashboardMenu("Keluar", R.drawable.ic_logout) {
                parent.logout()
            },
        )
    }
}