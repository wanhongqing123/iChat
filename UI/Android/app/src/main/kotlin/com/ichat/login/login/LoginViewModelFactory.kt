package com.ichat.login.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ichat.login.data.AuthService

class LoginViewModelFactory(private val auth: AuthService) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == LoginViewModel::class.java) {
            "Unsupported ViewModel: $modelClass"
        }
        return LoginViewModel(auth) as T
    }
}
