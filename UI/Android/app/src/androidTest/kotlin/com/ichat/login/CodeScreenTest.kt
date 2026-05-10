package com.ichat.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ichat.login.data.AuthResult
import com.ichat.login.data.AuthService
import com.ichat.login.login.CodeScreen
import com.ichat.login.login.LoginPhase
import com.ichat.login.login.LoginUiState
import com.ichat.login.login.LoginViewModel
import com.ichat.login.theme.IChatTheme
import org.junit.Rule
import org.junit.Test

class CodeScreenTest {
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

    private fun codeState(extra: LoginUiState.() -> LoginUiState = { this }): LoginUiState =
        LoginUiState(phase = LoginPhase.Code, phone = "13800138000", countdown = 60).extra()

    @Test fun renders_title_and_masked_subtitle() {
        rule.setContent { IChatTheme { CodeScreen(makeVm(codeState())) } }
        rule.onNodeWithText("输入验证码").assertIsDisplayed()
        rule.onNodeWithText("已发送至 +86 138****8000").assertIsDisplayed()
    }

    @Test fun shows_countdown_text_when_above_zero() {
        rule.setContent { IChatTheme { CodeScreen(makeVm(codeState { copy(countdown = 42) })) } }
        rule.onNodeWithText("42s 后重新发送").assertIsDisplayed()
    }

    @Test fun shows_resend_link_when_countdown_zero() {
        rule.setContent { IChatTheme { CodeScreen(makeVm(codeState { copy(countdown = 0) })) } }
        rule.onNodeWithText("重新发送").assertIsDisplayed()
    }

    @Test fun shows_error_message_when_set() {
        rule.setContent {
            IChatTheme {
                CodeScreen(makeVm(codeState { copy(errorMessage = "验证码错误，请重新输入", code = "000000") }))
            }
        }
        rule.onNodeWithText("验证码错误，请重新输入").assertIsDisplayed()
    }

    @Test fun renders_help_link() {
        rule.setContent { IChatTheme { CodeScreen(makeVm(codeState())) } }
        rule.onNodeWithText("收不到验证码？").assertIsDisplayed()
    }
}
