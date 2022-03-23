package com.rijal.salesandcredit.module.zakat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentZakatBinding
import com.rijal.salesandcredit.helpers.SharedPref
import com.rijal.salesandcredit.helpers.getPrefString
import org.koin.androidx.viewmodel.ext.android.viewModel

class ZakatFragment : Fragment() {

    private lateinit var parent: DetailActivity
    lateinit var binding: FragmentZakatBinding
    private val viewModel: ZakatViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentZakatBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Kalkulator Zakat / Shodaqoh")
        viewModel.totalTabungan.value = parent.getPrefString(SharedPref.SAVINGS, "0")?.toDouble()
        calculate()
    }

    private fun calculate() {
        viewModel.calculate()
    }
}