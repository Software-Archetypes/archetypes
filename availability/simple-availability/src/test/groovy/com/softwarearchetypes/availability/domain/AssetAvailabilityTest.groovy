package com.softwarearchetypes.availability.domain


import com.softwarearchetypes.availability.events.AssetActivated
import com.softwarearchetypes.availability.events.AssetActivationRejected
import com.softwarearchetypes.availability.events.AssetLockRejected
import com.softwarearchetypes.availability.events.AssetLocked
import com.softwarearchetypes.availability.events.AssetUnlocked
import com.softwarearchetypes.availability.events.AssetUnlockingRejected
import com.softwarearchetypes.availability.events.AssetWithdrawalRejected
import com.softwarearchetypes.availability.events.AssetWithdrawn
import com.softwarearchetypes.availability.common.Result
import spock.lang.Specification

import java.time.Duration

import static AssetAvailabilityFixture.someAsset
import static AssetAvailabilityFixture.someNewAsset
import static AssetIdFixture.someAssetId
import static DurationFixture.someValidDuration
import static OwnerIdFixture.someOwnerId
import static java.time.LocalDateTime.now

class AssetAvailabilityTest extends Specification {

    def "should create asset availability with given id"() {
        given:
            AssetId assetId = someAssetId()

        expect:
            AssetAvailability.of(assetId) != null
    }

    def "should activate the new asset"() {
        given:
            AssetAvailability asset = someNewAsset()

        when:
            Result<AssetActivationRejected, AssetActivated> result = asset.activate()

        then:
            result.success()
    }

    def "should fail to activate the activated asset"() {
        given:
            AssetAvailability asset = someAsset().thatIsActive().get()

        when:
            Result<AssetActivationRejected, AssetActivated> result = asset.activate()

        then:
            result.failure()
    }

    def "should fail to lock inactive asset"() {
        given:
            AssetAvailability asset = someNewAsset()
            OwnerId ownerId = someOwnerId()
            Duration duration = someValidDuration()

        when:
            def result = asset.lockFor(ownerId, duration)

        then:
            result.failure()
    }

    def "activated asset should be locked for given period"() {
        given:
            AssetAvailability asset = someAsset().thatIsActive().get()
            OwnerId ownerId = someOwnerId()
            Duration duration = someValidDuration()

        when:
            def result = asset.lockFor(ownerId, duration)

        then:
            result.success()
    }

    def "should extend the lock indefinitely when given owner has already locked the asset"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedByOwnerWith(ownerId).forSomeValidDuration()
                    .get()

        when:
            Result<AssetLockRejected, AssetLocked> result = asset.lockIndefinitelyFor(ownerId)

        then:
            result.success()
    }

    def "should fail to extend the lock when there is no lock on the asset"() {
        given:
            AssetAvailability asset = someAsset().thatIsActive().get()

        when:
            Result<AssetLockRejected, AssetLocked> result = asset.lockIndefinitelyFor(someOwnerId())

        then:
            result.failure()
    }

    def "should fail to extend the lock when the lock exists for other owner"() {
        given:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedBySomeOwner().forSomeValidDuration()
                    .get()

        when:
            Result<AssetLockRejected, AssetLocked> result = asset.lockIndefinitelyFor(someOwnerId())

        then:
            result.failure()
    }

    def "should fail to lock already locked asset"() {
        given:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedBySomeOwner().forSomeValidDuration()
                    .get()

        when:
            def result = asset.lockFor(someOwnerId(), someValidDuration())

        then:
            result.failure()
    }

    def "should unlock the locked asset"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedByOwnerWith(ownerId).forSomeValidDuration()
                    .get()

        when:
            Result<AssetUnlockingRejected, AssetUnlocked> result = asset.unlockFor(ownerId, now())

        then:
            result.success()
    }

    def "should fail to unlock the unlocked asset"() {
        given:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedBySomeOwner().forSomeValidDuration()
                    .thenUnlocked()
                    .get()

        when:
            def result = asset.unlockFor(someOwnerId(), now())

        then:
            result.failure()
    }

    def "should withdraw inactive asset"() {
        given:
            AssetAvailability asset = someNewAsset()

        when:
            Result<AssetWithdrawalRejected, AssetWithdrawn> result = asset.withdraw()

        then:
            result.success()
    }

    def "should withdraw active asset"() {
        given:
            AssetAvailability asset = someAsset().thatIsActive().get()

        when:
            Result<AssetWithdrawalRejected, AssetWithdrawn> result = asset.withdraw()

        then:
            result.success()
    }


    def "should withdraw active asset that was unlocked"() {
        given:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedBySomeOwner().forSomeValidDuration()
                    .thenUnlocked()
                    .get()

        when:
            Result<AssetWithdrawalRejected, AssetWithdrawn> result = asset.withdraw()

        then:
            result.success()
    }

    def "should fail to withdraw the locked asset"() {
        given:
            AssetAvailability asset = someAsset()
                    .thatIsActive()
                    .thatWasLockedBySomeOwner().forSomeValidDuration()
                    .get()
        when:
            Result<AssetWithdrawalRejected, AssetWithdrawn> result = asset.withdraw()

        then:
            result.failure()
    }
}
