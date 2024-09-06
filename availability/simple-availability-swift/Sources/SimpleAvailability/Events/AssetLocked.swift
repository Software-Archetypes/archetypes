import Foundation

struct AssetLocked: DomainEvent, Encodable {
    let assetId: String
    let ownerId: String
    let validUntil: Date
}

extension AssetLocked {
    init(assetId: AssetId, ownerId: OwnerId, from: Date) {
        self.init(assetId: assetId.value, ownerId: ownerId.value, validUntil: from)
    }
}
