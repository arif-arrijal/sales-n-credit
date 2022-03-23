package com.rijal.salesandcredit.module.person

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.DetailActivity
import com.rijal.salesandcredit.adapter.EasyAdapter
import com.rijal.salesandcredit.adapter.EasyViewHolder
import com.rijal.salesandcredit.databinding.FragmentPersonListBinding
import com.rijal.salesandcredit.helpers.snackbar
import com.rijal.salesandcredit.helpers.successSnackbar
import com.rijal.salesandcredit.helpers.valueOrDefault
import com.rijal.salesandcredit.helpers.visible
import org.koin.androidx.viewmodel.ext.android.viewModel
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class PersonListFragment : Fragment() {
    lateinit var parent: DetailActivity
    lateinit var binding: FragmentPersonListBinding
    private lateinit var adapter: EasyAdapter
    private val viewModel: PersonViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPersonListBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as DetailActivity
        parent.setToolbarTitle("Daftar Nasabah")

        initView()
        initListener()
        observe()
    }

    override fun onResume() {
        super.onResume()
        viewModel.findAll()
    }

    private fun initView() {
        adapter = object : EasyAdapter(R.layout.item_person_list, viewModel.dataList.valueOrDefault()) {
            override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
                super.onBindViewHolder(holder, position)

                val selectedData = viewModel.dataList.valueOrDefault()[position]
                val cvPhone = holder.itemView.findViewById(R.id.cv_phone) as CardView
                val cvDetail = holder.itemView.findViewById(R.id.cv_detail) as CardView

                cvPhone.setOnClickListener {
                    val phoneNo = selectedData.phone
                    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNo, null))
                    if (intent.resolveActivity(parent.packageManager) != null) {
                        parent.startActivity(Intent.createChooser(intent, ""))
                    }
                }

                cvDetail.setOnClickListener {
                    findNavController().navigate(PersonListFragmentDirections.actionPersonListFragmentToAddPersonFragment(selectedData.personId))
                }
            }
        }
        binding.rvMain.adapter = adapter
    }

    private fun initListener() {
        binding.fabAddNew.setOnClickListener {
            findNavController().navigate(PersonListFragmentDirections.actionPersonListFragmentToAddPersonFragment(0))
        }
    }

    private fun observe() {
        RxTextView.textChanges(binding.etSearch)
            .map(CharSequence::toString)
            .debounce(600, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.findAll()
            }

        viewModel.dataList.observe(viewLifecycleOwner) {
            binding.rvMain.visible(it.isNotEmpty())
            binding.tvNoData.visible(it.isEmpty())
            adapter.notifyDataSetChanged()
        }
    }
}