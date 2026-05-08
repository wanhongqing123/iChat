import SwiftUI

struct CodeBoxes: View {
    @Binding var value: String
    let isError: Bool
    @FocusState private var focused: Bool

    var body: some View {
        ZStack {
            HStack(spacing: 8) {
                ForEach(0..<6, id: \.self) { i in
                    let ch = (i < value.count) ? String(value[value.index(value.startIndex, offsetBy: i)]) : ""
                    let isFocus = (i == value.count) && !isError && focused
                    Text(ch)
                        .font(.codeBox)
                        .foregroundStyle(Color.textPrimary)
                        .frame(width: 48, height: 48)
                        .background(Color.bgSurface,
                                    in: RoundedRectangle(cornerRadius: Shapes.codeBoxCornerRadius))
                        .overlay(
                            RoundedRectangle(cornerRadius: Shapes.codeBoxCornerRadius)
                                .stroke(
                                    isError ? Color.stateError
                                            : (isFocus ? Color.borderInputFocus : Color.borderInput),
                                    lineWidth: 1
                                )
                        )
                }
            }
            TextField("", text: Binding(
                get: { value },
                set: { value = String($0.filter(\.isWholeNumber).prefix(6)) }
            ))
            .keyboardType(.numberPad)
            .textContentType(.oneTimeCode)
            .focused($focused)
            .opacity(0.001)
            .accessibilityIdentifier("code-input")
        }
        .frame(height: 48)
        .onAppear { focused = true }
    }
}
