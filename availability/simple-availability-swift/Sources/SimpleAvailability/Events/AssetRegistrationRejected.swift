import Foundation

struct AssetRegistrationRejected: DomainEvent, Error, Codable {
    let assetId: String
    let reason: String
}

extension AssetRegistrationRejected {
    static func dueToAlreadyExistingAssetWith(assetId: AssetId) -> Self {
        .init(assetId: assetId.value, reason: "ASSET_ALREADY_EXISTS")
    }
}
