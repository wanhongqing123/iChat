import XCTest
@testable import iChatLogin

final class PhoneUtilsTests: XCTestCase {

    func test_digitsOnly_removesNonDigits() {
        XCTAssertEqual(PhoneUtils.digitsOnly("138 0013 8000"), "13800138000")
        XCTAssertEqual(PhoneUtils.digitsOnly("138-0013-8000"), "13800138000")
        XCTAssertEqual(PhoneUtils.digitsOnly("abc"),           "")
    }

    func test_format_appliesGroupedSpacing() {
        XCTAssertEqual(PhoneUtils.format(""),              "")
        XCTAssertEqual(PhoneUtils.format("1"),             "1")
        XCTAssertEqual(PhoneUtils.format("138"),           "138")
        XCTAssertEqual(PhoneUtils.format("1380"),          "138 0")
        XCTAssertEqual(PhoneUtils.format("1380013"),       "138 0013")
        XCTAssertEqual(PhoneUtils.format("13800138"),      "138 0013 8")
        XCTAssertEqual(PhoneUtils.format("13800138000"),   "138 0013 8000")
    }

    func test_format_truncatesAt11Digits() {
        XCTAssertEqual(PhoneUtils.format("13800138000999"), "138 0013 8000")
    }

    func test_isValid_acceptsValidChineseMobile() {
        XCTAssertTrue(PhoneUtils.isValid("13800138000"))
        XCTAssertTrue(PhoneUtils.isValid("15912345678"))
        XCTAssertTrue(PhoneUtils.isValid("18800001111"))
    }

    func test_isValid_rejectsInvalid() {
        XCTAssertFalse(PhoneUtils.isValid(""))
        XCTAssertFalse(PhoneUtils.isValid("1380013800"))
        XCTAssertFalse(PhoneUtils.isValid("138001380001"))
        XCTAssertFalse(PhoneUtils.isValid("00000000000"))
        XCTAssertFalse(PhoneUtils.isValid("12345678901"))
        XCTAssertFalse(PhoneUtils.isValid("1a800138000"))
    }

    func test_mask_hidesMiddleFour() {
        XCTAssertEqual(PhoneUtils.mask("13800138000"), "138****8000")
    }

    func test_mask_returnsAsIs_whenNot11Digits() {
        XCTAssertEqual(PhoneUtils.mask(""),      "")
        XCTAssertEqual(PhoneUtils.mask("12345"), "12345")
    }
}
