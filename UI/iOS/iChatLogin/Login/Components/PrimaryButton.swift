import SwiftUI

struct PrimaryButton: View {
    let title: String
    let isEnabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.bodyMd)
                .foregroundStyle(Color.bgSurface)
                .frame(maxWidth: .infinity, minHeight: 48)
                .background(isEnabled ? Color.brandPrimary : Color.brandPrimaryDisabled,
                            in: RoundedRectangle(cornerRadius: Shapes.buttonCornerRadius))
        }
        .disabled(!isEnabled)
    }
}
