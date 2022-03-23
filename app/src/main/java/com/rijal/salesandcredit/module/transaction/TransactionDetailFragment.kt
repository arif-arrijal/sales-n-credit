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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentTransactionDetailBinding
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.db.entity.Transaction
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.model.UiModel
import com.rijal.salesandcredit.module.search.SearchFragment
import com.rijal.salesandcredit.module.search.SearchInterface
import me.shaohui.advancedluban.Luban
import me.shaohui.advancedluban.OnCompressListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionDetailFragment : Fragment(), SearchInterface {
    private lateinit var parent: DetailActivity
    lateinit var binding: FragmentTransactionDetailBinding
    private lateinit var datePickerDialog: DatePickerDialog
    private var calendar = Calendar.getInstance()
    private val viewModel: TransactionViewModel by viewModel()
    private var type: SearchEnum? = null
    private lateinit var goodsAdapter: EasyAdapter
    private val args: TransactionDetailFragmentArgs by navArgs()
    lateinit var currentPhotoPath: String
    var selectedImg = ""
    val numberFormat = DecimalFormat("Rp #,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTransactionDetailBinding.inflate(inflater, container, false)
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



    private fun calculatePricing() {
        var totalPurchasePrice = 0.0
        var totalSellingPrice = 0.0
        var totalRevenue = 0.0
        var totalRevenuePercentage = 0.0
        var totalKeuntunganPerAngsuran = 0.0
        var persentasePerAngsuran = 0.0

        viewModel.goodsList.valueOrDefault().forEach { item ->
            totalPurchasePrice += (item.purchasePrice * item.qty)
            totalSellingPrice += (item.sellingPrice * item.qty)
        }

        totalRevenue = totalSellingPrice - totalPurchasePrice
        totalRevenuePercentage = (totalRevenue / totalPurchasePrice) * 100

        viewModel.totalPurchasePrice.value = totalPurchasePrice
        viewModel.totalSellingPrice.value = totalSellingPrice
        viewModel.totalRevenue.value = totalRevenue
        viewModel.totalRevenuePercentage.value = totalRevenuePercentage

        binding.etPurchasePrice.setText(numberFormat.format(totalPurchasePrice))
        binding.etSellingPrice.setText(numberFormat.format(totalSellingPrice))
        binding.etRevenue.setText(numberFormat.format(totalRevenue))
        binding.etRevenuePercentage.setText(totalRevenuePercentage.toString())

        if (viewModel.totalInstallment.valueOrDefault() != 0) {
            persentasePerAngsuran = totalRevenuePercentage / viewModel.totalInstallment.valueOrDefault()
            totalKeuntunganPerAngsuran = totalRevenue / viewModel.totalInstallment.valueOrDefault()
        }
        binding.etPersentasePerAngsuran.setText(persentasePerAngsuran.toString())
        binding.etUntungPerAngsuran.setText(numberFormat.format(totalKeuntunganPerAngsuran))
    }

    private fun initView() {
        goodsAdapter = object: EasyAdapter(R.layout.item_item_list_with_qty, viewModel.goodsList.valueOrDefault()){
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)
                val selectedData = viewModel.goodsList.valueOrDefault()[position]

                val tvTotal = holder.itemView.findViewById<TextView>(R.id.tv_total)
                tvTotal.text = getTotalPrice(selectedData)

                holder.itemView.findViewById<Button>(R.id.btn_remove)?.apply {
                    if (args.id > 0) {
                        this.visible(false)
                    }
                    this.setOnClickListener { removeGoods(position) }
                }
                holder.itemView.findViewById<EditText>(R.id.et_qty).apply {
                    this.setText(selectedData.qty.toString())
                    this.watch(
                        onChanged = {
                            val value = it.toIntOrNull()
                            if (value != null) {
                                selectedData.qty = it.toInt()
                                tvTotal.text = getTotalPrice(selectedData)
                            } else {
                                tvTotal.text = 0.0.toRupiah()
                            }

                            calculatePricing()
                        }
                    )
                }
            }
        }
        binding.rvGoods.adapter = goodsAdapter

        if (viewModel.id > 0) {
            binding.btnAddGoods.visible(false)
            binding.cvAdd.visible(false)
            binding.btnAkadPhoto.visible(false)
            binding.btnPenyerahanPhoto.visible(false)
            viewModel.findOne()
        }
    }

    private fun removeGoods(index: Int) {
        viewModel.goodsList.valueOrDefault().removeAt(index)
        viewModel.goodsList.notifyObserver()
        calculatePricing()
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
        binding.btnAddGoods.setOnClickListener {
            type = SearchEnum.ITEM
            SearchFragment.newInstance(SearchEnum.ITEM, this).apply {
                this.dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                this.dialog?.window?.setLayout(-1, -1)
                show(parent.supportFragmentManager, "")
            }
        }
        binding.tvAdd.setOnClickListener {
            save()
        }

        binding.etTotalInstallment.watch(
            onChanged = {
                binding.tilTotalInstallment.error = null

                if (it.toIntOrNull() != null) {
                    viewModel.totalInstallment.value = it.toInt()
                }
                calculatePricing()
            },
            onAfterChanged = {
                isJumlahAngsuranError()
            }
        )
    }

    private fun getTotalPrice(selectedData: Item): String {
        return (selectedData.qty * selectedData.sellingPrice).toRupiah()
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
        viewModel.goodsList.observe(viewLifecycleOwner) {
            goodsAdapter.notifyDataSetChanged()
            calculatePricing()
        }

        viewModel.insertState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil ditambahkan.") {
                        parent.onBackPressed()
                    }
                }
            }
        }
        viewModel.selectedTransaction.observe(viewLifecycleOwner) {
            binding.etTotalInstallment.setText(it.totalInstallment.toString())
        }
        viewModel.akadImg.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Glide.with(parent)
                    .asBitmap()
                    .load(it)
                    .into(binding.ivAkadPhoto)
                binding.btnAkadPhoto.text = "Ubah Foto"
            }
            binding.cvAkadPhoto.visible(it.isNotEmpty())
        }
        viewModel.penyerahanImg.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Glide.with(parent)
                    .asBitmap()
                    .load(it)
                    .into(binding.ivPenyerahanPhoto)
                binding.btnPenyerahanPhoto.text = "Ubah Foto"
            }
            binding.cvPenyerahanPhoto.visible(it.isNotEmpty())
        }
    }

    override fun onSearchFinish(result: String) {
        if (type == SearchEnum.PERSON) {
            if (result.isNotEmpty()) {
                viewModel.selectedNasabah.value = result.toObj(UiModel.Search::class.java)
                binding.tilNasabah.error = null
            }
        } else if (type == SearchEnum.ITEM) {
            if (result.isNotEmpty()) {
                val data = result.toObj(Item::class.java)
                data.qty = 1
                if (viewModel.goodsList.valueOrDefault().firstOrNull { goods -> goods.itemId == data.itemId } == null) {
                    viewModel.goodsList.valueOrDefault().add(data)
                    viewModel.goodsList.notifyObserver()
                    calculatePricing()
                } else {
                    errorSnackbar("Barang ini sudah ada.")
                }
            }
        }
    }

    private fun save() {
        hideKeyboard()

        var isError = false
        if (isPersonError()) isError = true
        if (isItemError()) isError = true
        if (isTanggalAkadError()) isError = true
        if (isTanggalPembayaranError()) isError = true
        if (isJumlahAngsuranError()) isError = true
        if (isTanggalJatuhTempoError()) isError = true
        if (isError) return

        val profitSharing = viewModel.totalRevenuePercentage.value ?: 0.0
        val totalModal = viewModel.totalPurchasePrice.value ?: 0.0
        val totalKeuntungan = viewModel.totalRevenue.value ?: 0.0
        val nilaiTransaksi = viewModel.totalSellingPrice.value ?: 0.0

        val transaction = Transaction(
            nilaiTransaksi = nilaiTransaksi,
            totalKeuntungan = totalKeuntungan,
            totalModal = totalModal,
            totalInstallment = viewModel.totalInstallment.valueOrDefault(),
            transactionModule = TransactionEnum.SALES.name,
            personId = viewModel.selectedNasabah.value?.id!!,
            profitSharingPercentage = profitSharing,
            penyerahanBarangImgPath = viewModel.penyerahanImg.valueOrDefault(),
            akadImgPath = viewModel.akadImg.valueOrDefault(),
            commitmentDate = viewModel.tglPembayaran.valueOrDefault().toDate("dd-MM-yyyy"),
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
    private fun isItemError(): Boolean {
        var error = false
        if (viewModel.goodsList.valueOrDefault().isEmpty()) {
            errorSnackbar("Barang tidak boleh kosong")
            error = true
        }
        if (viewModel.goodsList.valueOrDefault().firstOrNull { it.qty == 0 } != null) {
            errorSnackbar("Jumlah barang minimal 1")
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