package com.ichat.login.data

import kotlinx.coroutines.delay

sealed class AuthResult {
    data object RequestSuccess : AuthResult()
    data class VerifySuccess(val token: String, val isNewUser: Boolean) : AuthResult()
    data object InvalidCode : AuthResult()
    data object NetworkError : AuthResult()
}

/**
 * Mock 实现：本地模拟延迟 + 固定通过码 "123456"。仅用于第一期 demo 与测试。
 */
class MockAuthService : AuthService {
    override suspend fun requestCode(phone: String): AuthResult {
        delay(800)
        return AuthResult.RequestSuccess
    }

    override suspend fun verifyCode(phone: String, code: String): AuthResult {
        delay(600)
        return if (code == "123456") AuthResult.VerifySuccess(token = "mock-token", isNewUser = false)
        else AuthResult.InvalidCode
    }
}
