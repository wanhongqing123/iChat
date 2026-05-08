import Foundation

enum PhoneUtils {

    static func digitsOnly(_ input: String) -> String {
        input.filter(\.isWholeNumber)
    }

    static func format(_ raw: String) -> String {
        let digits = String(digitsOnly(raw).prefix(11))
        switch digits.count {
        case 0...3:  return digits
        case 4...7:  return "\(digits.prefix(3)) \(digits.dropFirst(3))"
        default:
            let p1 = digits.prefix(3)
            let p2 = digits.dropFirst(3).prefix(4)
            let p3 = digits.dropFirst(7)
            return "\(p1) \(p2) \(p3)"
        }
    }

    static func isValid(_ phone: String) -> Bool {
        let d = digitsOnly(phone)
        guard d.count == 11 else { return false }
        guard d.first == "1" else { return false }
        let second = d[d.index(d.startIndex, offsetBy: 1)]
        return ("3"..."9").contains(second)
    }

    static func mask(_ phone: String) -> String {
        let d = digitsOnly(phone)
        guard d.count == 11 else { return d }
        return "\(d.prefix(3))****\(d.suffix(4))"
    }
}
