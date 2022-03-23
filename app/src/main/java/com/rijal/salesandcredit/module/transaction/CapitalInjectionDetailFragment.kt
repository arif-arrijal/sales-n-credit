package com.rijal.salesandcredit.module.transaction

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentCapitalInjectionDetailBinding
import com.rijal.salesandcredit.db.entity.Transaction
import com.rijal.salesandcredit.db.entity.TransactionWithItems
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.model.UiModel
import com.rijal.salesandcredit.module.search.SearchFragment
import com.rijal.salesandcredit.module.search.SearchInterface
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class CapitalInjectionDetailFragment : Fragment(), SearchInterface {

    private lateinit var parent: DetailActivity
    lateinit var binding: FragmentCapitalInjectionDetailBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private var calendar = Calendar.getInstance()
    private val viewModel: CapitalInjectionViewModel by viewModel()
    private var type: SearchEnum? = null
    private val args: TransactionDetailFragmentArgs by navArgs()
    lateinit var currentPhotoPath: String
    var selectedImg = ""
    val numberFormat = DecimalFormat("Rp #,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCapitalInjectionDetailBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Detail")
        viewModel.id = args.id

        initView()
        initListener()
        observe()
    }

    private fun initView() {
        if (viewModel.id > 0) {
            binding.btnAkadPhoto.visible(false)
            binding.btnPenyerahanPhoto.visible(false)
            binding.cvAdd.visible(false)
            viewModel.findOne()
        }
    }

    private fun initListener() {
        binding.btnAkadPhoto.setOnClickListener {
            showDialogPickImage("akad")
        }
        binding.btnPenyerahanPhoto.setOnClickListener {
            showDialogPickImage("penyerahan")
        }
        binding.etTglAkad.setOnClickListener { showDateDialog(viewModel.tglAkad, binding.tilTglAkad) }
        binding.etTglJatuhTempo.setOnClickListener { showDateDialog(viewModel.tglJatuhTempo, binding.tilTglJatuhTempo) }
        binding.etTglPembayaran.setOnClickListener { showDateDialog(viewModel.tglPembayaran, binding.tilTglPembayaran) }
        binding.etNasabah.setOnClickListener {
            type = SearchEnum.PERSON
            SearchFragment.newInstance(SearchEnum.PERSON, this).apply {
                this.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                this.dialog?.window?.setLayout(-1, -1)
                show(parent.supportFragmentManager, "")
            }
        }
        binding.tvAdd.setOnClickListener {
            save()
        }

        binding.etBusinessType.watch(
            onChanged = {
                binding.tilBusinessType.error = null
            },
            onAfterChanged = {
                isJenisUsahaError()
            }
        )
        binding.etTotalCapitalInjection.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etTotalCapitalInjection.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.totalCapitalInjection.value = parsedData

                binding.etTotalCapitalInjection.setText(formatted)
                binding.etTotalCapitalInjection.setSelection(binding.etTotalCapitalInjection.length())
                binding.etTotalCapitalInjection.addTextChangedListener(this)

                if (!isJumlahSuntikanModalError()) {
                    checkTotalRevenue()
                }

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilTotalCapitalInjection.error = null
            }
        })

        binding.etRevenue.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etRevenue.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.totalRevenue.value = parsedData

                binding.etRevenue.setText(formatted)
                binding.etRevenue.setSelection(binding.etRevenue.length())
                binding.etRevenue.addTextChangedListener(this)

                if (!isTotalRevenueError()) {
                    checkTotalRevenue()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilTotalCapitalInjection.error = null
            }
        })

        binding.etTotalInstallment.watch(
            onChanged = {
                binding.tilTotalInstallment.error = null

                if (it.toIntOrNull() != null) {
                    viewModel.totalInstallment.value = it.toInt()
                }
                calculatePersentasePerAngsuran()
            },
            onAfterChanged = {
                isJumlahAngsuranError()
            }
        )
    }

    private fun calculatePersentasePerAngsuran() {
        val totalCapitalInjection = viewModel.totalCapitalInjection.value ?: 0.0
        val totalRevenue = viewModel.totalRevenue.value ?: 0.0
        val totalInstallment = viewModel.totalInstallment.value ?: 0
        var totalRevenuePercentage = 0.0
        var totalPersentasePerAngsuran = 0.0
        var totalRevenuePerAngsuran = 0.0

        if (totalCapitalInjection > 0.0 && totalRevenue > 0.0 && totalInstallment > 0) {
            totalRevenuePercentage = (totalRevenue / totalCapitalInjection) * 100
            totalPersentasePerAngsuran = totalRevenuePercentage / totalInstallment
            totalRevenuePerAngsuran = totalRevenue / totalInstallment
        }

        binding.etPersentasePerAngsuran.setText(totalPersentasePerAngsuran.toString())
        binding.etUntungPerAngsuran.setText(totalRevenuePerAngsuran.toRupiah())
    }

    private fun checkTotalRevenue() {
        val totalCapitalInjection = viewModel.totalCapitalInjection.value ?: 0.0
        val totalRevenue = viewModel.totalRevenue.value ?: 0.0
        val totalTransaction = totalCapitalInjection + totalRevenue
        var totalRevenuePercentage = 0.0

        if (totalCapitalInjection > 0.0 && totalRevenue > 0.0) {
            totalRevenuePercentage = (totalRevenue / totalCapitalInjection) * 100
        }

        viewModel.totalRevenuePercentage.value = totalRevenuePercentage
        binding.etRevenuePercentage.setText(totalRevenuePercentage.toString())
        binding.etTotalTransaction.setText(totalTransaction.toRupiah())

        calculatePersentasePerAngsuran()
    }

    private fun checkTotalRevenueFromPercentage(transactionData: TransactionWithItems) {
        viewModel.totalCapitalInjection.value = transactionData.totalCapital
        viewModel.totalRevenuePercentage.value = transactionData.profitSharingPercentage

        val totalCapitalInjection = viewModel.totalCapitalInjection.value ?: 0.0
        val totalRevenuePercentage = viewModel.totalRevenuePercentage.value ?: 0.0
        val totalRevenue = totalRevenuePercentage / 100 * totalCapitalInjection
        val totalTransaction = totalRevenue + totalCapitalInjection

        viewModel.totalRevenue.value = totalRevenue
        binding.etRevenue.setText(numberFormat.format(totalRevenue))
        binding.etTotalTransaction.setText(totalTransaction.toRupiah())
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
        viewModel.insertState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil ditambahkan.") {
                        parent.onBackPressed()
                    }
                }
            }
        }

        viewModel.selectedData.observe(viewLifecycleOwner) {
            binding.etBusinessType.setText(it.businessType)
            binding.etTotalCapitalInjection.setText(numberFormat.format(it.totalCapital))
            binding.etRevenuePercentage.setText(it.profitSharingPercentage.toString())
            binding.etTotalInstallment.setText(it.totalInstallment.toString())
            checkTotalRevenueFromPercentage(it)
        }

        viewModel.akadImg.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Glide.with(parent)
                    .asBitmap()
                    .load(it)
                    .into(binding.ivAkadPhoto)
                binding.btnAkadPhoto.text = getString(R.string.ubah_foto)
            }
            binding.cvAkadPhoto.visible(it.isNotEmpty())
        }
        viewModel.penyerahanImg.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Glide.with(parent)
                    .asBitmap()
                    .load(it)
                    .into(binding.ivPenyerahanPhoto)
                binding.btnPenyerahanPhoto.text = getString(R.string.ubah_foto)
            }
            binding.cvPenyerahanPhoto.visible(it.isNotEmpty())
        }
    }

    override fun onSearchFinish(result: String) {
        if (type == SearchEnum.PERSON) {
            if (result.isNotEmpty()) {
                viewModel.selectedNasabah.value = result.toObj(UiModel.Search::class.java)
                binding.tilNasabah.error = null
                hideKeyboard()
            }
        }
    }

    private fun save() {
        hideKeyboard()

        var isError = false
        if (isPersonError()) isError = true
        if (isJenisUsahaError()) isError = true
        if (isJumlahSuntikanModalError()) isError = true
        if (isTotalRevenueError()) isError = true
        if (isTanggalAkadError()) isError = true
        if (isTanggalPembayaranError()) isError = true
        if (isJumlahAngsuranError()) isError = true
        if (isTanggalJatuhTempoError()) isError = true

        if (isError) return

        val profitSharing = viewModel.totalRevenuePercentage.value ?: 0.0
        val totalCapital = viewModel.totalCapitalInjection.value ?: 0.0
        val profit = viewModel.totalRevenue.value ?: 0.0
        val total = totalCapital + profit

        val transaction = Transaction(
            totalInstallment = viewModel.totalInstallment.valueOrDefault(),
            transactionModule = TransactionEnum.CAPITAL_INJECTION.name,
            personId = viewModel.selectedNasabah.value?.id!!,
            commitmentDate = viewModel.tglPembayaran.valueOrDefault().toDate("dd-MM-yyyy"),
            businessType = viewModel.businessType.valueOrDefault(),
            totalCapital = totalCapital,
            profitSharingPercentage = profitSharing,
            nilaiTransaksi = total,
            totalKeuntungan = profit,
            totalModal = totalCapital,
            penyerahanBarangImgPath = viewModel.penyerahanImg.valueOrDefault(),
            akadImgPath = viewModel.akadImg.valueOrDefault(),
            installmentPaymentDate = viewModel.tglAkad.valueOrDefault().toDate("dd-MM-yyyy"),
            paymentDueDate = viewModel.tglJatuhTempo.valueOrDefault().toDate("dd-MM-yyyy"))
        viewModel.save(transaction)
    }

    private fun isPersonError(): Boolean {
        return if (viewModel.selectedNasabah.value == null) {
            binding.tilNasabah.error = "Nasabah tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isJenisUsahaError(): Boolean {
        return if (viewModel.businessType.valueOrDefault().isEmpty()) {
            binding.tilBusinessType.error = "Jenis usaha tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isJumlahSuntikanModalError(): Boolean {
        var error = false

        if (viewModel.totalCapitalInjection.value == null) {
            binding.tilTotalCapitalInjection.error = "Jumlah suntikan modal tidak boleh kosong"
            error = true
        }
        if (viewModel.totalCapitalInjection.value ?: 0.0 <= 0.0) {
            binding.tilTotalCapitalInjection.error = "Jumlah suntikan modal harus lebih dari 0"
            error = true
        }
        return error
    }
    private fun isTotalRevenueError(): Boolean {
        var error = false

        if (viewModel.totalRevenue.value == null) {
            binding.tilRevenue.error = "Jumlah keuntungan tidak boleh kosong"
            error = true
        }
        if (viewModel.totalRevenue.value ?: 0.0 <= 0.0) {
            binding.tilRevenue.error = "Jumlah keuntungan harus lebih dari 0"
            error = true
        }
        return error
    }
    private fun isTanggalAkadError(): Boolean {
        return if (viewModel.tglAkad.valueOrDefault().isEmpty()) {
            binding.tilTglAkad.error = "Tanggal akad tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isTanggalPembayaranError(): Boolean {
        return if (viewModel.tglPembayaran.valueOrDefault().isEmpty()) {
            binding.tilTglPembayaran.error = "Tanggal pembayaran tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isJumlahAngsuranError(): Boolean {
        return if (viewModel.totalInstallment.value == null) {
            binding.tilTotalInstallment.error = "Jumlah angsuran tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isTanggalJatuhTempoError(): Boolean {
        return if (viewModel.tglJatuhTempo.valueOrDefault().isEmpty()) {
            binding.tilTglJatuhTempo.error = "Tanggal jatuh tempo tidak boleh kosong"
            true
        } else {
            false
        }
    }
    fun pickPictureIntent() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        parent.startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.INTENT_PICK_PHOTO)
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(parent.packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        parent,
                        "com.rijal.salesandcredit.provider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    parent.startActivityForResult(takePictureIntent, Constants.INTENT_TAKE_A_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = parent.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun onPictureGetResult(uri: Uri) {
        val imageFile = File(uri.path!!)
        if (!imageFile.exists()) {
            errorSnackbar("Gambar tidak ditemukan")
            return
        }
        if (selectedImg == "akad") {
            viewModel.akadImg.value = imageFile.absolutePath
        } else {
            viewModel.penyerahanImg.value = imageFile.absolutePath
        }
    }


    private fun showDialogPickImage(type: String) {
        selectedImg = type

        CFAlertDialog.Builder(parent)
            .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
            .setTitle("Unggah gambar")
            .setMessage("Silahkan pilih gambar yang akan di unggah")
            .addButton(
                "Dari Kamera",
                -1,
                -1,
                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, _ ->

                dialog.dismiss()

                if (parent.validateCameraPermission()) {
                    takePictureIntent()
                }
            }
            .addButton(
                "Dari Galeri",
                -1,
                -1,
                CFAlertDialog.CFAlertActionStyle.DEFAULT,
                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED) { dialog, _ ->

                dialog.dismiss()

                if (parent.validateReadStoragePermission()) {
                    pickPictureIntent()
                }
            }
            .create()
            .apply {
                this.show()
            }
    }
}