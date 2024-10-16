import Foundation

struct AssetUnlocked: DomainEvent, Encodable {
    let assetId: String
    let ownerId: String
    let unlockedAt: Date
}

extension AssetUnlocked {
    init(assetId: AssetId, ownerId: OwnerId, unlockedAt: Date) {
        self.init(assetId: assetId.value, ownerId: ownerId.value, unlockedAt: unlockedAt)
    }
}
