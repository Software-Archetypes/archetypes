import Foundation
import Sugar

struct AvailabilityService {
    let repository: AssetAvailabilityRepository
    let publisher: DomainEventsPublisher
    let clock: Supplier<Date>

    func registerAssetWith(assetId: AssetId) -> Result<AssetRegistered, AssetRegistrationRejected> {
        if repository.find(by: assetId) == nil {
            let asset = AssetAvailability(assetId: assetId, clock: clock)
            let event = AssetRegistered(assetId: assetId)
            repository.save(assetAvailability: asset)
            publisher.publish(event: event)
            return .success(event)
        } else {
            return .failure(AssetRegistrationRejected.dueToAlreadyExistingAssetWith(assetId: assetId))
        }
    }

    func activate(assetId: AssetId) -> Result<AssetActivated, AssetActivationRejected> {
        repository
            .find(by: assetId)
            .map { asset in
                handle(asset, asset.activate())
            }
            .or(else: .failure(AssetActivationRejected.missing(assetId: assetId)))
    }

    func withdraw(assetId: AssetId) -> Result<AssetWithdrawn, AssetWithdrawalRejected> {
        repository
            .find(by: assetId)
            .map { asset in
                handle(asset, asset.withdraw())
            }
            .or(else: .failure(AssetWithdrawalRejected.dueToMissingAssetWith(assetId: assetId)))
    }


    func lock(assetId: AssetId, ownerId: OwnerId, time: Duration) -> Result<AssetLocked, AssetLockRejected> {
        repository
            .find(by: assetId)
            .map { asset in
                handle(asset, asset.lockFor(ownerId: ownerId, time: time))
            }
            .or(else: .failure(AssetLockRejected.dueToMissingAssetWith(assetId: assetId, ownerId: ownerId)))
    }

    func lockIndefinitely(assetId: AssetId, ownerId: OwnerId) -> Result<AssetLocked, AssetLockRejected> {
        repository
            .find(by: assetId)
            .map { asset in
                handle(asset, asset.lockIndefinitelyFor(ownerId: ownerId))
            }
            .or(else: .failure(AssetLockRejected.dueToMissingAssetWith(assetId: assetId, ownerId: ownerId)))
    }

    func unlock(assetId: AssetId, ownerId: OwnerId, at date: Date) -> Result<AssetUnlocked, AssetUnlockingRejected> {
        repository
            .find(by: assetId)
            .map { asset in
                handle(asset, asset.unlockFor(ownerId: ownerId, at: date))
            }
            .or(else: .failure(AssetUnlockingRejected.dueToMissingAssetWith(assetId: assetId, ownerId: ownerId)))
    }

    func unlockIfOverdue(assetAvailability: AssetAvailability) {
        if let event = assetAvailability.unlockIfOverdue() {
            repository.save(assetAvailability: assetAvailability)
            publisher.publish(event: event)
        }
    }

    private func handle<T: DomainEvent, U: DomainEvent&Error>(_ asset: AssetAvailability, _ result: Result<T, U>) -> Result<T, U> {
        do {
            let event = try result.get()

            repository.save(assetAvailability: asset)
            publisher.publish(event: event)
        } catch {

        }

        return result
    }
}
