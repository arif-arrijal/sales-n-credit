package com.rijal.salesandcredit.module.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentAddItemBinding
import com.rijal.salesandcredit.databinding.FragmentAddPersonBinding
import com.rijal.salesandcredit.databinding.FragmentReportBinding
import com.rijal.salesandcredit.module.person.PersonViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportFragment : Fragment() {

    lateinit var parent: DetailActivity
    private lateinit var binding: FragmentReportBinding
    private val viewModel: ReportViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Laporan")
        viewModel.calculateReportData()
    }
}