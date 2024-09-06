import Foundation

struct AssetActivationRejected: DomainEvent, Encodable, Error, Equatable {
    let assetId: String
    let reason: String
}

extension AssetActivationRejected {
    init(assetId: AssetId, reason: String) {
        self.init(assetId: assetId.value, reason: reason)
    }

    static func missing(assetId: AssetId) -> Self {
        .init(assetId: assetId, reason: "ASSET_IS_MISSING")
    }
}
