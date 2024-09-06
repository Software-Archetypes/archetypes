import Foundation

extension Result {
    var success: Bool {
        switch self {
        case .success: true
        case .failure: false
        }
    }

    var failure: Bool {
        switch self {
        case .success: false
        case .failure: true
        }
    }
}
