package com.ichat.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ichat.login.login.LoginNavGraph
import com.ichat.login.theme.IChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IChatTheme { LoginNavGraph() }
        }
    }
}
