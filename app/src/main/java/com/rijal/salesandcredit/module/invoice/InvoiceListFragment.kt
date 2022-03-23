package com.rijal.salesandcredit.module.invoice

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentInvoiceListBinding
import com.rijal.salesandcredit.helpers.errorSnackbar
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.helpers.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class InvoiceListFragment : Fragment() {

    lateinit var parent: DetailActivity
    lateinit var binding: FragmentInvoiceListBinding
    lateinit var adapter: EasyAdapter
    private val viewModel: InvoiceViewModel by viewModel()
    private val args: InvoiceListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Tagihan")
        viewModel.transactionId = args.id
        initView()
        initListener()
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.findAll()
        viewModel.findUnpaid()
    }

    private fun initView() {
        adapter = object : EasyAdapter(R.layout.item_invoice_list, viewModel.invoiceList.valueOrDefault()) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val selectedData = viewModel.invoiceList.valueOrDefault()[position]

                holder.itemView.findViewById<CardView>(R.id.cv_detail).setOnClickListener {
                    findNavController().navigate(InvoiceListFragmentDirections.actionInvoiceListFragmentToGenerateInvoiceFragment(selectedData.invoiceId, args.id))
                }
                holder.itemView.findViewById<CardView>(R.id.cv_invoice).setOnClickListener {
                    viewModel.generateInvoice(selectedData)
                }
            }
        }
        binding.rvMain.adapter = adapter
    }

    private fun initListener() {
        binding.fabAddNew.setOnClickListener {
            if (viewModel.unpaidInvoice.value != null) {
                val invoice = viewModel.unpaidInvoice.value!!
                errorSnackbar("Tagihan ke ${invoice.installmentAt} belum terbayar. Silahkan lengkapi tagihan ke ${invoice.installmentAt} terlebih dahulu.")
            } else {
                findNavController().navigate(InvoiceListFragmentDirections.actionInvoiceListFragmentToGenerateInvoiceFragment(0, viewModel.transactionId))
            }
        }
    }

    private fun observe() {
        viewModel.invoiceList.observe(viewLifecycleOwner) {
            binding.rvMain.visible(it.isNotEmpty())
            binding.tvNoData.visible(it.isEmpty())
            adapter.notifyDataSetChanged()
        }
        viewModel.generateInvoiceState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.let {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, it)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            }
        }
    }
}