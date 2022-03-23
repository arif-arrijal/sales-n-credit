package com.rijal.salesandcredit.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rijal.salesandcredit.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun goToDetailActivity() {
        startActivity(Intent(this, DetailActivity::class.java))
        finishAffinity()
    }
}