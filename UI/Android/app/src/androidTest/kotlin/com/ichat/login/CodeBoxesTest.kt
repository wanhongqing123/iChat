package com.ichat.login

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.ichat.login.login.components.CodeBoxes
import com.ichat.login.theme.IChatTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CodeBoxesTest {
    @get:Rule val rule = createComposeRule()

    @Test fun typing_appends_digits_and_calls_onChange() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextInput("8")
        assertEquals("8", state.value)
        rule.onNodeWithTag("code-input").performTextInput("3")
        assertEquals("83", state.value)
    }

    @Test fun pasting_six_digits_fills_all_boxes() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextReplacement("123456")
        assertEquals("123456", state.value)
    }

    @Test fun ignores_non_digit_input() {
        val state = mutableStateOf("")
        rule.setContent {
            IChatTheme {
                CodeBoxes(value = state.value, onValueChange = { state.value = it }, isError = false)
            }
        }
        rule.onNodeWithTag("code-input").performTextInput("a8b3")
        assertEquals("83", state.value)
    }
}
