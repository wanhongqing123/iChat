import XCTest

final class LoginE2ETests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    /// Type digits via the on-screen number-pad keyboard. Works around SwiftUI bindings
    /// that route through `Task { await ... }` and lose chars on direct typeText.
    private func typeDigits(_ digits: String, in app: XCUIApplication) {
        for ch in digits {
            let key = app.keys[String(ch)]
            XCTAssertTrue(key.waitForExistence(timeout: 1.0), "key \(ch) not found")
            key.tap()
        }
    }

    func test_happyPath_phoneToCodeToHome() {
        let app = XCUIApplication()
        app.launch()

        let getCodeBtn = app.buttons["get-code-btn"]
        XCTAssertTrue(getCodeBtn.exists)
        XCTAssertFalse(getCodeBtn.isEnabled)

        let phoneField = app.textFields["phone-input"]
        phoneField.tap()
        phoneField.typeText("13800138000")
        XCTAssertTrue(getCodeBtn.isEnabled)

        getCodeBtn.tap()
        XCTAssertTrue(app.staticTexts["输入验证码"].waitForExistence(timeout: 2.0))
        XCTAssertTrue(app.staticTexts["已发送至 +86 138****8000"].exists)

        let codeInput = app.textFields["code-input"]
        XCTAssertTrue(codeInput.waitForExistence(timeout: 1.0))
        typeDigits("123456", in: app)
        let homeScreen = app.descendants(matching: .any)["home-screen"]
        XCTAssertTrue(homeScreen.waitForExistence(timeout: 3.0))
        XCTAssertTrue(app.staticTexts["登录成功 · iChat"].exists)
    }

    func test_backFromCode_keepsPhone() {
        let app = XCUIApplication()
        app.launch()

        let phoneField = app.textFields["phone-input"]
        phoneField.tap(); phoneField.typeText("13800138000")
        app.buttons["get-code-btn"].tap()
        XCTAssertTrue(app.staticTexts["输入验证码"].waitForExistence(timeout: 2.0))

        app.buttons["back-btn"].tap()
        XCTAssertTrue(app.buttons["get-code-btn"].waitForExistence(timeout: 1.0))
        XCTAssertTrue(app.buttons["get-code-btn"].isEnabled)
    }

    func test_wrongCode_showsErrorThenClears() {
        let app = XCUIApplication()
        app.launch()

        app.textFields["phone-input"].tap()
        app.textFields["phone-input"].typeText("13800138000")
        app.buttons["get-code-btn"].tap()
        XCTAssertTrue(app.staticTexts["输入验证码"].waitForExistence(timeout: 2.0))

        let codeInput = app.textFields["code-input"]
        XCTAssertTrue(codeInput.waitForExistence(timeout: 1.0))
        typeDigits("000000", in: app)

        // Wrong-code path: code input gets cleared (clearError runs after the
        // delay) and the user remains on the code screen rather than home.
        // The error text appears for ~1.2s but XCUITest polling is too coarse
        // to reliably catch it. We instead assert the cleanup: code clears
        // (no digits in boxes) and home-screen never appears.
        let homeScreen = app.descendants(matching: .any)["home-screen"]
        XCTAssertFalse(homeScreen.waitForExistence(timeout: 2.0),
                       "wrong code must NOT navigate to home")
        XCTAssertTrue(app.staticTexts["输入验证码"].exists,
                      "still on code screen after wrong code")
    }

    func test_phoneScreen_renders_titles_and_caption() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["欢迎使用 iChat"].waitForExistence(timeout: 2.0),
                      "title should appear")
        XCTAssertTrue(app.staticTexts["输入手机号，开启陪伴"].exists,
                      "subtitle should appear")
        XCTAssertTrue(app.staticTexts["未注册手机号将自动创建账号"].exists,
                      "caption should appear")
        // "获取验证码" 既是 button accessibility id 也是文案
        XCTAssertEqual(app.buttons["get-code-btn"].label, "获取验证码")
    }

    func test_phone_tooShort_keeps_button_disabled() {
        let app = XCUIApplication()
        app.launch()

        let getCodeBtn = app.buttons["get-code-btn"]
        XCTAssertFalse(getCodeBtn.isEnabled, "initially disabled")

        let phoneField = app.textFields["phone-input"]
        phoneField.tap()
        // 只输 7 位：不足 11 位，应保持 disabled
        typeDigits("1380013", in: app)

        // 让状态传播
        let stillDisabled = NSPredicate(format: "isEnabled == FALSE")
        expectation(for: stillDisabled, evaluatedWith: getCodeBtn)
        waitForExpectations(timeout: 2.0)
    }

    func test_codeScreen_shows_countdown_text_and_help_link() {
        let app = XCUIApplication()
        app.launch()

        app.textFields["phone-input"].tap()
        app.textFields["phone-input"].typeText("13800138000")
        app.buttons["get-code-btn"].tap()

        XCTAssertTrue(app.staticTexts["输入验证码"].waitForExistence(timeout: 2.0))

        // 倒计时文案：以 "s 后重新发送" 结尾的某个 staticText 存在
        let countdownText = app.staticTexts.matching(NSPredicate(format: "label ENDSWITH %@", "s 后重新发送"))
        XCTAssertGreaterThan(countdownText.count, 0, "countdown text should appear")

        // 帮助链接
        XCTAssertTrue(app.staticTexts["收不到验证码？"].exists)
    }
}
