import Foundation

protocol AssetLock {
    var ownerId: OwnerId { get }

    func wasMadeFor(ownerId: OwnerId) -> Bool
}

extension AssetLock {
    func wasMadeFor(ownerId: OwnerId) -> Bool {
        self.ownerId == ownerId
    }
}

struct WithdrawalLock: AssetLock {
    let ownerId: OwnerId = .withdrawal
}

struct MaintenanceLock: AssetLock {
    let ownerId: OwnerId = .maintenance
}

struct OwnerLock: AssetLock {
    let ownerId: OwnerId
    let until: Date
}
