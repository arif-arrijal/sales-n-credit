package com.rijal.salesandcredit.module.item

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.databinding.FragmentAddItemBinding
import com.rijal.salesandcredit.db.entity.Item
import com.rijal.salesandcredit.helpers.cleanCurrency
import com.rijal.salesandcredit.helpers.informationDialog
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.helpers.watch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.DecimalFormat

class AddItemFragment : Fragment() {
    lateinit var parent: DetailActivity
    private lateinit var binding: FragmentAddItemBinding
    private val viewModel: ItemViewModel by viewModel()
    private val args: AddItemFragmentArgs by navArgs()
    val numberFormat = DecimalFormat("Rp #,###")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddItemBinding.inflate(inflater, container, false)
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
            binding.etSeries.isEnabled = false
            binding.tvAdd.text = "Ubah"
            viewModel.findById()
        }
    }

    private fun initListener() {
        binding.cvAdd.setOnClickListener { insertData() }

        binding.etName.watch(
            onChanged = {
                binding.tilName.error = null
            },
            onAfterChanged = {
                isNameError()
            }
        )

        binding.etUom.watch(
            onChanged = {
                binding.tilUom.error = null
            },
            onAfterChanged = {
                isUomError()
            }
        )

        binding.etPurchasePrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etPurchasePrice.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.purchasePrice.value = parsedData

                binding.etPurchasePrice.setText(formatted)
                binding.etPurchasePrice.setSelection(formatted.length)
                binding.etPurchasePrice.addTextChangedListener(this)

                if (!isPurchasePriceError()) {
                    checkRevenue()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilPurchasePrice.error = null
            }
        })

        binding.etSellingPrice.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.etSellingPrice.removeTextChangedListener(this)

                var cleanString: String = s.toString().cleanCurrency()
                if (cleanString.isEmpty()) {
                    cleanString = "0"
                }

                val parsedData = cleanString.toDouble()
                val formatted = numberFormat.format(parsedData)

                viewModel.sellingPrice.value = parsedData

                binding.etSellingPrice.setText(formatted)
                binding.etSellingPrice.setSelection(formatted.length)
                binding.etSellingPrice.addTextChangedListener(this)

                if (!isSellingPriceError()) {
                    checkRevenue()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Timber.i("before text changed")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.tilSellingPrice.error = null
            }
        })
    }

    private fun checkRevenue() {
        val purchasePrice = viewModel.purchasePrice.value ?: 0.0
        val sellingPrice = viewModel.sellingPrice.value ?: 0.0
        var revenue = 0.0
        var revenuePercentage = 0.0

        if (purchasePrice > 0.0 && sellingPrice > 0.0) {
            revenue = sellingPrice - purchasePrice
            revenuePercentage = (revenue / purchasePrice) * 100
        }

        viewModel.revenue.value = revenue
        viewModel.revenuePercentage.value = revenuePercentage


        val formattedRevenue = numberFormat.format(revenue)
        binding.etRevenue.setText(formattedRevenue)
        binding.etRevenuePercentage.setText(revenuePercentage.toString())
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
        viewModel.updateState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    if (this) parent.informationDialog("Data berhasil dirubah.") {
                        parent.onBackPressed()
                    }
                }
            }
        }
        viewModel.findOneState.observe(viewLifecycleOwner) {
            it.read {
                it.data?.apply {
                    binding.etPurchasePrice.setText(numberFormat.format(this.purchasePrice))
                    binding.etSellingPrice.setText(numberFormat.format(this.sellingPrice))
                }
            }
        }
    }

    private fun insertData() {
        var isError = false
        if (isNameError()) isError = true
        if (isPurchasePriceError()) isError = true
        if (isSellingPriceError()) isError = true

        if (isError) return
        val data = Item(
            name = viewModel.name.valueOrDefault(),
            series = viewModel.series.valueOrDefault(),
            purchasePrice = viewModel.purchasePrice.value ?: 0.0,
            sellingPrice = viewModel.sellingPrice.value ?: 0.0,
            uom = viewModel.uom.valueOrDefault())

        if (viewModel.id > 0) {
            data.itemId = viewModel.id
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


    private fun isUomError(): Boolean {
        return if (viewModel.uom.value.isNullOrEmpty()) {
            binding.tilUom.error = "Satuan barang tidak boleh kosong"
            true
        } else {
            false
        }
    }

    private fun isPurchasePriceError(): Boolean {
        val purchasePrice = viewModel.purchasePrice.value
        if (purchasePrice == null) {
            binding.tilPurchasePrice.error = "Harga beli tidak boleh kosong."
            return true
        }
        if (purchasePrice.isNaN()) {
            binding.tilPurchasePrice.error = "Harga beli tidak sesuai format."
            return true
        }
        return false
    }
    private fun isSellingPriceError(): Boolean {
        val purchasePrice = viewModel.sellingPrice.value
        if (purchasePrice == null) {
            binding.tilSellingPrice.error = "Harga jual tidak boleh kosong."
            return true
        }
        if (purchasePrice.isNaN()) {
            binding.tilSellingPrice.error = "Harga jual tidak sesuai format."
            return true
        }
        return false
    }

}