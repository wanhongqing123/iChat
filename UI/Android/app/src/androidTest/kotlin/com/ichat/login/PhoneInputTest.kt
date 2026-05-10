package com.ichat.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.ichat.login.login.components.PhoneInput
import com.ichat.login.theme.IChatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PhoneInputTest {
    @get:Rule val rule = createComposeRule()

    @Test fun shows_plus86_prefix_and_placeholder_when_empty() {
        rule.setContent {
            IChatTheme {
                PhoneInput(rawDigits = "", onValueChange = {})
            }
        }
        rule.onNodeWithText("+86").assertExists()
        rule.onNodeWithText("请输入手机号").assertExists()
    }

    @Test fun typing_digits_emits_raw_digits_only() {
        val captured = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                PhoneInput(rawDigits = "", onValueChange = { captured.value = it })
            }
        }
        rule.onNodeWithText("请输入手机号").performTextInput("13800138000")
        assertEquals("13800138000", captured.value)
    }

    @Test fun typing_more_than_eleven_digits_truncates_at_eleven() {
        val captured = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                PhoneInput(rawDigits = "", onValueChange = { captured.value = it })
            }
        }
        rule.onNodeWithText("请输入手机号").performTextInput("138001380009999")
        assertEquals("13800138000", captured.value)
    }

    @Test fun ignores_non_digits() {
        val captured = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                PhoneInput(rawDigits = "", onValueChange = { captured.value = it })
            }
        }
        rule.onNodeWithText("请输入手机号").performTextInput("13a8b0")
        assertEquals("1380", captured.value)
    }
}
