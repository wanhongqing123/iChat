package com.ichat.login.login.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BrandPrimary
import com.ichat.login.theme.BrandPrimaryDisabled
import com.ichat.login.theme.ButtonShape

@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = ButtonShape,
        modifier = modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandPrimary,
            contentColor = BgSurface,
            disabledContainerColor = BrandPrimaryDisabled,
            disabledContentColor = BgSurface,
        ),
    ) {
        Text(text)
    }
}
