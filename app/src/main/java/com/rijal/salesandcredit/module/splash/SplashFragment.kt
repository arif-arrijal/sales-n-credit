package com.rijal.salesandcredit.module.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rijal.salesandcredit.R
import com.rijal.salesandcredit.activity.MainActivity
import com.rijal.salesandcredit.helpers.SharedPref
import com.rijal.salesandcredit.helpers.getPrefBoolean
import com.rijal.salesandcredit.module.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : Fragment() {
    lateinit var parent: MainActivity
    private val viewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as MainActivity


        checkUserData()
    }

    private fun checkUserData() {
        if (parent.getPrefBoolean(SharedPref.IS_LOGIN)) {
            parent.goToDetailActivity()
        } else {
            countUser()
        }
    }

    private fun countUser() {
        viewModel.countUser().observe(viewLifecycleOwner) {
            if (it > 0) {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignInFragment())
            } else {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToSignUpFragment())
            }
        }
    }
}