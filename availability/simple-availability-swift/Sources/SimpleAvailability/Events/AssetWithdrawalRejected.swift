import Foundation

struct AssetWithdrawalRejected: DomainEvent, Error, Codable {
    let assetId: String
    let reason: String
}

extension AssetWithdrawalRejected {
    init(assetId: AssetId, reason: String) {
        self.init(assetId: assetId.value, reason: reason)
    }

    static func dueToMissingAssetWith(assetId: AssetId) -> Self {
        .init(assetId: assetId, reason: "ASSET_IS_MISSING")
    }
}
