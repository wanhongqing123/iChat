package com.ichat.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ichat.login.login.components.PrimaryButton
import com.ichat.login.theme.IChatTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PrimaryButtonTest {
    @get:Rule val rule = createComposeRule()

    @Test fun renders_text_and_handles_click_when_enabled() {
        val clicked = mutableStateOf(false)
        rule.setContent {
            IChatTheme {
                PrimaryButton(text = "获取验证码", enabled = true) { clicked.value = true }
            }
        }
        rule.onNodeWithText("获取验证码").assertHasClickAction().assertIsEnabled().performClick()
        assertTrue(clicked.value)
    }

    @Test fun shows_disabled_when_enabled_false() {
        rule.setContent {
            IChatTheme {
                PrimaryButton(text = "获取验证码", enabled = false) {}
            }
        }
        rule.onNodeWithText("获取验证码").assertIsNotEnabled()
    }
}
