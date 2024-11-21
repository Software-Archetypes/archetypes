import Foundation

struct AssetActivated: DomainEvent, Encodable, Equatable {
    let assetId: String
}

extension AssetActivated {
    init(assetId: AssetId) {
        self.init(assetId: assetId.value)
    }
}
