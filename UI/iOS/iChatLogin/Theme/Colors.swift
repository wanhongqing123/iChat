import SwiftUI

extension Color {
    static let brandPrimary         = Color(red: 0x4F/255.0, green: 0x86/255.0, blue: 0xFF/255.0)
    static let brandPrimaryPressed  = Color(red: 0x3B/255.0, green: 0x6B/255.0, blue: 0xE0/255.0)
    static let brandPrimaryDisabled = Color(red: 0xB7/255.0, green: 0xCD/255.0, blue: 0xFA/255.0)

    static let bgPage    = Color(red: 0xF2/255.0, green: 0xF6/255.0, blue: 0xFF/255.0)
    static let bgSurface = Color.white

    static let borderInput      = Color(red: 0xDC/255.0, green: 0xE6/255.0, blue: 0xFA/255.0)
    static let borderInputFocus = Color.brandPrimary

    static let textPrimary   = Color(red: 0x1A/255.0, green: 0x22/255.0, blue: 0x33/255.0)
    static let textSecondary = Color(red: 0x78/255.0, green: 0x84/255.0, blue: 0xA3/255.0)
    static let textTertiary  = Color(red: 0x9A/255.0, green: 0xA3/255.0, blue: 0xB5/255.0)

    static let stateError = Color(red: 0xE5/255.0, green: 0x48/255.0, blue: 0x4D/255.0)
}
