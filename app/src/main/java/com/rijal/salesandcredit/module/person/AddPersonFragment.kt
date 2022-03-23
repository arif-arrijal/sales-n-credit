package com.rijal.salesandcredit.module.person

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.crowdfire.cfalertdialog.CFAlertDialog
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentAddPersonBinding
import com.rijal.salesandcredit.db.entity.Person
import com.rijal.salesandcredit.helpers.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddPersonFragment : Fragment() {
    lateinit var parent: DetailActivity
    private lateinit var binding: FragmentAddPersonBinding
    private val viewModel: PersonViewModel by viewModel()
    private val args: AddPersonFragmentArgs by navArgs()
    lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPersonBinding.inflate(inflater, container, false)
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
            binding.etName.isEnabled = false
            binding.etIdCard.isEnabled = false
            binding.btnTakePhoto.visible(false)
            binding.tvAdd.text = "Ubah"
            viewModel.findById()
        }
    }

    private fun initListener() {
        binding.cvAdd.setOnClickListener { insertOrUpdateData() }
        binding.btnTakePhoto.setOnClickListener {
            showDialogPickImage()
        }
        binding.etName.watch(
            onChanged = {
                binding.tilName.error = null
            },
            onAfterChanged = {
                isNameError()
            }
        )

        binding.etAddress.watch(
            onChanged = {
                binding.tilAddress.error = null
            },
            onAfterChanged = {
                isAddressError()
            }
        )

        binding.etIdCard.watch(
            onChanged = {
                binding.tilIdCardNo.error = null
            },
            onAfterChanged = {
                isIdCardNoError()
            }
        )

        binding.etPhone.watch(
            onChanged = {
                binding.tilPhone.error = null
            },
            onAfterChanged = {
                isPhoneError()
            }
        )
    }

    private fun observe() {
        viewModel.loadingState.observe(viewLifecycleOwner) {
            binding.cvAdd.isEnabled = !it
        }
        viewModel.insertState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil ditambahkan.") {
                        if (args.id == -1) {
                            parent.finish()
                        } else {
                            parent.onBackPressed()
                        }
                    }
                }
            }
        }
        viewModel.updateState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil dirubah.") {
                        parent.onBackPressed()
                    }
                }
            }
        }
        viewModel.imgPath.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Glide.with(parent)
                    .asBitmap()
                    .load(it)
                    .into(binding.ivIdCardPhoto)
                binding.btnTakePhoto.text = "Ubah Foto"
            }
            binding.cvIdCardPhoto.visible(it.isNotEmpty())
        }
    }

    private fun insertOrUpdateData() {
        var isError = false
        if (isNameError()) isError = true
        if (isAddressError()) isError = true
        if (isIdCardNoError()) isError = true
        if (isPhoneError()) isError = true

        if (isError) return

        val data = Person(
            name = viewModel.name.valueOrDefault(),
            address = viewModel.address.valueOrDefault(),
            idCardNo = viewModel.idCardNo.valueOrDefault(),
            idCardImgPath = viewModel.imgPath.valueOrDefault(),
            phone = viewModel.phone.valueOrDefault())

        if (viewModel.id > 0) {
            data.personId = viewModel.id
            viewModel.update(data)
        } else {
            viewModel.insert(data)
        }
    }

    private fun isNameError(): Boolean {
        return if (viewModel.name.value.isNullOrEmpty()) {
            binding.tilName.error = "Nama tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isAddressError(): Boolean {
        return if (viewModel.address.value.isNullOrEmpty()) {
            binding.tilAddress.error = "Alamat tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isIdCardNoError(): Boolean {
        return if (viewModel.idCardNo.value.isNullOrEmpty()) {
            binding.tilIdCardNo.error = "Nomor identitas tidak boleh kosong"
            true
        } else {
            false
        }
    }
    private fun isPhoneError(): Boolean {
        return if (viewModel.phone.value.isNullOrEmpty()) {
            binding.tilPhone.error = "Nomor telepon tidak boleh kosong"
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

        viewModel.imgPath.value = imageFile.absolutePath
    }

    private fun showDialogPickImage() {
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