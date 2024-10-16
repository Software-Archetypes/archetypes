import Foundation
import Testing
import Sugar
@testable import SimpleAvailability

@Suite struct AvailabilityServiceTests {
    func prepare() -> (InMemoryAssetAvailabilityRepository, InMemoryDomainEventsPublisher, AvailabilityService) {
        let clock = Supplier {
            try! Date("2001-02-03T04:05:06Z", strategy: .iso8601)
        }
        let repository = InMemoryAssetAvailabilityRepository()
        let publisher = InMemoryDomainEventsPublisher()
        let service = AvailabilityService(repository: repository, publisher: publisher, clock: clock)

        return (repository, publisher, service)
    }

    @Test func should_register_asset() throws {
        let (repository, _, service) = prepare()
        let assetId = AssetId.fixture

        _ = service.registerAssetWith(assetId: assetId)

        #expect(repository.find(by: assetId) != nil)
    }

    @Test func should_emit_AssetRegistered_event_when_successfully_registered_asset() {
        let (_, publisher, service) = prepare()
        let asset = AssetId.fixture

        _ = service.registerAssetWith(assetId: asset)

        #expect(publisher.contains(of: AssetRegistered.self, where: { $0.assetId == asset.value }))
    }
    
    @Test func should_withdraw_existing_asset() {
        let (repository, _, service) = prepare()
        let asset = AssetAvailability.fixture
        repository.save(assetAvailability: asset)

        _ = service.withdraw(assetId: asset.assetId)

        #expect(repository.find(by: asset.assetId)?.currentLock is WithdrawalLock)
    }

    @Test func should_emit_AssetWithdrawn_event_when_asset_was_successfully_withdrawn() {
        let (repository, publisher, service) = prepare()
        let asset = AssetAvailability.fixture
        repository.save(assetAvailability: asset)

        _ = service.withdraw(assetId: asset.assetId)

        #expect(publisher.contains(of: AssetWithdrawn.self, where: { $0.assetId == asset.assetId.value }))
    }

    @Test func should_lock_activated_asset() {
        let (repository, _, service) = prepare()
        let asset = AssetAvailability.fixture
            .thatIsActive()
        repository.save(assetAvailability: asset)
        let owner = OwnerId.Fixture.some

        _ = service.lock(assetId: asset.assetId, ownerId: owner, time: .minutes(17))

        let lock = repository.find(by: asset.assetId)?.currentLock
        #expect((lock as? OwnerLock)?.ownerId == owner)
    }

    @Test func should_emit_AssetLocked_event_when_asset_was_successfully_locked() {
        let (repository, publisher, service) = prepare()
        let asset = AssetAvailability.fixture
            .thatIsActive()
        repository.save(assetAvailability: asset)
        let owner = OwnerId.Fixture.some

        _ = service.lock(assetId: asset.assetId, ownerId: owner, time: .minutes(17))

        #expect(publisher.contains(of: AssetLocked.self, where: { $0.assetId == asset.assetId.value && $0.ownerId == owner.value }))
    }

    @Test func should_lock_indefinitely_activated_asset() {
        let (repository, _, service) = prepare()
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)
        repository.save(assetAvailability: asset)

        _ = service.lockIndefinitely(assetId: asset.assetId, ownerId: owner)

        let lock = repository.find(by: asset.assetId)?.currentLock
        #expect((lock as? OwnerLock)?.ownerId == owner)
    }

    @Test func should_emit_AssetLocked_event_when_asset_was_successfully_locked_indefinitely() {
        let (repository, publisher, service) = prepare()
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)
        repository.save(assetAvailability: asset)

        _ = service.lockIndefinitely(assetId: asset.assetId, ownerId: owner)

        #expect(publisher.contains(of: AssetLocked.self, where: { $0.assetId == asset.assetId.value && $0.ownerId == owner.value }))
    }

    @Test func should_unlock_asset() throws {
        let (repository, _, service) = prepare()
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)
        repository.save(assetAvailability: asset)

        _ = service.unlock(assetId: asset.assetId, ownerId: owner, at: .now)

        let xxx = try #require(repository.find(by: asset.assetId))
        #expect(xxx.currentLock == nil)
    }

    @Test func should_emit_AssetUnlocked_event_when_asset_was_successfully_unlocked() {
        let (repository, publisher, service) = prepare()
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)
        repository.save(assetAvailability: asset)

        _ = service.unlock(assetId: asset.assetId, ownerId: owner, at: .now)

        #expect(publisher.contains(of: AssetUnlocked.self, where: { $0.assetId == asset.assetId.value && $0.ownerId == owner.value }))
    }
}
