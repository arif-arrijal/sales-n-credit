package com.rijal.salesandcredit.module.cost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding.widget.RxTextView
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentCostListBinding
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.helpers.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class CostListFragment : Fragment() {

    lateinit var parent: DetailActivity
    lateinit var binding: FragmentCostListBinding
    private lateinit var adapter: EasyAdapter
    private val viewModel: CostViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCostListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Daftar Biaya")

        initView()
        initListener()
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.findAll()
    }

    private fun initView() {
        adapter = object : EasyAdapter(R.layout.item_cost_list, viewModel.dataList.valueOrDefault()) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)

                val selectedData = viewModel.dataList.valueOrDefault()[position]
                val cvDetail = holder.itemView.findViewById(R.id.cv_detail) as CardView

                cvDetail.setOnClickListener {
                    findNavController().navigate(CostListFragmentDirections.actionCostListFragmentToAddCostFragment(selectedData.id))
                }
            }
        }
        binding.rvMain.adapter = adapter
    }

    private fun initListener() {
        binding.fabAddNew.setOnClickListener {
            findNavController().navigate(CostListFragmentDirections.actionCostListFragmentToAddCostFragment(0))
        }
    }

    private fun observe() {
        RxTextView.textChanges(binding.etSearch)
            .map(CharSequence::toString)
            .debounce(600, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.findAll()
            }

        viewModel.dataList.observe(viewLifecycleOwner) {
            binding.rvMain.visible(it.isNotEmpty())
            binding.tvNoData.visible(it.isEmpty())
            adapter.notifyDataSetChanged()
        }
    }
}