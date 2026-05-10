package com.ichat.login

import com.ichat.login.data.AuthResult
import com.ichat.login.data.MockAuthService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockAuthServiceTest {

    @Test fun requestCode_returnsSuccess() = runTest {
        val repo = MockAuthService()
        assertEquals(AuthResult.RequestSuccess, repo.requestCode("13800138000"))
    }

    @Test fun verifyCode_correctCode_returnsSuccess() = runTest {
        val repo = MockAuthService()
        val result = repo.verifyCode("13800138000", "123456")
        assertTrue(result is AuthResult.VerifySuccess)
        result as AuthResult.VerifySuccess
        assertEquals("mock-token", result.token)
        assertEquals(false, result.isNewUser)
    }

    @Test fun verifyCode_wrongCode_returnsInvalidCode() = runTest {
        val repo = MockAuthService()
        val result = repo.verifyCode("13800138000", "000000")
        assertEquals(AuthResult.InvalidCode, result)
    }
}
