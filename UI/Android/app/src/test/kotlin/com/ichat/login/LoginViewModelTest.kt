package com.ichat.login

import com.ichat.login.data.AuthResult
import com.ichat.login.data.MockAuthRepository
import com.ichat.login.login.LoginPhase
import com.ichat.login.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp()  { Dispatchers.setMain(dispatcher) }
    @After  fun tearDown(){ Dispatchers.resetMain() }

    private fun stubRepo(
        request: suspend (String) -> AuthResult = { AuthResult.RequestSuccess },
        verify:  suspend (String, String) -> AuthResult =
            { _, c -> if (c == "123456") AuthResult.VerifySuccess("t", false) else AuthResult.InvalidCode },
    ) = object : MockAuthRepository() {
        override suspend fun requestCode(phone: String) = request(phone)
        override suspend fun verifyCode(phone: String, code: String) = verify(phone, code)
    }

    @Test fun phoneTooShort_buttonDisabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("1380013")
        assertEquals(false, vm.state.value.canRequestCode)
    }

    @Test fun phoneInvalidFormat_buttonDisabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("00000000000")
        assertEquals(false, vm.state.value.canRequestCode)
    }

    @Test fun phoneValid_buttonEnabled() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000")
        assertEquals(true, vm.state.value.canRequestCode)
    }

    @Test fun requestCode_movesToCodePhaseAndStartsCountdown() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000")
        vm.requestCode()
        runCurrent()
        assertEquals(LoginPhase.Code, vm.state.value.phase)
        assertEquals(60, vm.state.value.countdown)
    }

    @Test fun countdown_decrementsEverySecond() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode()
        runCurrent()
        assertEquals(60, vm.state.value.countdown)
        advanceTimeBy(3_000); runCurrent()
        assertEquals(57, vm.state.value.countdown)
    }

    @Test fun verifyCode_correct_emitsLoginSuccess() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); runCurrent()
        vm.onCodeChange("123456")
        runCurrent()
        assertTrue(vm.events.replayCache.contains(com.ichat.login.login.LoginEvent.LoginSuccess))
    }

    @Test fun verifyCode_wrong_setsErrorThenClearsAfterDelay() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); runCurrent()
        vm.onCodeChange("000000")
        runCurrent()
        assertEquals("验证码错误，请重新输入", vm.state.value.errorMessage)
        advanceTimeBy(1_200); runCurrent()
        assertEquals("",     vm.state.value.code)
        assertNull(vm.state.value.errorMessage)
    }

    @Test fun goBack_preservesPhoneAndCountdown() = runTest {
        val vm = LoginViewModel(stubRepo())
        vm.onPhoneChange("13800138000"); vm.requestCode(); runCurrent()
        advanceTimeBy(5_000); runCurrent()           // countdown should be ~55
        vm.goBack()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        assertEquals("13800138000", vm.state.value.phone)
        assertEquals(55, vm.state.value.countdown)
    }

    @Test fun requestCode_duringCountdown_skipsRefetchAndReentersCodeScreen() = runTest {
        var requestCount = 0
        val vm = LoginViewModel(stubRepo(request = {
            requestCount++
            AuthResult.RequestSuccess
        }))
        vm.onPhoneChange("13800138000"); vm.requestCode(); runCurrent()
        assertEquals(1, requestCount)
        advanceTimeBy(5_000); runCurrent()
        vm.goBack()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        vm.requestCode(); runCurrent()
        assertEquals(LoginPhase.Code, vm.state.value.phase)
        assertEquals(55, vm.state.value.countdown)
        assertEquals(1, requestCount)               // 没有再次发码
    }

    @Test fun networkError_onRequestCode_keepsPhonePhaseAndShowsError() = runTest {
        val vm = LoginViewModel(stubRepo(request = { AuthResult.NetworkError }))
        vm.onPhoneChange("13800138000")
        vm.requestCode(); runCurrent()
        assertEquals(LoginPhase.Phone, vm.state.value.phase)
        assertEquals("网络异常，请检查后重试", vm.state.value.errorMessage)
    }

    private fun runCurrent() = dispatcher.scheduler.runCurrent()
}
