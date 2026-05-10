package com.ichat.login.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ichat.login.data.AuthResult
import com.ichat.login.data.AuthService
import com.ichat.login.data.MockAuthService
import com.ichat.login.data.PhoneUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LoginPhase { Phone, Code }

data class LoginUiState(
    val phase: LoginPhase = LoginPhase.Phone,
    val phone: String = "",
    val code: String = "",
    val countdown: Int = 0,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val canRequestCode: Boolean get() = PhoneUtils.isValid(phone) && !isSubmitting
}

sealed class LoginEvent {
    data object LoginSuccess : LoginEvent()
}

class LoginViewModel(
    private val auth: AuthService = MockAuthService(),
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>(replay = 1)
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private var countdownJob: Job? = null
    private var errorClearJob: Job? = null

    fun onPhoneChange(raw: String) {
        _state.update { it.copy(phone = PhoneUtils.digitsOnly(raw).take(11)) }
    }

    fun onCodeChange(raw: String) {
        val digits = raw.filter { it.isDigit() }.take(6)
        _state.update { it.copy(code = digits, errorMessage = null) }
        if (digits.length == 6) verifyCode()
    }

    fun requestCode() {
        if (!PhoneUtils.isValid(_state.value.phone) || _state.value.isSubmitting) return
        // 倒计时未归零 → 不重新发码，直接切回 Code 屏（spec §6.1 #7）
        if (_state.value.countdown > 0) {
            _state.update { it.copy(phase = LoginPhase.Code, errorMessage = null) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (auth.requestCode(_state.value.phone)) {
                is AuthResult.RequestSuccess -> {
                    _state.update { it.copy(phase = LoginPhase.Code, code = "", isSubmitting = false, countdown = 60) }
                    startCountdown()
                }
                else -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = "网络异常，请检查后重试")
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_state.value.countdown > 0) {
                delay(1_000)
                _state.update { it.copy(countdown = (it.countdown - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun resendCode() {
        if (_state.value.countdown > 0) return
        viewModelScope.launch {
            when (auth.requestCode(_state.value.phone)) {
                is AuthResult.RequestSuccess -> { _state.update { it.copy(countdown = 60) }; startCountdown() }
                else -> _state.update { it.copy(errorMessage = "网络异常，请检查后重试") }
            }
        }
    }

    private fun verifyCode() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            val result = auth.verifyCode(_state.value.phone, _state.value.code)
            _state.update { it.copy(isSubmitting = false) }
            when (result) {
                is AuthResult.VerifySuccess -> _events.emit(LoginEvent.LoginSuccess)
                AuthResult.InvalidCode      -> showCodeError()
                AuthResult.NetworkError     -> _state.update { it.copy(errorMessage = "网络异常，请检查后重试") }
                else                        -> Unit
            }
        }
    }

    private fun showCodeError() {
        _state.update { it.copy(errorMessage = "验证码错误，请重新输入") }
        errorClearJob?.cancel()
        errorClearJob = viewModelScope.launch {
            delay(1_200)
            _state.update { it.copy(code = "", errorMessage = null) }
        }
    }

    @androidx.annotation.VisibleForTesting
    internal fun setStateForTest(state: LoginUiState) {
        _state.value = state
    }

    fun goBack() {
        _state.update { it.copy(phase = LoginPhase.Phone, errorMessage = null) }
    }
}
