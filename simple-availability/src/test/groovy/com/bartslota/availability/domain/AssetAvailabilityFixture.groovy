package com.bartslota.availability.domain


import static com.bartslota.availability.domain.AssetIdFixture.someAssetId
import static com.bartslota.availability.domain.DurationFixture.someValidDuration
import static com.bartslota.availability.domain.OwnerIdFixture.someOwnerId
import static java.time.LocalDateTime.now

class AssetAvailabilityFixture {

    static AssetAvailability someNewAsset() {
        AssetAvailability.of(someAssetId())
    }

    static AssetAvailabilityBuilder someAsset() {
        new AssetAvailabilityBuilder().with(someAssetId())
    }

    static class AssetAvailabilityBuilder {

        private AssetAvailability assetAvailability
        private OwnerId lastLockOwnerId

        AssetAvailabilityBuilder with(AssetId assetId) {
            assetAvailability = AssetAvailability.of(assetId)
            this
        }

        AssetAvailabilityBuilder thatIsActive() {
            assetAvailability.activate()
            this
        }

        AssetAvailabilityLockBuilder thatWasLockedByOwnerWith(OwnerId ownerId) {
            new AssetAvailabilityLockBuilder(this, ownerId)
        }

        AssetAvailabilityLockBuilder thatWasLockedBySomeOwner() {
            lastLockOwnerId = someOwnerId()
            new AssetAvailabilityLockBuilder(this, lastLockOwnerId)
        }

        AssetAvailability get() {
            assetAvailability
        }

        private AssetAvailabilityBuilder executeOnAsset(Closure assetFunction) {
            assetFunction(assetAvailability)
            this
        }

        AssetAvailabilityBuilder thenUnlocked() {
            assetAvailability.unlockFor(lastLockOwnerId, now())
            this
        }
    }

    static class AssetAvailabilityLockBuilder {
        private AssetAvailabilityBuilder parent
        private OwnerId lockOwnerId

        AssetAvailabilityLockBuilder(AssetAvailabilityBuilder parent, OwnerId ownerId) {
            this.parent = parent
            this.lockOwnerId = ownerId
        }

        AssetAvailabilityBuilder forSomeValidDuration() {
            parent.executeOnAsset {
                AssetAvailability asset -> asset.lockFor(lockOwnerId, someValidDuration())
            }
        }
    }
}
