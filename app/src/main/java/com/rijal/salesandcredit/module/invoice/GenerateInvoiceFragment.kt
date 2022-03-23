package com.rijal.salesandcredit.module.invoice

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
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentGenerateInvoiceBinding
import com.rijal.salesandcredit.db.entity.Invoice
import com.rijal.salesandcredit.helpers.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat
import java.util.*

class GenerateInvoiceFragment : Fragment() {
    lateinit var parent: DetailActivity
    lateinit var binding: FragmentGenerateInvoiceBinding
    private val viewModel: InvoiceViewModel by viewModel()
    private lateinit var datePickerDialog: DatePickerDialog
    private var calendar = Calendar.getInstance()
    private val args: GenerateInvoiceFragmentArgs by navArgs()
    val numberFormat = DecimalFormat("Rp #,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGenerateInvoiceBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle(if (args.id > 0) "Ubah Tagihan" else "Buat Tagihan")
        viewModel.id.value = args.id
        viewModel.transactionId = args.transactionId
        initView()
        initListener()
        observe()
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

    fun initView() {
        viewModel.checkCollectibility()

        if (args.id > 0) {
            viewModel.findOne()
            binding.etTotalInstallment.isEnabled = false
            binding.etInstallmentAt.isEnabled = false
            binding.etRemainingUnpaid.isEnabled = false
            binding.tvAdd.text = getString(R.string.ubah)
        }
    }

    fun initListener() {
        binding.etMaximumDueDate.setOnClickListener { showDateDialog(viewModel.maximumDueDate, binding.tilMaximumDueDate) }
        binding.etPaymentDueDate.setOnClickListener { showDateDialog(viewModel.invoiceDueDate, binding.tilPaymentDueDate) }
        binding.etInputDate.setOnClickListener { showDateDialog(viewModel.inputDate, binding.tilInputDate) }
        binding.cvAdd.setOnClickListener {
            if (args.id > 0) {
                update()
            } else {
                save()
            }
        }

        binding.etTotalInstallment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etTotalInstallment.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)
                val totalUnpaid = (viewModel.selectedTransaction.value?.nilaiTransaksi ?: 0.0) - parsedData - (viewModel.totalPaid.value ?: 0.0)

                viewModel.totalPayment.value = parsedData
                viewModel.totalUnpaid.value = totalUnpaid

                binding.etRemainingUnpaid.setText(numberFormat.format(totalUnpaid))
                binding.etTotalInstallment.setText(formatted)
                binding.etTotalInstallment.setSelection(binding.etTotalInstallment.length())
                binding.etTotalInstallment.addTextChangedListener(this)

                isBesarPembayaranError()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilTotalInstallment.error = null
            }
        })
        binding.etRemainingUnpaid.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etRemainingUnpaid.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.totalUnpaid.value = parsedData

                binding.etRemainingUnpaid.setText(formatted)
                binding.etRemainingUnpaid.setSelection(binding.etRemainingUnpaid.length())
                binding.etRemainingUnpaid.addTextChangedListener(this)

                isSisaBelumDibayarError()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilRemainingUnpaid.error = null
            }
        })
        binding.etJumlahTerbayar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etJumlahTerbayar.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)
                var totalUnpaid = (viewModel.selectedTransaction.value?.nilaiTransaksi ?: 0.0) - parsedData - (viewModel.totalPaid.value ?: 0.0)
                if (totalUnpaid < 0.0) {
                    totalUnpaid = 0.0
                }
                viewModel.totalUnpaid.value = totalUnpaid
                viewModel.totalTerbayar.value = parsedData

                binding.etRemainingUnpaid.setText(numberFormat.format(totalUnpaid))

                binding.etJumlahTerbayar.setText(formatted)
                binding.etJumlahTerbayar.setSelection(formatted.length)
                binding.etJumlahTerbayar.addTextChangedListener(this)

                isJumlahTerbayarError()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilJumlahTerbayar.error = null
            }
        })

        binding.etInstallmentAt.watch(
            onChanged = {
                binding.tilInstallmentAt.error = null
            },
            onAfterChanged = {
                isAngsuranKeError()
            }
        )
    }

    fun observe() {
        viewModel.collectibility.observe(viewLifecycleOwner) {
            if (it != null) {
                val color = when (it) {
                    0 -> R.color.blue_700
                    1 -> R.color.yellow_700
                    2 -> R.color.red_700
                    else -> R.color.transparent
                }

                binding.cvStatus.setBackgroundColor(colorFromId(color))
            }
        }
        viewModel.checkCollectibilityState.observe(viewLifecycleOwner) {
            it.read {
                parent.loadingDialog.visible(it.isLoading)
            }
        }
        viewModel.insertState.observe(viewLifecycleOwner) {
            it.read {
                it.message?.apply {
                    errorSnackbar(this)
                }
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil ditambahkan.") {
                        parent.onBackPressed()
                    }
                }
            }
        }
        viewModel.updateState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil diperbarui.") {
                        parent.onBackPressed()
                    }
                }
            }
        }

        viewModel.selectedTransaction.observe(viewLifecycleOwner) {
            if (viewModel.id.value == 0) {
                val angsuranTerbaru =  it.angsuranTerbaru + 1
                viewModel.installmentAt.value = angsuranTerbaru
                binding.etInstallmentAt.setText(angsuranTerbaru.toString())

                setTotalUnpaid()
            }
        }

        viewModel.selectedInvoice.observe(viewLifecycleOwner) {
            binding.etTotalInstallment.setText(numberFormat.format(it.totalPayment))
            binding.etInstallmentAt.setText(it.installmentAt.toString())
            binding.etRemainingUnpaid.setText(numberFormat.format(it.totalUnpaid))
            binding.etJumlahTerbayar.setText(numberFormat.format(it.totalTerbayar))
            if (viewModel.inputDate.valueOrDefault().isNotEmpty()) {
                parent.setToolbarTitle("Detail Tagihan")
                binding.cvAdd.visible(false)
            }

            setTotalUnpaid()
        }

        viewModel.totalPaid.observe(viewLifecycleOwner) {
            val totalValue = viewModel.selectedTransaction.value?.nilaiTransaksi ?: 0.0
            val totalPaid = (viewModel.totalPaid.value ?: 0.0)
            var totalUnpaid =  totalValue - totalPaid
            if (totalUnpaid < 0.0) {
                totalUnpaid = 0.0
            }
            viewModel.totalUnpaid.value = totalUnpaid
            binding.etRemainingUnpaid.setText(numberFormat.format(totalUnpaid))
        }
    }

    fun setTotalUnpaid() {
        val totalValue = viewModel.selectedTransaction.value?.nilaiTransaksi ?: 0.0
        val totalPaid = (viewModel.totalPaid.value ?: 0.0)
        var totalUnpaid =  totalValue - totalPaid
        if (totalUnpaid < 0.0) {
            totalUnpaid = 0.0
        }
        viewModel.totalUnpaid.value = totalUnpaid
        binding.etRemainingUnpaid.setText(numberFormat.format(totalUnpaid))
    }
    fun save() {
        hideKeyboard()
        var isError = false
        if (isBesarPembayaranError()) isError = true
        if (isAngsuranKeError()) isError = true
        if (isSisaBelumDibayarError()) isError = true
        if (isPembayaranMaksimalTanggalError()) isError = true
        if (isJatuhTempoPelunasanError()) isError = true
        if (isError) return

        val data = Invoice(
            transactionId = viewModel.transactionId,
            totalPayment = viewModel.totalPayment.value ?: 0.0,
            installmentAt = binding.etInstallmentAt.intVal(),
            maximumDueDate = viewModel.maximumDueDate.valueOrDefault().toDate("dd-MM-yyyy"),
            invoiceDueDate = viewModel.invoiceDueDate.valueOrDefault().toDate("dd-MM-yyyy")
        )

        viewModel.save(data)
    }

    fun update() {
        hideKeyboard()
        var error = false
        if (isInputDateError()) error = true
        if (isJumlahTerbayarError()) error = true
        if (error) return

        val data = Invoice(
            invoiceId = viewModel.id.valueOrDefault(),
            transactionId = viewModel.transactionId,
            totalPayment = viewModel.totalPayment.value ?: 0.0,
            installmentAt = binding.etInstallmentAt.intVal(),
            maximumDueDate = viewModel.maximumDueDate.valueOrDefault().toDate("dd-MM-yyyy"),
            totalUnpaid = viewModel.totalUnpaid.value ?: 0.0,
            invoiceDueDate = viewModel.invoiceDueDate.valueOrDefault().toDate("dd-MM-yyyy"),
            inputDate = viewModel.inputDate.valueOrDefault().toDate("dd-MM-yyyy"),
            totalTerbayar = viewModel.totalTerbayar.value ?: 0.0
        )
        viewModel.update(data)
    }

    private fun isBesarPembayaranError(): Boolean {
        return if (viewModel.totalPayment.value == null) {
            binding.tilTotalInstallment.error = "Besar pembayaran tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isAngsuranKeError(): Boolean {
        return if (binding.etInstallmentAt.intValOrNull() == null) {
            binding.tilInstallmentAt.error = "Angsuran ke tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isSisaBelumDibayarError(): Boolean {
        return if (viewModel.totalUnpaid.value == null) {
            binding.tilRemainingUnpaid.error = "Sisa yang belum dibayar tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isJumlahTerbayarError(): Boolean {
        return if (viewModel.totalUnpaid.value == null) {
            binding.tilRemainingUnpaid.error = "Jumlah terbayar tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isPembayaranMaksimalTanggalError(): Boolean {
        return if (viewModel.maximumDueDate.valueOrDefault().isEmpty()) {
            binding.tilMaximumDueDate.error = "Tanggal pembayaran maksimal tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isJatuhTempoPelunasanError(): Boolean {
        return if (viewModel.invoiceDueDate.valueOrDefault().isEmpty()) {
            binding.tilPaymentDueDate.error = "Tanggal jatuh tempo pelunasan tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isInputDateError(): Boolean {
        return if (viewModel.inputDate.valueOrDefault().isEmpty()) {
            binding.tilInputDate.error = "Tanggal input ke tidak boleh kosong"
            true
        } else {
            false
        }
    }
}