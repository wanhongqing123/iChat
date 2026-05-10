package com.ichat.login

import android.app.Application
import com.ichat.login.data.AuthService
import com.ichat.login.data.MockAuthService

/**
 * Composition root：所有外部依赖（AuthService / 未来 IMService）在这里实例化，
 * 全 App 共享。手动 DI，不引 Hilt/Koin（YAGNI）。
 */
class IChatApplication : Application() {
    val authService: AuthService by lazy { MockAuthService() }
}
