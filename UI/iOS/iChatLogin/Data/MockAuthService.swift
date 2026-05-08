import Foundation

enum AuthResult: Equatable {
    case requestSuccess
    case verifySuccess(token: String, isNewUser: Bool)
    case invalidCode
    case networkError
}

class MockAuthService {

    func requestCode(phone: String) async throws -> AuthResult {
        try await Task.sleep(nanoseconds: 800_000_000)
        return .requestSuccess
    }

    func verifyCode(phone: String, code: String) async throws -> AuthResult {
        try await Task.sleep(nanoseconds: 600_000_000)
        return code == "123456"
            ? .verifySuccess(token: "mock-token", isNewUser: false)
            : .invalidCode
    }
}
