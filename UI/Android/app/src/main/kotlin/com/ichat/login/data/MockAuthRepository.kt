package com.ichat.login.data

import kotlinx.coroutines.delay

sealed class AuthResult {
    data object RequestSuccess : AuthResult()
    data class VerifySuccess(val token: String, val isNewUser: Boolean) : AuthResult()
    data object InvalidCode : AuthResult()
    data object NetworkError : AuthResult()
}

open class MockAuthRepository {

    open suspend fun requestCode(phone: String): AuthResult {
        delay(800)
        return AuthResult.RequestSuccess
    }

    open suspend fun verifyCode(phone: String, code: String): AuthResult {
        delay(600)
        return if (code == "123456") {
            AuthResult.VerifySuccess(token = "mock-token", isNewUser = false)
        } else {
            AuthResult.InvalidCode
        }
    }
}
