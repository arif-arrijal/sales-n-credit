package com.rijal.salesandcredit.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.module.person.AddPersonFragment
import com.rijal.salesandcredit.module.transaction.CapitalInjectionDetailFragment
import com.rijal.salesandcredit.module.transaction.TransactionDetailFragment
import timber.log.Timber

class DetailActivity : AppCompatActivity() {
    var openedFragment = ""
    lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        initView()
        initDialog()

        intent.getStringExtra(IntentExtra.ROUTE)?.let {
            openedFragment = it
        }
    }

    fun setToolbarTitle(title: String) {
        findViewById<TextView>(R.id.tv_title).text = title
    }

    private fun initView() {
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<ImageButton>(R.id.ib_back).setOnClickListener { onBackPressed() }
    }

    fun logout() {
        removePrefData(SharedPref.IS_LOGIN)
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment)
        if (navController.previousBackStackEntry == null) {
            finish()
        }
        navController.popBackStack()
    }

    fun validateCameraPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 23) return true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), Constants.REQ_CAMERA)
            return false
        }
        return true
    }

    fun validateReadStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.REQ_READ_EXTERNAL_STORAGE)
            return false
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQ_CAMERA -> {
                when (PackageManager.PERMISSION_GRANTED) {
                    grantResults[0] -> {
                        getCurrentFragment(R.id.nav_host_fragment)?.let { currentFragment ->
                            when (currentFragment) {
                                is AddPersonFragment -> {
                                    currentFragment.takePictureIntent()
                                }
                                is TransactionDetailFragment -> {
                                    currentFragment.takePictureIntent()
                                }
                                is CapitalInjectionDetailFragment -> {
                                    currentFragment.takePictureIntent()
                                }
                            }
                        } ?: kotlin.run {
                            Timber.i("req camera, unknown fragment")
                        }
                    }
                    else -> snackbar("Silahkan ijinkan aplikasi untuk mengakses kamera untuk menggunakan fitur ini.", InfoEnum.DANGER, Snackbar.LENGTH_LONG)
                }
            }
            Constants.REQ_READ_EXTERNAL_STORAGE -> {
                when (PackageManager.PERMISSION_GRANTED) {
                    grantResults[0] -> {
                        getCurrentFragment(R.id.nav_host_fragment)?.let { currentFragment ->
                            if (currentFragment is AddPersonFragment) {
                                currentFragment.pickPictureIntent()
                            }
                            if (currentFragment is TransactionDetailFragment) {
                                currentFragment.pickPictureIntent()
                            }
                            if (currentFragment is CapitalInjectionDetailFragment) {
                                currentFragment.pickPictureIntent()
                            }
                        } ?: kotlin.run {
                            Timber.i("req storage, unknown fragment")
                        }
                    }
                    else -> snackbar("Silahkan ijinkan aplikasi untuk mengakses storage untuk menggunakan fitur ini.", InfoEnum.DANGER)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.INTENT_TAKE_A_PHOTO) {
                getCurrentFragment(R.id.nav_host_fragment)?.let { currentFragment ->
                    when (currentFragment) {
                        is AddPersonFragment -> {
                            currentFragment.onPictureGetResult(Uri.parse(currentFragment.currentPhotoPath))
                        }
                        is TransactionDetailFragment -> {
                            currentFragment.onPictureGetResult(Uri.parse(currentFragment.currentPhotoPath))
                        }
                        is CapitalInjectionDetailFragment -> {
                            currentFragment.onPictureGetResult(Uri.parse(currentFragment.currentPhotoPath))
                        }
                    }
                } ?: kotlin.run {
                    Timber.i("activity result camera, unknown fragment")
                }
            } else if (requestCode == Constants.INTENT_PICK_PHOTO) {
                getCurrentFragment(R.id.nav_host_fragment)?.let { currentFragment ->
                    if (currentFragment is AddPersonFragment) {
                        currentFragment.onPictureGetResult(Uri.parse(data.getFilePath(this)))
                    }
                    if (currentFragment is TransactionDetailFragment) {
                        currentFragment.onPictureGetResult(Uri.parse(data.getFilePath(this)))
                    }
                    if (currentFragment is CapitalInjectionDetailFragment) {
                        currentFragment.onPictureGetResult(Uri.parse(data.getFilePath(this)))
                    }
                } ?: kotlin.run {
                    Timber.i("activity result camera, unknown fragment")
                }
            }
        }

    }

    private fun initDialog() {
        val inflater = layoutInflater
        val loadingView = inflater.inflate(R.layout.dialog_loading, null)

        loadingDialog = AlertDialog.Builder(this)
            .setCancelable(false)
            .setView(loadingView)
            .create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
    }
}