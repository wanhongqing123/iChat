import Foundation

/// Composition root：所有外部依赖（AuthService / 未来 IMService）在这里实例化，
/// 全 App 共享。手动 DI，不引第三方 DI 框架（YAGNI）。
@MainActor
final class AppServices {
    let authService: AuthService

    init(authService: AuthService = MockAuthService()) {
        self.authService = authService
    }

    func makeLoginStore() -> LoginStore {
        LoginStore(service: authService)
    }
}
