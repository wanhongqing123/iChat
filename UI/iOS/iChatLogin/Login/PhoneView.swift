import SwiftUI

struct PhoneView: View {
    @ObservedObject var store: LoginStore

    var body: some View {
        VStack(spacing: 20) {
            Spacer().frame(height: 24)

            Text("💬")
                .font(.system(size: 28))
                .frame(width: 56, height: 56)
                .background(Color.bgSurface, in: Circle())
                .shadow(color: Color.brandPrimary.opacity(0.18), radius: 12, y: 6)

            VStack(spacing: 4) {
                Text("欢迎使用 iChat").font(.titleLg).foregroundStyle(Color.textPrimary)
                Text("输入手机号，开启陪伴").font(.bodySm).foregroundStyle(Color.textSecondary)
            }

            PhoneField(rawDigits: Binding(
                get: { store.phone },
                set: { store.onPhoneChange($0) }
            ))
            .accessibilityIdentifier("phone-input")

            PrimaryButton(
                title: store.isSubmitting ? "发送中…" : "获取验证码",
                isEnabled: store.canRequestCode
            ) {
                Task { await store.requestCode() }
            }
            .accessibilityIdentifier("get-code-btn")

            if let err = store.errorMessage {
                Text(err).font(.caption).foregroundStyle(Color.stateError)
            }

            Spacer()

            Text("未注册手机号将自动创建账号")
                .font(.caption)
                .foregroundStyle(Color.textTertiary)
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 32)
        .iChatPage()
    }
}
