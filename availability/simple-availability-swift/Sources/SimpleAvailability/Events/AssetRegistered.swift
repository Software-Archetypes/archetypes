import Foundation

struct AssetRegistered: DomainEvent, Codable {
    let assetId: String
}

extension AssetRegistered {
    init(assetId: AssetId) {
        self.init(assetId: assetId.value)
    }
}
