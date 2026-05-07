package com.ichat.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class LoginE2ETest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun happy_path_phone_to_code_to_home() {
        rule.onNodeWithTag("get-code-btn").assertIsNotEnabled()

        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").assertIsEnabled()

        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(timeoutMillis = 2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("已发送至 +86 138****8000").assertIsDisplayed()

        rule.onNodeWithTag("code-input").performTextInput("123456")
        rule.waitUntil(timeoutMillis = 2_000) {
            rule.onAllNodesWithTag("home-screen").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("登录成功 · iChat").assertIsDisplayed()
    }

    @Test fun back_from_code_keeps_phone_and_countdown() {
        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("back-btn").performClick()
        rule.onNodeWithTag("get-code-btn").assertIsEnabled()
    }

    @Test fun wrong_code_shows_error_then_clears() {
        rule.onNodeWithTag("phone-input").performTextInput("13800138000")
        rule.onNodeWithTag("get-code-btn").performClick()
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("输入验证码").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("code-input").performTextInput("000000")
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("验证码错误，请重新输入").fetchSemanticsNodes().isNotEmpty()
        }
        rule.waitUntil(2_000) {
            rule.onAllNodesWithText("验证码错误，请重新输入").fetchSemanticsNodes().isEmpty()
        }
    }
}
