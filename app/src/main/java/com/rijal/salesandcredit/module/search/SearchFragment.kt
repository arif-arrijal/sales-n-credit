package com.rijal.salesandcredit.module.search

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentSearchBinding
import com.rijal.salesandcredit.helpers.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment(private val enum: SearchEnum, val searchInterface: SearchInterface) : DialogFragment() {
    lateinit var binding: FragmentSearchBinding
    private val viewModel: SearchViewModel by viewModel()
    private lateinit var adapter: EasyAdapter

    companion object {
        fun newInstance(enum: SearchEnum, searchInterface: SearchInterface): SearchFragment {
            return SearchFragment(enum, searchInterface)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(-1, -1)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.search()
    }

    private fun initView() {
        viewModel.enum = enum.toString()
        binding.llCancel.setOnClickListener { this.dismiss() }
        binding.ibSearch.setOnClickListener { viewModel.search() }
        binding.llAdd.setOnClickListener {
            startActivity(Intent(activity, DetailActivity::class.java).apply {
                putExtra(IntentExtra.ROUTE, viewModel.enum)
            })
        }
        adapter = object : EasyAdapter(R.layout.item_select_list, viewModel.dataList.valueOrDefault()) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val selectedData = viewModel.dataList.valueOrDefault()[position]
                holder.itemView.findViewById<Button>(R.id.btn_choose)?.apply {
                    this.setOnClickListener {
                        if (viewModel.enum == SearchEnum.ITEM.name) {
                            searchInterface.onSearchFinish(viewModel.itemList.valueOrDefault()[position].toJson())
                        } else {
                            searchInterface.onSearchFinish(selectedData.toJson())
                        }

                        dismiss()
                    }
                }
            }
        }
        binding.rvMain.adapter = adapter
    }

    private fun observe() {
        viewModel.dataList.observe(viewLifecycleOwner) {
            binding.tvNoData.visible(it.isEmpty())
            binding.rvMain.visible(it.isNotEmpty())
            adapter.notifyDataSetChanged()
        }
    }
}