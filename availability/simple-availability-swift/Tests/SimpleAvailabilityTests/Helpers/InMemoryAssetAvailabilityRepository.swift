import Foundation
@testable import SimpleAvailability

final class InMemoryAssetAvailabilityRepository: AssetAvailabilityRepository {
    private(set) var content: [AssetId: AssetAvailability] = [:]

    func save(assetAvailability: AssetAvailability) {
        content[assetAvailability.assetId] = assetAvailability
    }

    func find(by assetId: AssetId) -> AssetAvailability? {
        content[assetId]
    }

    func findOverdue() -> [AssetAvailability] {
        []
    }
}
