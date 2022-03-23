package com.rijal.salesandcredit.module.auth.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rijal.salesandcredit.activity.MainActivity
import com.rijal.salesandcredit.databinding.FragmentSignUpBinding
import com.rijal.salesandcredit.db.entity.User
import com.rijal.salesandcredit.helpers.*
import com.rijal.salesandcredit.module.auth.AuthViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : Fragment() {
    lateinit var parent: MainActivity
    lateinit var binding: FragmentSignUpBinding
    private val viewModel: AuthViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
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

    private fun initListener() {
        binding.cvRegister.setOnClickListener {
            hideKeyboard()
            var error = false
            if (isUsernameError()) error = true
            if (isPasswordError()) error = true
            if (error) return@setOnClickListener

            viewModel.registerUser(
                User(
                    username = viewModel.username.valueOrDefault(),
                    password = viewModel.password.valueOrDefault())
            )
        }

        binding.tvSignin.setOnClickListener {
            findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment2())
        }
    }

    private fun observe() {
        viewModel.registerUserState.observe(viewLifecycleOwner) {
            if (!it.isConsumed) {
                it.isConsumed = true
                it.data?.apply {
                    parent.informationDialog("Anda berhasil membuat akun.") {
                        findNavController().navigate(SignUpFragmentDirections.actionSignUpFragmentToSignInFragment2())
                    }
                }
            }
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
            viewModel.password.valueOrDefault().length < 6 -> {
                binding.tilPassword.error = "Kata sandi minimal harus terdiri oleh 6 karakter"
                true
            }
            else -> {
                false
            }
        }
    }
}