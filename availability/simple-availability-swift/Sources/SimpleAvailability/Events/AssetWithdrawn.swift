import Foundation

struct AssetWithdrawn: DomainEvent, Codable {
    let assetId: String
}

extension AssetWithdrawn {
    init(assetId: AssetId) {
        self.init(assetId: assetId.value)
    }
}
