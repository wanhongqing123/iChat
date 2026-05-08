import XCTest
@testable import iChatLogin

final class MockAuthServiceTests: XCTestCase {

    func test_requestCode_returnsSuccess() async throws {
        let svc = MockAuthService()
        let result = try await svc.requestCode(phone: "13800138000")
        XCTAssertEqual(result, .requestSuccess)
    }

    func test_verifyCode_correctCode_returnsVerifySuccess() async throws {
        let svc = MockAuthService()
        let result = try await svc.verifyCode(phone: "13800138000", code: "123456")
        guard case let .verifySuccess(token, isNewUser) = result else {
            XCTFail("expected verifySuccess, got \(result)"); return
        }
        XCTAssertEqual(token, "mock-token")
        XCTAssertEqual(isNewUser, false)
    }

    func test_verifyCode_wrongCode_returnsInvalidCode() async throws {
        let svc = MockAuthService()
        let result = try await svc.verifyCode(phone: "13800138000", code: "000000")
        XCTAssertEqual(result, .invalidCode)
    }
}
