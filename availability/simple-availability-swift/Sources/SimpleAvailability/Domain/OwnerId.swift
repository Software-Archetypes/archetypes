import Foundation

struct OwnerId: Equatable {
    let value: String
}

extension OwnerId {
    static let withdrawal = Self(value: "WITHDRAWAL")
    static let maintenance = Self(value: "MAINTENANCE")
}

extension OwnerId {
    static func of(_ principal: Principal) -> Self {
        .init(value: principal.name)
    }
}
