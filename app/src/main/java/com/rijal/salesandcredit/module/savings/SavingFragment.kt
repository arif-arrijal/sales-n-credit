package com.rijal.salesandcredit.module.savings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentSavingBinding
import com.rijal.salesandcredit.databinding.FragmentZakatBinding
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.module.zakat.ZakatViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat

class SavingFragment : Fragment() {

    private lateinit var parent: DetailActivity
    lateinit var binding: FragmentSavingBinding
    private val viewModel: SavingsViewModel by viewModel()
    val numberFormat = DecimalFormat("Rp #,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSavingBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Tabungan")
        viewModel.savings.value = parent.getPrefString(SharedPref.SAVINGS, "0")?.toLong()
        initListener()
    }

    private fun initListener() {
        binding.btnExecute.setOnClickListener {
            if (isAmountError()) return@setOnClickListener
            execute()
        }
        binding.btnAmbil.setOnClickListener {
            viewModel.mode.value = "put"
            viewModel.amount.value = 0
            val formatted = numberFormat.format(0)
            binding.etAmount.setText(formatted)
        }
        binding.btnTambah.setOnClickListener {
            viewModel.mode.value = "add"
            viewModel.amount.value = 0
            val formatted = numberFormat.format(0)
            binding.etAmount.setText(formatted)
        }
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etAmount.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toLong()
                val formatted = numberFormat.format(parsedData)

                viewModel.amount.value = parsedData

                binding.etAmount.setText(formatted)
                binding.etAmount.setSelection(formatted.length)
                binding.etAmount.addTextChangedListener(this)

                isAmountError()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilAmount.error = null
            }
        })
    }

    private fun isAmountError(): Boolean {
        val purchasePrice = viewModel.amount.value
        if (purchasePrice == null) {
            binding.tilAmount.error = "Data tidak boleh kosong."
            return true
        }
        return false
    }

    private fun execute() {
        hideKeyboard()

        val amount = viewModel.amount.value ?: 0L
        val mode = viewModel.mode.valueOrDefault()
        val savings = viewModel.savings.value ?: 0L
        val newValue: Long

        if (mode == "add") {
            newValue = savings + amount
        } else {
            newValue = savings - amount
            if (newValue < 0) {
                errorSnackbar("Tabungan tidak boleh kurang dari Rp 0")
                return
            }
        }

        parent.putPrefData(SharedPref.SAVINGS, newValue)
        viewModel.savings.value = newValue
        viewModel.mode.value = ""
        viewModel.amount.value = 0

        val formatted = numberFormat.format(0)
        binding.etAmount.setText(formatted)

        if (mode == "add") {
            successSnackbar("Tabungan berhasil ditambah.")
        } else {
            successSnackbar("Tabungan berhasil diambil.")
        }
    }
}