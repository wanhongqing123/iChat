import Foundation
import SwiftUI

enum LoginPhase { case phone, code }

@MainActor
final class LoginStore: ObservableObject {

    @Published private(set) var phase: LoginPhase = .phone
    @Published var phone: String = ""
    @Published var code: String = ""
    @Published private(set) var countdown: Int = 0
    @Published private(set) var isSubmitting: Bool = false
    @Published private(set) var errorMessage: String?
    @Published private(set) var didLoginSuccess: Bool = false

    private let service: AuthService
    private let countdownTickInterval: Duration
    private let errorClearDelay: Duration
    private var countdownTask: Task<Void, Never>?
    private var errorClearTask: Task<Void, Never>?

    init(
        service: AuthService = MockAuthService(),
        countdownTickInterval: Duration = .seconds(1),
        errorClearDelay: Duration = .milliseconds(1_200)
    ) {
        self.service = service
        self.countdownTickInterval = countdownTickInterval
        self.errorClearDelay = errorClearDelay
    }

    var canRequestCode: Bool {
        PhoneUtils.isValid(phone) && !isSubmitting
    }

    func onPhoneChange(_ raw: String) {
        phone = String(PhoneUtils.digitsOnly(raw).prefix(11))
    }

    func onCodeChange(_ raw: String) async {
        let digits = String(raw.filter(\.isWholeNumber).prefix(6))
        code = digits
        errorMessage = nil
        if digits.count == 6 { await verifyCode() }
    }

    func requestCode() async {
        guard PhoneUtils.isValid(phone), !isSubmitting else { return }
        if countdown > 0 {
            phase = .code
            errorMessage = nil
            return
        }
        isSubmitting = true; errorMessage = nil
        do {
            switch try await service.requestCode(phone: phone) {
            case .requestSuccess:
                code = ""; phase = .code; countdown = 60; isSubmitting = false
                startCountdown()
            default:
                isSubmitting = false; errorMessage = "网络异常，请检查后重试"
            }
        } catch {
            isSubmitting = false; errorMessage = "网络异常，请检查后重试"
        }
    }

    func resendCode() async {
        guard countdown == 0 else { return }
        do {
            if case .requestSuccess = try await service.requestCode(phone: phone) {
                countdown = 60
                startCountdown()
            } else {
                errorMessage = "网络异常，请检查后重试"
            }
        } catch {
            errorMessage = "网络异常，请检查后重试"
        }
    }

    private func startCountdown() {
        countdownTask?.cancel()
        countdownTask = Task { [weak self] in
            guard let self else { return }
            while await self.countdown > 0 {
                try? await Task.sleep(for: self.countdownTickInterval)
                await self.tick()
            }
        }
    }

    private func tick() {
        countdown = max(0, countdown - 1)
    }

    private func verifyCode() async {
        isSubmitting = true
        defer { isSubmitting = false }
        do {
            switch try await service.verifyCode(phone: phone, code: code) {
            case .verifySuccess: didLoginSuccess = true
            case .invalidCode:   showCodeError()
            case .networkError:  errorMessage = "网络异常，请检查后重试"
            default: break
            }
        } catch {
            errorMessage = "网络异常，请检查后重试"
        }
    }

    private func showCodeError() {
        errorMessage = "验证码错误，请重新输入"
        errorClearTask?.cancel()
        errorClearTask = Task { [weak self] in
            try? await Task.sleep(for: self?.errorClearDelay ?? .milliseconds(1_200))
            await self?.clearError()
        }
    }

    private func clearError() {
        code = ""
        errorMessage = nil
    }

    func goBack() {
        phase = .phone
        errorMessage = nil
    }
}
