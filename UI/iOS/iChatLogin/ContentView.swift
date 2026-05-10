import SwiftUI

struct ContentView: View {
    @ObservedObject var store: LoginStore

    var body: some View {
        if store.didLoginSuccess {
            HomePlaceholderView()
        } else {
            NavigationStack {
                PhoneView(store: store)
                    .navigationDestination(isPresented: Binding(
                        get: { store.phase == .code },
                        set: { isPresented in if !isPresented { store.goBack() } }
                    )) {
                        CodeView(store: store).navigationBarBackButtonHidden(true)
                    }
            }
        }
    }
}
