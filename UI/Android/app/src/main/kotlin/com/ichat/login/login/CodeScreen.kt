package com.ichat.login.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ichat.login.data.PhoneUtils
import com.ichat.login.login.components.CodeBoxes
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.BodySm
import com.ichat.login.theme.BrandPrimary
import com.ichat.login.theme.Caption
import com.ichat.login.theme.StateError
import com.ichat.login.theme.TitleLg

@Composable
fun CodeScreen(vm: LoginViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                "‹",
                style = TitleLg,
                modifier = Modifier
                    .size(44.dp)
                    .clickable { vm.goBack() }
                    .testTag("back-btn"),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("输入验证码", style = TitleLg)
        Spacer(Modifier.height(4.dp))
        Text("已发送至 +86 ${PhoneUtils.mask(state.phone)}", style = BodySm)

        Spacer(Modifier.height(24.dp))
        CodeBoxes(
            value = state.code,
            onValueChange = vm::onCodeChange,
            isError = state.errorMessage != null,
            modifier = Modifier.testTag("code-boxes"),
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.errorMessage!!, style = Caption.copy(color = StateError))
        }

        Spacer(Modifier.height(16.dp))
        if (state.countdown > 0) {
            Text("${state.countdown}s 后重新发送", style = Caption.copy(color = BrandPrimary))
        } else {
            Text(
                "重新发送",
                style = Caption.copy(color = BrandPrimary),
                modifier = Modifier.clickable { vm.resendCode() }.testTag("resend-btn"),
            )
        }

        Spacer(Modifier.weight(1f))
        Text("收不到验证码？", style = Caption.copy(color = BrandPrimary))
    }
}
