package com.bartslota.availability.application

import com.bartslota.availability.domain.AssetAvailability
import com.bartslota.availability.domain.AssetAvailabilityRepository
import com.bartslota.availability.domain.AssetId
import com.bartslota.availability.domain.OwnerId

import static com.bartslota.availability.domain.AssetAvailabilityFixture.someAsset
import static com.bartslota.availability.domain.AssetAvailabilityFixture.someNewAsset

trait AssetAvailabilityStoreSupport {

    abstract AssetAvailabilityRepository repository()

    AssetAvailability existingAsset() {
        AssetAvailability asset = someNewAsset()
        repository().save(asset)
        asset
    }

    AssetAvailability activatedAsset() {
        AssetAvailability asset = someAsset().thatIsActive().get()
        repository().save(asset)
        asset
    }

    AssetAvailability assetLockedBy(OwnerId ownerId) {
        AssetAvailability asset = someAsset().thatIsActive().thatWasLockedByOwnerWith(ownerId).forSomeValidDuration().get()
        repository().save(asset)
        asset
    }

    AssetAvailability lockedAsset() {
        AssetAvailability asset = someAsset()
                .thatIsActive()
                .thatWasLockedBySomeOwner()
                .forSomeValidDuration()
                .get()
        repository().save(asset)
        asset
    }

    boolean thereIsAWithdrawnAssetWith(AssetId id) {
        repository().findBy(id).flatMap { it.currentLock() }.filter { it instanceof AssetAvailability.WithdrawalLock }.isPresent()
    }

    boolean thereIsALockedAssetWith(AssetId assetId, OwnerId ownerId) {
        repository().findBy(assetId).flatMap { it.currentLock() }.filter { it instanceof AssetAvailability.OwnerLock && it.ownerId() == ownerId }.isPresent()
    }

    boolean assetIsRegisteredWith(AssetId assetId) {
        repository().findBy(assetId).isPresent()
    }

    boolean thereIsAnUnlockedAssetWith(AssetId assetId) {
        repository().findBy(assetId).filter { it.currentLock().isEmpty() }.isPresent()
    }

}