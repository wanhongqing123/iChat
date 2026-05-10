package com.ichat.login.data

/**
 * 鉴权 / 登录数据接口。具体实现以插件方式提供（Mock / 未来真实后端），
 * 上层（LoginViewModel）只持有此接口。
 */
interface AuthService {
    suspend fun requestCode(phone: String): AuthResult
    suspend fun verifyCode(phone: String, code: String): AuthResult
}
