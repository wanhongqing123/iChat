import SwiftUI

struct HomePlaceholderView: View {
    var body: some View {
        ZStack {
            Color.bgPage.ignoresSafeArea()
            Text("登录成功 · iChat").font(.titleLg).foregroundStyle(Color.textPrimary)
        }
        .accessibilityIdentifier("home-screen")
    }
}
