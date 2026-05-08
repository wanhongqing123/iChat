import SwiftUI

struct ContentView: View {
    var body: some View {
        ZStack {
            Color.bgPage.ignoresSafeArea()
            Text("iChat").font(.titleLg).foregroundStyle(Color.textPrimary)
        }
    }
}
