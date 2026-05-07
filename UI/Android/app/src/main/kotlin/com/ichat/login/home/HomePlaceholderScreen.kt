package com.ichat.login.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ichat.login.theme.BgPage
import com.ichat.login.theme.TitleLg

@Composable
fun HomePlaceholderScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BgPage).testTag("home-screen"),
        contentAlignment = Alignment.Center,
    ) {
        Text("登录成功 · iChat", style = TitleLg)
    }
}
