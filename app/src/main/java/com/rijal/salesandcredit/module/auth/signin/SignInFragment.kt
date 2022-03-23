package com.rijal.salesandcredit.module.auth.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rijal.salesandcredit.activity.MainActivity
import com.rijal.salesandcredit.databinding.FragmentSignInBinding
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.module.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInFragment : Fragment() {
    lateinit var parent: MainActivity
    lateinit var binding: FragmentSignInBinding
    private val viewModel: AuthViewModel by viewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        parent = activity as MainActivity
        initListener()
        observe()
    }

    private fun observe() {

    }

    private fun initListener() {
        hideKeyboard()

        binding.cvLogin.setOnClickListener {
            hideKeyboard()
            var error = false
            if (isUsernameError()) error = true
            if (isPasswordError()) error = true
            if (error) return@setOnClickListener

            viewModel.findUser().observe(viewLifecycleOwner) {
                it?.apply {
                    parent.putPrefData(SharedPref.IS_LOGIN, true)
                    parent.goToDetailActivity()
                } ?: kotlin.run {
                    errorSnackbar("Username atau password salah.")
                }
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(SignInFragmentDirections.actionSignInFragmentToSignUpFragment())
        }

        binding.etUsername.watch(
            onChanged = {
                binding.tilUsername.error = null
            },
            onAfterChanged = {
                isUsernameError()
            }
        )
        binding.etPassword.watch(
            onChanged = {
                binding.tilPassword.error = null
            },
            onAfterChanged = {
                isPasswordError()
            }
        )
    }

    private fun isUsernameError(): Boolean {
        return if (viewModel.username.value.isNullOrEmpty()) {
            binding.tilUsername.error = "Username tidak boleh kosong"
            true
        } else {
            false
        }
    }

    private fun isPasswordError(): Boolean {
        return when {
            viewModel.password.value.isNullOrEmpty() -> {
                binding.tilPassword.error = "Kata sandi tidak boleh kosong"
                true
            }
            else -> {
                false
            }
        }
    }
}