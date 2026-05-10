import Foundation

/// 鉴权 / 登录数据接口。具体实现以插件方式提供（Mock / 未来真实后端），
/// 上层（LoginStore）只持有此 protocol。
protocol AuthService {
    func requestCode(phone: String) async throws -> AuthResult
    func verifyCode(phone: String, code: String) async throws -> AuthResult
}
