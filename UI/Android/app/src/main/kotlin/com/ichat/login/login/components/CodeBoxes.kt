package com.ichat.login.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BorderInput
import com.ichat.login.theme.BorderInputFocus
import com.ichat.login.theme.CodeBox
import com.ichat.login.theme.CodeBoxShape
import com.ichat.login.theme.StateError

@Composable
fun CodeBoxes(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    Box(modifier = modifier.fillMaxWidth().height(48.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(6) { i ->
                val ch = value.getOrNull(i)?.toString().orEmpty()
                val borderColor = when {
                    isError              -> StateError
                    i == value.length    -> BorderInputFocus
                    else                 -> BorderInput
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BgSurface, CodeBoxShape)
                        .border(1.dp, borderColor, CodeBoxShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(ch, style = CodeBox)
                }
            }
        }
        BasicTextField(
            value = value,
            onValueChange = { onValueChange(it.filter { c -> c.isDigit() }.take(6)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(0f)
                .testTag("code-input")
                .focusRequester(focus)
                .focusable(),
        )
    }
}
