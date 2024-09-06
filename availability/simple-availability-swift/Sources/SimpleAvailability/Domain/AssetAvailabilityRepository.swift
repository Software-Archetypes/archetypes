import Foundation

protocol AssetAvailabilityRepository {
    func save(assetAvailability: AssetAvailability)
    func find(by assetId: AssetId) -> AssetAvailability?
    func findOverdue() -> [AssetAvailability]
}
