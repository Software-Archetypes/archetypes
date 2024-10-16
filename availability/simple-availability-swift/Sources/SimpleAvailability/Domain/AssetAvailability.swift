import Foundation
import Sugar

final class AssetAvailability {
    let assetId: AssetId
    private(set) var currentLock: AssetLock?
    private var clock: Supplier<Date>

    init(assetId: AssetId, clock: Supplier<Date>) {
        self.assetId = assetId
        self.currentLock = MaintenanceLock()
        self.clock = clock
    }

    func activate() -> Result<AssetActivated, AssetActivationRejected> {
        if currentLock is MaintenanceLock {
            currentLock = nil
            return .success(AssetActivated(assetId: assetId))
        }
        return .failure(AssetActivationRejected.init(assetId: assetId, reason: "ASSET_ALREADY_ACTIVATED_REASON"))
    }

    func withdraw() -> Result<AssetWithdrawn, AssetWithdrawalRejected> {
        if currentLock == nil || currentLock is MaintenanceLock {
            currentLock = WithdrawalLock()
            return .success(AssetWithdrawn.init(assetId: assetId))
        }
        return .failure(AssetWithdrawalRejected.init(assetId: assetId, reason: "ASSET_CURRENTLY_LOCKED"))
    }

    func lockFor(ownerId: OwnerId, time: Duration) -> Result<AssetLocked, AssetLockRejected> {
        if currentLock == nil {
            let validUntil = clock().advanced(by: TimeInterval(time))
            currentLock = OwnerLock(ownerId: ownerId, until: validUntil)
            return .success(AssetLocked(assetId: assetId, ownerId: ownerId, from: validUntil))
        }
        return .failure(AssetLockRejected.init(assetId: assetId, ownerId: ownerId, reason: "ASSET_LOCKED_REASON"))
    }

    func lockIndefinitelyFor(ownerId: OwnerId) -> Result<AssetLocked, AssetLockRejected> {
        if thereIsAnActiveLockFor(ownerId: ownerId) {
            let validUntil = clock().addingTimeInterval(TimeInterval(.days(365)))
            currentLock = OwnerLock(ownerId: ownerId, until: validUntil)
            return .success(AssetLocked.init(assetId: assetId, ownerId: ownerId, from: validUntil))
        } else {
            return .failure(AssetLockRejected(assetId: assetId, ownerId: ownerId, reason: "NO_LOCK_DEFINED_FOR_OWNER_REASON"))
        }
    }

    func unlockFor(ownerId: OwnerId, at: Date) -> Result<AssetUnlocked, AssetUnlockingRejected> {
        if thereIsAnActiveLockFor(ownerId: ownerId) {
            currentLock = nil
            return .success(AssetUnlocked(assetId: assetId, ownerId: ownerId, unlockedAt: at))
        }
        return .failure(AssetUnlockingRejected.init(assetId: assetId, ownerId: ownerId, reason: "NO_LOCK_ON_THE_ASSET_REASON"))
    }

    func unlockIfOverdue() -> AssetLockExpired? {
        if currentLock != nil {
            currentLock = nil
            return AssetLockExpired.init(assetId: assetId)
        }

        return nil
    }

    func with(lock: AssetLock) -> Self {
        currentLock = lock
        return self
    }

    private func thereIsAnActiveLockFor(ownerId: OwnerId) -> Bool {
        currentLock?.wasMadeFor(ownerId: ownerId) ?? false
    }
}
