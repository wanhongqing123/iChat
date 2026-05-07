package com.ichat.login.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichat.login.login.components.PhoneInput
import com.ichat.login.login.components.PrimaryButton
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.BgSurface
import com.ichat.login.theme.BodySm
import com.ichat.login.theme.Caption
import com.ichat.login.theme.TitleLg

@Composable
fun PhoneScreen(vm: LoginViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(24.dp))
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center,
        ) { Text("💬") }

        Spacer(Modifier.height(20.dp))
        Text("欢迎使用 iChat", style = TitleLg)
        Spacer(Modifier.height(4.dp))
        Text("输入手机号，开启陪伴", style = BodySm, textAlign = TextAlign.Center)

        Spacer(Modifier.height(20.dp))
        PhoneInput(
            rawDigits = state.phone,
            onValueChange = vm::onPhoneChange,
            modifier = Modifier.testTag("phone-input"),
        )

        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = if (state.isSubmitting) "发送中…" else "获取验证码",
            enabled = state.canRequestCode,
            modifier = Modifier.testTag("get-code-btn"),
            onClick = vm::requestCode,
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage!!, style = Caption.copy(color = com.ichat.login.theme.StateError))
        }

        Spacer(Modifier.weight(1f))
        Text("未注册手机号将自动创建账号", style = Caption)
    }
}
