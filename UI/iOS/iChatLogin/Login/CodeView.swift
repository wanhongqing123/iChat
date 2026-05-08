import SwiftUI

struct CodeView: View {
    @ObservedObject var store: LoginStore

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button { store.goBack() } label: {
                    Text("‹").font(.titleLg).foregroundStyle(Color.textPrimary)
                        .frame(width: 44, height: 44)
                }
                .accessibilityIdentifier("back-btn")
                Spacer()
            }
            .frame(height: 44)

            VStack(spacing: 4) {
                Text("输入验证码").font(.titleLg).foregroundStyle(Color.textPrimary)
                Text("已发送至 +86 \(PhoneUtils.mask(store.phone))")
                    .font(.bodySm).foregroundStyle(Color.textSecondary)
            }

            Spacer().frame(height: 8)

            CodeBoxes(
                value: Binding(
                    get: { store.code },
                    set: { v in Task { await store.onCodeChange(v) } }
                ),
                isError: store.errorMessage != nil
            )

            if let err = store.errorMessage {
                Text(err).font(.caption).foregroundStyle(Color.stateError)
            }

            if store.countdown > 0 {
                Text("\(store.countdown)s 后重新发送").font(.caption).foregroundStyle(Color.brandPrimary)
            } else {
                Button { Task { await store.resendCode() } } label: {
                    Text("重新发送").font(.caption).foregroundStyle(Color.brandPrimary)
                }
                .accessibilityIdentifier("resend-btn")
            }

            Spacer()

            Text("收不到验证码？").font(.caption).foregroundStyle(Color.brandPrimary)
        }
        .padding(.horizontal, 24)
        .iChatPage()
    }
}
