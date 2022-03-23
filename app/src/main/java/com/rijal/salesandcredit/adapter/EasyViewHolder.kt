package com.rijal.salesandcredit.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

class EasyViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val binding: ViewDataBinding? = androidx.databinding.DataBindingUtil.bind(view)

    fun onBind(variable: Int?, data: Any?){
        variable?.takeIf { data != null }?.let {
            binding?.setVariable(it, data)
            binding?.executePendingBindings()
        }
    }
}