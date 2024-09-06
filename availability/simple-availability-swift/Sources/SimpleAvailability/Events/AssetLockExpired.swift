import Foundation

struct AssetLockExpired: DomainEvent {
    let assetId: String
}

extension AssetLockExpired {
    init(assetId: AssetId) {
        self.init(assetId: assetId.value)
    }
}
