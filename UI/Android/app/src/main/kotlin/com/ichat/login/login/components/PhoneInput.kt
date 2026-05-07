package com.ichat.login.login.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ichat.login.data.PhoneUtils
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BodyMd
import com.ichat.login.theme.BorderInput
import com.ichat.login.theme.InputShape
import com.ichat.login.theme.TextSecondary

@Composable
fun PhoneInput(
    rawDigits: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val display = PhoneUtils.format(rawDigits)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(BgSurface, InputShape)
            .border(1.dp, BorderInput, InputShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("+86", style = BodyMd.copy(color = TextSecondary))
        Text("|", style = BodyMd.copy(color = BorderInput))
        BasicTextField(
            value = display,
            onValueChange = { onValueChange(PhoneUtils.digitsOnly(it).take(11)) },
            singleLine = true,
            textStyle = BodyMd,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            decorationBox = { inner ->
                if (display.isEmpty()) Text("请输入手机号", style = BodyMd.copy(color = TextSecondary))
                inner()
            },
            modifier = modifier.fillMaxWidth(),
        )
    }
}
