package com.rijal.salesandcredit.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.model.UiModel

class MenuAdapter(
    val context: Context,
    private val dashboardMenus: ArrayList<UiModel.DashboardMenu>): BaseAdapter() {

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            val layoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.item_menu, null)
        }

        view?.let {
            val data = dashboardMenus[position]
            val cvMain = it.findViewById(R.id.cv_main) as CardView
            val imgLogo = it.findViewById(R.id.iv_logo) as ImageView
            val tvName = it.findViewById(R.id.tv_name) as TextView

            cvMain.setOnClickListener { data.func() }
            data.imageId?.apply {
                Glide.with(context)
                    .asBitmap()
                    .load(data.imageId)
                    .into(imgLogo)
            }


            tvName.text = data.menuName
        }

        return view!!
    }

    override fun getItem(position: Int): Any = dashboardMenus[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = dashboardMenus.size

}