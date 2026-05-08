import SwiftUI

struct PhoneField: View {
    @Binding var rawDigits: String
    var onCommit: () -> Void = {}

    var body: some View {
        HStack(spacing: 12) {
            Text("+86").font(.bodyMd).foregroundStyle(Color.textSecondary)
            Rectangle().fill(Color.borderInput).frame(width: 1, height: 16)
            TextField(
                "请输入手机号",
                text: Binding(
                    get: { PhoneUtils.format(rawDigits) },
                    set: { rawDigits = String(PhoneUtils.digitsOnly($0).prefix(11)) }
                )
            )
            .keyboardType(.numberPad)
            .font(.bodyMd)
            .foregroundStyle(Color.textPrimary)
        }
        .padding(.horizontal, 16)
        .frame(height: 48)
        .background(Color.bgSurface, in: RoundedRectangle(cornerRadius: Shapes.inputCornerRadius))
        .overlay(
            RoundedRectangle(cornerRadius: Shapes.inputCornerRadius)
                .stroke(Color.borderInput, lineWidth: 1)
        )
    }
}
