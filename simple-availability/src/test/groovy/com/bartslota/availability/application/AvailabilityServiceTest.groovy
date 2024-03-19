package com.bartslota.availability.application

import com.bartslota.availability.domain.AssetAvailability
import com.bartslota.availability.domain.AssetAvailabilityRepository
import com.bartslota.availability.domain.AssetId
import com.bartslota.availability.domain.OwnerId
import spock.lang.Specification

import static com.bartslota.availability.domain.AssetIdFixture.someAssetId
import static com.bartslota.availability.domain.DurationFixture.someValidDuration
import static com.bartslota.availability.domain.OwnerIdFixture.someOwnerId
import static java.time.LocalDateTime.now

class AvailabilityServiceTest extends Specification implements AssetAvailabilityEventsSupport, AssetAvailabilityStoreSupport {

    private AssetAvailabilityRepository repository = new InMemoryAssetAvailabilityRepository()
    private AvailabilityService availabilityService = new AvailabilityService(repository, publisher)

    def "should register asset"() {
        given:
            AssetId assetId = someAssetId()

        when:
            availabilityService.registerAssetWith(assetId)

        then:
            assetIsRegisteredWith(assetId)
    }

    def "should emit AssetRegistered event when successfully registered asset"() {
        given:
            AssetId assetId = someAssetId()

        when:
            availabilityService.registerAssetWith(assetId)

        then:
            assetRegisteredEventWasPublishedFor(assetId)
    }

    def "should withdraw existing asset"() {
        given:
            AssetAvailability asset = existingAsset()

        when:
            availabilityService.withdraw(asset.id())

        then:
            thereIsAWithdrawnAssetWith(asset.id())
    }

    def "should emit AssetWithdrawn event when asset was successfully withdrawn"() {
        given:
            AssetAvailability asset = existingAsset()

        when:
            availabilityService.withdraw(asset.id())

        then:
            assetWithdrawnEventWasPublishedFor(asset.id())
    }

    def "should lock activated asset"() {
        given:
            AssetAvailability asset = activatedAsset()
            OwnerId ownerId = someOwnerId()
        when:
            availabilityService.lock(asset.id(), ownerId, someValidDuration())

        then:
            thereIsALockedAssetWith(asset.id(), ownerId)
    }

    def "should emit AssetLocked event when asset was successfully locked"() {
        given:
            AssetAvailability asset = activatedAsset()
            OwnerId ownerId = someOwnerId()
        when:
            availabilityService.lock(asset.id(), ownerId, someValidDuration())

        then:
            assetLockedEventWasPublishedFor(asset.id(), ownerId)
    }

    def "should lock indefinitely activated asset"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = assetLockedBy(ownerId)

        when:
            availabilityService.lockIndefinitely(asset.id(), ownerId)

        then:
            thereIsALockedAssetWith(asset.id(), ownerId)
    }

    def "should emit AssetLocked event when asset was successfully locked indefinitely"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = assetLockedBy(ownerId)

        when:
            availabilityService.lockIndefinitely(asset.id(), ownerId)

        then:
            assetLockedEventWasPublishedFor(asset.id(), ownerId)
    }

    def "should unlock asset"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = assetLockedBy(ownerId)

        when:
            availabilityService.unlock(asset.id(), ownerId, now())

        then:
            thereIsAnUnlockedAssetWith(asset.id())
    }

    def "should emit AssetUnlocked event when asset was successfully unlocked"() {
        given:
            OwnerId ownerId = someOwnerId()

        and:
            AssetAvailability asset = assetLockedBy(ownerId)

        when:
            availabilityService.unlock(asset.id(), ownerId, now())

        then:
            assetUnlockedEventWasPublishedFor(asset.id(), ownerId)
    }

    @Override
    AssetAvailabilityRepository repository() {
        return repository
    }

}
