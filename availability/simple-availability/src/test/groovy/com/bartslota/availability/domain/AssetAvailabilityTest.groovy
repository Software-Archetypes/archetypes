package com.bartslota.availability.domain

import com.bartslota.availability.common.Result
import com.bartslota.availability.events.AssetActivated
import com.bartslota.availability.events.AssetActivationRejected
import com.bartslota.availability.events.AssetLockRejected
import com.bartslota.availability.events.AssetLocked
import com.bartslota.availability.events.AssetUnlocked
import com.bartslota.availability.events.AssetUnlockingRejected
import com.bartslota.availability.events.AssetWithdrawalRejected
import com.bartslota.availability.events.AssetWithdrawn
import spock.lang.Specification

import java.time.Duration

import static com.bartslota.availability.domain.AssetAvailabilityFixture.someAsset
import static com.bartslota.availability.domain.AssetAvailabilityFixture.someNewAsset
import static com.bartslota.availability.domain.AssetIdFixture.someAssetId
import static com.bartslota.availability.domain.DurationFixture.someValidDuration
import static com.bartslota.availability.domain.OwnerIdFixture.someOwnerId
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
