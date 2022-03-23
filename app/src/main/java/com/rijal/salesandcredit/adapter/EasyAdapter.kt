package com.rijal.salesandcredit.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.rijal.salesandcredit.BR

abstract class EasyAdapter(
    @field:LayoutRes private var layoutId: Int,
    private val listData: List<*>? = null
): RecyclerView.Adapter<EasyViewHolder>() {

    init {
        this.setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EasyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return EasyViewHolder(view)
    }

    override fun onBindViewHolder(holder: EasyViewHolder, position: Int) {
        listData?.apply {
            holder.onBind(BR.model, listData[position])
        }
    }

    override fun getItemCount() = listData?.size ?: 0

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position
}