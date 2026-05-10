package com.ichat.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.ichat.login.data.AuthResult
import com.ichat.login.data.AuthService
import com.ichat.login.login.LoginPhase
import com.ichat.login.login.LoginUiState
import com.ichat.login.login.LoginViewModel
import com.ichat.login.login.PhoneScreen
import com.ichat.login.theme.IChatTheme
import org.junit.Rule
import org.junit.Test

class PhoneScreenTest {
    @get:Rule val rule = createComposeRule()

    private fun stubAuth() = object : AuthService {
        override suspend fun requestCode(phone: String) = AuthResult.RequestSuccess
        override suspend fun verifyCode(phone: String, code: String) = AuthResult.InvalidCode
    }

    private fun makeVm(state: LoginUiState): LoginViewModel {
        val vm = LoginViewModel(stubAuth())
        vm.setStateForTest(state)
        return vm
    }

    @Test fun renders_logo_title_subtitle_and_caption() {
        rule.setContent { IChatTheme { PhoneScreen(makeVm(LoginUiState())) } }
        rule.onNodeWithText("欢迎使用 iChat").assertIsDisplayed()
        rule.onNodeWithText("输入手机号，开启陪伴").assertIsDisplayed()
        rule.onNodeWithText("未注册手机号将自动创建账号").assertIsDisplayed()
    }

    @Test fun button_disabled_when_phone_empty() {
        rule.setContent { IChatTheme { PhoneScreen(makeVm(LoginUiState(phone = ""))) } }
        rule.onNodeWithTag("get-code-btn").assertIsNotEnabled()
    }

    @Test fun button_disabled_when_phone_invalid_format() {
        rule.setContent { IChatTheme { PhoneScreen(makeVm(LoginUiState(phone = "00000000000"))) } }
        rule.onNodeWithTag("get-code-btn").assertIsNotEnabled()
    }

    @Test fun button_enabled_when_phone_valid() {
        rule.setContent { IChatTheme { PhoneScreen(makeVm(LoginUiState(phone = "13800138000"))) } }
        rule.onNodeWithTag("get-code-btn").assertIsEnabled()
    }

    @Test fun button_disabled_and_loading_label_when_submitting() {
        rule.setContent {
            IChatTheme {
                PhoneScreen(makeVm(LoginUiState(phone = "13800138000", isSubmitting = true)))
            }
        }
        rule.onNodeWithText("发送中…").assertIsDisplayed()
        rule.onNodeWithTag("get-code-btn").assertIsNotEnabled()
    }

    @Test fun shows_error_message_when_set() {
        rule.setContent {
            IChatTheme {
                PhoneScreen(makeVm(LoginUiState(phone = "13800138000", errorMessage = "网络异常，请检查后重试")))
            }
        }
        rule.onNodeWithText("网络异常，请检查后重试").assertIsDisplayed()
    }
}
