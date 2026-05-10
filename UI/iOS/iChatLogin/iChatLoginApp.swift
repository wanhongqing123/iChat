import SwiftUI

@main
struct iChatLoginApp: App {
    @StateObject private var loginStore: LoginStore

    init() {
        let services = AppServices()
        // _loginStore is the StateObject's underlying storage; can't `=` to a StateObject directly
        _loginStore = StateObject(wrappedValue: services.makeLoginStore())
    }

    var body: some Scene {
        WindowGroup { ContentView(store: loginStore) }
    }
}
