import Foundation

struct AssetUnlockingRejected: DomainEvent, Error, Encodable {
    let assetId: String
    let ownerId: String
    let reason: String
}

extension AssetUnlockingRejected {
    init(assetId: AssetId, ownerId: OwnerId, reason: String) {
        self.init(assetId: assetId.value, ownerId: ownerId.value, reason: reason)
    }

    static func dueToMissingAssetWith(assetId: AssetId, ownerId: OwnerId) -> Self {
        .init(assetId: assetId, ownerId: ownerId, reason: "ASSET_IS_MISSING")
    }
}
