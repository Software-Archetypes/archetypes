import Testing
@testable import SimpleAvailability

@Suite struct AssetAvailabilityTests {
    @Test func should_activate_the_new_asset() throws {
        let asset = AssetAvailability.fixture

        let result = asset.activate()

        #expect(result.success)
    }

    @Test func should_fail_to_activate_the_activated_asset() {
        let asset = AssetAvailability.fixture.thatIsActive()

        let result = asset.activate()

        #expect(result.failure)
    }

    @Test func should_fail_to_lock_inactive_asset() {
        let asset = AssetAvailability.fixture
        let owner = OwnerId.Fixture.some
        let duration = Duration.Fixture.some

        let result = asset.lockFor(ownerId: owner, time: duration)

        #expect(result.failure)
    }

    @Test func activated_asset_should_be_locked_for_given_period() {
        let asset = AssetAvailability.fixture.thatIsActive()
        let owner = OwnerId.Fixture.some
        let duration = Duration.Fixture.some

        let result = asset.lockFor(ownerId: owner, time: duration)

        #expect(result.success)
    }

    @Test func should_extend_the_lock_indefinitely_when_given_owner_has_already_locked_the_asset() {
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)

        let result = asset.lockIndefinitelyFor(ownerId: owner)

        #expect(result.success)
    }

    @Test func should_fail_to_extend_the_lock_when_there_is_no_lock_on_the_asset() {
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()

        let result = asset.lockIndefinitelyFor(ownerId: owner)

        #expect(result.failure)
    }

    @Test func should_fail_to_extend_the_lock_when_the_lock_exists_for_other_owner() {
        let owner1 = OwnerId.Fixture.some
        let owner2 = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner1)

        let result = asset.lockIndefinitelyFor(ownerId: owner2)

        #expect(result.failure)
    }

    @Test func should_fail_to_lock_already_locked_asset() {
        let owner1 = OwnerId.Fixture.some
        let owner2 = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner1)

        let result = asset.lockFor(ownerId: owner2, time: Duration.Fixture.some)

        #expect(result.failure)
    }

    @Test func should_unlock_the_locked_asset() {
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)

        let result = asset.unlockFor(ownerId: owner, at: .now)

        #expect(result.success)
    }

    @Test func should_fail_to_unlock_the_unlocked_asset() {
        let owner1 = OwnerId.Fixture.some
        let owner2 = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner1)
            .thenUnlocked(ownerId: owner1)

        let result = asset.unlockFor(ownerId: owner2, at: .now)

        #expect(result.failure)
    }

    @Test func should_withdraw_inactive_asset() {
        let asset = AssetAvailability.fixture

        let result = asset.withdraw()

        #expect(result.success)
    }

    @Test func should_withdraw_active_asset() {
        let asset = AssetAvailability.fixture
            .thatIsActive()

        let result = asset.withdraw()

        #expect(result.success)
    }

    @Test func should_withdraw_active_asset_that_was_unlocked() {
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)
            .thenUnlocked(ownerId: owner)

        let result = asset.withdraw()

        #expect(result.success)
    }

    @Test func should_fail_to_withdraw_the_locked_asset() {
        let owner = OwnerId.Fixture.some
        let asset = AssetAvailability.fixture
            .thatIsActive()
            .thatWasLockedByOwnerWithForSomeValidDuration(ownerId: owner)

        let result = asset.withdraw()

        #expect(result.failure)
    }
}
