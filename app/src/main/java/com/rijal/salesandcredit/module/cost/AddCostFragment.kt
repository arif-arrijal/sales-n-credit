package com.rijal.salesandcredit.module.cost

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentAddCostBinding
import com.rijal.salesandcredit.db.entity.Cost
import com.rijal.salesandcredit.helpers.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat
import java.util.*

class AddCostFragment : Fragment() {
    lateinit var parent: DetailActivity
    private lateinit var binding: FragmentAddCostBinding
    private val viewModel: CostViewModel by viewModel()
    private val args: AddCostFragmentArgs by navArgs()
    val numberFormat = DecimalFormat("Rp #,###")
    private lateinit var datePickerDialog: DatePickerDialog
    private var calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCostBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        viewModel.id = args.id

        parent.setToolbarTitle(if (viewModel.id == 0 || viewModel.id == -1) "Tambah Data" else "Ubah Data")
        initView()

        initListener()
        observe()
    }
    private fun initView() {
        if (viewModel.id > 0) {
            binding.etTitle.isEnabled = false
            binding.etDescription.isEnabled = false
            binding.etCost.isEnabled = false
            binding.etInputDate.isEnabled = false
            binding.tvAdd.text = "Ubah"
            binding.cvAdd.visible(false)
            viewModel.findById()
        }
    }
    private fun initListener() {
        binding.etInputDate.setOnClickListener { showDateDialog(viewModel.createdAt, binding.tilInputDate) }
        binding.cvAdd.setOnClickListener { insertData() }

        binding.etTitle.watch(
            onChanged = {
                binding.tilTitle.error = null
            },
            onAfterChanged = {
                isTitleError()
            }
        )
        binding.etDescription.watch(
            onChanged = {
                binding.tilDescription.error = null
            },
            onAfterChanged = {
                isDescriptionError()
            }
        )

        binding.etCost.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etCost.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.amount.value = parsedData

                binding.etCost.setText(formatted)
                binding.etCost.setSelection(binding.etCost.length())
                binding.etCost.addTextChangedListener(this)

                isAmountError()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilCost.error = null
            }
        })
    }

    private fun showDateDialog(variable: MutableLiveData<String>, til: TextInputLayout) {
        val date = variable.valueOrDefault().toDate("dd-MM-yyyy")
        calendar.time = date

        datePickerDialog = DatePickerDialog(parent,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                    .apply { set(year, month, dayOfMonth) }
                    .time.toFormattedString("dd-MM-yyyy")
                variable.value = selectedDate
                til.error = null
                hideKeyboard()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)

        ).apply {
            show()
        }
    }

    private fun observe() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            binding.cvAdd.isEnabled = !it
        }
        viewModel.insertState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data sukses ditambahkan") {
                        if (args.id == -1) {
                            parent.finish()
                        } else {
                            parent.onBackPressed()
                        }
                    }
                }
            }
        }
        viewModel.selectedData.observe(viewLifecycleOwner) {
            binding.etCost.setText(numberFormat.format(it.amount))
        }
    }
    private fun insertData() {
        var isError = false
        if (isTitleError()) isError = true
        if (isDescriptionError()) isError = true
        if (isAmountError()) isError = true
        if (isCreatedAtError()) isError = true

        if (isError) return
        val data = Cost(
            title = viewModel.title.valueOrDefault(),
            description = viewModel.description.valueOrDefault(),
            amount = viewModel.amount.value ?: 0.0,
            createdAt = viewModel.createdAt.valueOrDefault().toDate("dd-MM-yyyy"))

        viewModel.insert(data)
    }
    private fun isTitleError(): Boolean {
        return if (viewModel.title.value.isNullOrEmpty()) {
            binding.tilTitle.error = "Judul tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isDescriptionError(): Boolean {
        return if (viewModel.description.value.isNullOrEmpty()) {
            binding.tilDescription.error = "Deskripsi tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isAmountError(): Boolean {
        val purchasePrice = viewModel.amount.value
        if (purchasePrice == null) {
            binding.tilCost.error = "Biaya tidak boleh kosong."
            return true
        }
        if (purchasePrice.isNaN()) {
            binding.tilCost.error = "Biaya tidak sesuai format."
            return true
        }
        return false
    }
    private fun isCreatedAtError(): Boolean {
        return if (viewModel.createdAt.valueOrDefault().isEmpty()) {
            binding.tilInputDate.error = "Tanggal tidak boleh kosong"
            true
        } else {
            false
        }
    }
}