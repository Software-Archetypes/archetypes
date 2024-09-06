import Foundation

struct OverdueLockHandling {
    let availabilityService: AvailabilityService
    let repository: AssetAvailabilityRepository

    func unlockOverdue() {
        for item in repository.findOverdue() {
            availabilityService.unlockIfOverdue(assetAvailability: item)
        }
    }
}
