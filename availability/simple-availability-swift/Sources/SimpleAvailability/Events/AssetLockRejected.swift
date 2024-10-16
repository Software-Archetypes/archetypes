import Foundation

struct AssetLockRejected: DomainEvent, Error, Encodable {
    let assetId: String
    let ownerId: String
    let reason: String
}

extension AssetLockRejected {
    init(assetId: AssetId, ownerId: OwnerId, reason: String) {
        self.init(assetId: assetId.value, ownerId: ownerId.value, reason: reason)
    }

    static func dueToMissingAssetWith(assetId: AssetId, ownerId: OwnerId) -> Self {
        .init(assetId: assetId.value, ownerId: ownerId.value, reason: "ASSET_IS_MISSING")
    }
}
