package com.rijal.salesandcredit.module.transaction

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
import com.rijal.salesandcredit.databinding.FragmentTransactionListBinding
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.helpers.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class TransactionListFragment : Fragment() {
    lateinit var parent: DetailActivity
    lateinit var binding: FragmentTransactionListBinding
    lateinit var adapter: EasyAdapter
    private val viewModel: TransactionViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Perdagangan")

        initView()
        initListener()
        observe()
    }

    private fun initView() {
        adapter = object : EasyAdapter(R.layout.item_transaction_list, viewModel.transactionList.valueOrDefault()) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val selectedData = viewModel.transactionList.valueOrDefault()[position]

                holder.itemView.findViewById<CardView>(R.id.cv_detail).setOnClickListener {
                    findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailFragment(selectedData.transactionId))
                }
                holder.itemView.findViewById<CardView>(R.id.cv_invoice).setOnClickListener {
                    findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToInvoiceListFragment(selectedData.transactionId))
                }
            }
        }
        binding.rvMain.adapter = adapter
        viewModel.findTransactionByType()
    }

    private fun initListener() {
        RxTextView.textChanges(binding.etSearch)
            .map(CharSequence::toString)
            .debounce(600, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.findTransactionByType()
            }
        
        binding.fabAddNew.setOnClickListener {
            findNavController().navigate(TransactionListFragmentDirections.actionTransactionListFragmentToTransactionDetailFragment(0))
        }
    }

    private fun observe() {
        viewModel.transactionList.observe(viewLifecycleOwner) {
            binding.rvMain.visible(it.isNotEmpty())
            binding.tvNoData.visible(it.isEmpty())
            adapter.notifyDataSetChanged()
        }
    }
}