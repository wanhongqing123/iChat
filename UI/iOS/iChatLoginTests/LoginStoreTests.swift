import XCTest
@testable import iChatLogin

@MainActor
final class LoginStoreTests: XCTestCase {

    struct StubService: AuthService {
        var requestImpl: (String) async -> AuthResult = { _ in .requestSuccess }
        var verifyImpl:  (String, String) async -> AuthResult = { _, c in
            c == "123456" ? .verifySuccess(token: "t", isNewUser: false) : .invalidCode
        }
        // requestCount can't be a stored mutable property on a struct used by reference.
        // For tests that need to count calls, use a class wrapper:
        final class Counter { var value = 0 }
        var counter = Counter()

        func requestCode(phone: String) async throws -> AuthResult {
            counter.value += 1
            return await requestImpl(phone)
        }
        func verifyCode(phone: String, code: String) async throws -> AuthResult {
            await verifyImpl(phone, code)
        }
    }

    func test_phoneTooShort_canRequestCode_isFalse() {
        let store = LoginStore(service: StubService())
        store.onPhoneChange("1380013")
        XCTAssertFalse(store.canRequestCode)
    }

    func test_phoneInvalidFormat_canRequestCode_isFalse() {
        let store = LoginStore(service: StubService())
        store.onPhoneChange("00000000000")
        XCTAssertFalse(store.canRequestCode)
    }

    func test_phoneValid_canRequestCode_isTrue() {
        let store = LoginStore(service: StubService())
        store.onPhoneChange("13800138000")
        XCTAssertTrue(store.canRequestCode)
    }

    func test_requestCode_movesToCodePhase_andStartsCountdown() async {
        let store = LoginStore(service: StubService(), countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        XCTAssertEqual(store.phase, .code)
        XCTAssertEqual(store.countdown, 60)
    }

    func test_countdown_decrementsOverTime() async {
        let store = LoginStore(service: StubService(), countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        try? await Task.sleep(nanoseconds: 35_000_000)
        XCTAssertLessThanOrEqual(store.countdown, 57)
        XCTAssertGreaterThanOrEqual(store.countdown, 56)
    }

    func test_verifyCode_correct_emitsLoginSuccess() async {
        let store = LoginStore(service: StubService(), countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        await store.onCodeChange("123456")
        XCTAssertTrue(store.didLoginSuccess)
    }

    func test_verifyCode_wrong_setsErrorThenClears() async {
        let store = LoginStore(
            service: StubService(),
            countdownTickInterval: .milliseconds(10),
            errorClearDelay: .milliseconds(50)
        )
        store.onPhoneChange("13800138000")
        await store.requestCode()
        await store.onCodeChange("000000")
        XCTAssertEqual(store.errorMessage, "验证码错误，请重新输入")
        try? await Task.sleep(nanoseconds: 80_000_000)
        XCTAssertNil(store.errorMessage)
        XCTAssertEqual(store.code, "")
    }

    func test_goBack_preservesPhoneAndCountdown() async {
        let store = LoginStore(service: StubService(), countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        try? await Task.sleep(nanoseconds: 35_000_000)
        let savedCountdown = store.countdown
        store.goBack()
        XCTAssertEqual(store.phase, .phone)
        XCTAssertEqual(store.phone, "13800138000")
        XCTAssertEqual(store.countdown, savedCountdown)
    }

    func test_requestCode_duringCountdown_skipsRefetchAndReentersCode() async {
        let stub = StubService()
        let store = LoginStore(service: stub, countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        XCTAssertEqual(stub.counter.value, 1)
        store.goBack()
        await store.requestCode()
        XCTAssertEqual(store.phase, .code)
        XCTAssertEqual(stub.counter.value, 1)
    }

    func test_networkError_onRequestCode_keepsPhonePhase() async {
        var stub = StubService()
        stub.requestImpl = { _ in .networkError }
        let store = LoginStore(service: stub, countdownTickInterval: .milliseconds(10))
        store.onPhoneChange("13800138000")
        await store.requestCode()
        XCTAssertEqual(store.phase, .phone)
        XCTAssertEqual(store.errorMessage, "网络异常，请检查后重试")
    }
}
